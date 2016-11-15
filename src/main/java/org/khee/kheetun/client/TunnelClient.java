package org.khee.kheetun.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.GlobalConfig;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.server.comm.Protocol;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;


public class TunnelClient implements Runnable, ConfigManagerListener {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private Socket                          clientSocket;
    private Protocol                        receive;
    private ObjectInputStream               commIn;
    private ObjectOutputStream              commOut;
    private static TunnelClient             instance;
    private Thread                          client;
    private boolean                         clientRunning;
    private CopyOnWriteArrayList<TunnelClientListener> listeners       = new CopyOnWriteArrayList<TunnelClientListener>();
    private Integer                         port            = -1;
    private Semaphore                       sender          = new Semaphore( 1 );
    
    protected TunnelClient() {

        ConfigManager.addConfigManagerListener( this );
    }
    
    public static void init() {
        
        instance = new TunnelClient();
    }
    
    public static boolean isConnected() {
        
        return ( instance.clientSocket != null && instance.clientSocket.isConnected() );
    }
    
    @Override
    public void configManagerGlobalConfigChanged( GlobalConfig oldConfig, GlobalConfig newConfig, boolean valid ) {
        
        if ( valid && ! this.port.equals( newConfig.getPort() ) ) {

            logger.info( "Connecting to port " + newConfig.getPort() + " after config change" );
            
            this.port = newConfig.getPort();
            
            this.send( new Protocol( Protocol.DISCONNECT ) );
            
            if ( this.client == null ) {
                this.client = new Thread( this, "kheetun-client-thread" );
                this.client.start();
            }
        }        
    }
    
    @Override
    public void configManagerConfigChanged( Config oldConfig, Config newConfig, boolean valid ) {

            TunnelClient.sendConfig( newConfig );
    }
    
    public void run() {
        
        clientRunning   = true;
        
        while ( clientRunning ) {
        
            logger.info( "Trying to connect to kheetun server at port " + port );
            
            try {
                
                clientSocket = new Socket( InetAddress.getLocalHost(), port );
                commOut     = new ObjectOutputStream( clientSocket.getOutputStream() );
                commOut.flush();
    
                send( new Protocol( Protocol.CONNECT ) );
                
                commIn      = new ObjectInputStream( clientSocket.getInputStream() );
                
                do {
                    try {
                        
                        receive = (Protocol)commIn.readObject();
                        this.handle( receive );
                        
                    } catch ( ClassNotFoundException e ) {
                        logger.error( "Class error: " + e.getMessage() );
                    }
                } while ( receive.getCommand() != Protocol.DISCONNECT && receive.getCommand() != Protocol.QUIT && clientRunning );
                
            } catch ( ConnectException eConnect ) {
                
                logger.error( "Could not connect to kheetun server: " + eConnect.getMessage() );
                
            } catch ( SocketException eSocket ) {
                
                logger.error( "Socket error: " + eSocket.getMessage() + ", disconnected" );
            } catch ( IOException eIO ) {
                
                logger.error( "Socket IO error: " + eIO.getMessage() + ", disconnected" );
            } catch ( Exception e ) {
                
                logger.warn( "", e );
            }
            
            try {
                if ( commIn != null ) {
                    commIn.close();
                }
                if ( commOut != null ) {
                    commOut.close();
                }
                if ( clientSocket != null ) {
                    clientSocket.close();
                    logger.info( "Disconnected from kheetun server" );
                    
                    for( TunnelClientListener listener : this.listeners ) {
                        listener.TunnelClientConnection( false );
                    }
                }
            
            } catch ( IOException e ) {
                
                logger.warn( e.getMessage() );
            }

            logger.debug( "Disconnected from kheetun server, retrying in 2 seconds" );
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                logger.warn( "Error while trying to sleep before reconnect: " + e.getLocalizedMessage() );
            }
        }
        
        logger.info( "Client stopped" );
    }
    
    private void handle( Protocol receive ) {
        
        logger.debug( "Client handling " + receive );
        
        switch ( receive.getCommand() ) {

        case Protocol.ECHO:
            logger.info( "Server echo: " + receive.getString() );
            break;

        case Protocol.ACCEPT:
            
            logger.info( "Connected to kheetun server" );
            
            for( TunnelClientListener listener : this.listeners ) {
                listener.TunnelClientConnection( true );
            }
            
            TunnelClient.sendConfig( ConfigManager.getConfig() );
            break;
            
        case Protocol.QUIT:
            
            logger.info( "Received QUIT command, bye!" );
            System.exit( 0 );
            break;
            
        case Protocol.TUNNEL:
            
            for( TunnelClientListener listener : this.listeners ) {
                listener.TunnelClientTunnelStatus( receive.getTunnel() );
            }
            break;
            
        default:
            break;
        }
    }
    
    public static void sendQuit() {
        
        instance.send( new Protocol( Protocol.QUIT ) );
    }
    
    public static void sendQueryTunnel( Tunnel tunnel ) {
        
        instance.send( new Protocol( Protocol.TUNNEL, tunnel ) );
    }
    
    public static void sendStartTunnel( Tunnel tunnel ) {
        
        if ( tunnel.getSshKey() != null ) {

            KeyPair keypair = null;
            try {
                keypair = KeyPair.load( new JSch(), tunnel.getSshKeyString() );
            } catch ( JSchException e ) {
                
                logger.error( e.getMessage() );
                logger.debug( "", e );
                return;
            }
            
            if ( keypair.isEncrypted() ) {
                
                JPasswordField password = new JPasswordField();
                
                int answer = JOptionPane.showConfirmDialog( null, password, "Give me the passphrase", JOptionPane.OK_CANCEL_OPTION );
                
                if ( answer == JOptionPane.OK_OPTION ) {
                    
                    tunnel.setPassPhrase( new String( password.getPassword() ) );
                } else {
                    tunnel.setPassPhrase( "" );
                    return;
                }
                
                if ( ! keypair.decrypt( tunnel.getPassPhrase() ) ) {
                    JOptionPane.showMessageDialog( null , "I'm afraid that passphrase does not compute :]" );
                    tunnel.setPassPhrase( "" );
                    return;
                }
            }
        }
        
        instance.send(new Protocol( Protocol.START, tunnel,  "" ));
        tunnel.setPassPhrase( "" );
    }
    
    public static void sendStop( Tunnel tunnel ) {
        
        instance.send( new Protocol( Protocol.STOP, tunnel ) );
    }
    
    public static void sendStopAll() {
        
        instance.send( new Protocol( Protocol.STOPALL ) );
    }
    
    public static void sendToggle( Tunnel tunnel ) {
        
        instance.send( new Protocol( Protocol.TOGGLE, tunnel ) );
    }
    
    public static void sendAutoAll() {
        
        instance.send( new Protocol( Protocol.AUTOALL ) );
    }
    
    public static void sendConfig( Config config ) {
        
        instance.send( new Protocol( Protocol.CONFIG, config ) );
    }
    
    private void send( Protocol protocol ) {
        
        if ( clientSocket == null || ! clientSocket.isConnected() ) {
            
            logger.warn( "Client not connected while trying to send" );
            return;
        }
        
        logger.debug( "Client sending: " + protocol );
        
        try {
            
            sender.acquire();
            
            commOut.reset();
            commOut.writeObject( protocol );
            commOut.flush();
            
            sender.release();
            
        } catch( Exception e ) {
            
            logger.error( "Error during send: " + e.getMessage() );
        }
    }
    
    public static void addTunnelClientListener( TunnelClientListener listener ) {
        
        instance.listeners.add( listener );
    }

}
