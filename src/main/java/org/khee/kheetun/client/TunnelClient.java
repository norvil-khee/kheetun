package org.khee.kheetun.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

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
    
    public static final int                             CONNECT_OK                  = 0;
    public static final int                             CONNECT_ERROR_REFUSED       = 100;
    public static final int                             CONNECT_ERROR_OTHER         = 900;
    
    private Socket                                      clientSocket;
    private Protocol                                    receive;
    private ObjectInputStream                           commIn;
    private ObjectOutputStream                          commOut;
    private static TunnelClient                         instance;
    private Thread                                      client;
    private boolean                                     clientRunning;
    private CopyOnWriteArrayList<TunnelClientListener>  listeners       = new CopyOnWriteArrayList<TunnelClientListener>();
    private Integer                                     port            = -1;
    private String                                      host            = null;
    
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
        
        if ( valid && newConfig.getHost() != null && ( ! newConfig.getHost().equals( this.host ) || ! this.port.equals( newConfig.getPort() ) ) ) {

            logger.info( "Connecting to " + newConfig.getHost() + ":" + newConfig.getPort() );
            
            this.port = newConfig.getPort();
            this.host = newConfig.getHost();
            
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
        
        logger.info( "Trying to connect to kheetun server at " + this.host + ":" + this.port );
        
        clientRunning   = true;
        
        String connectionError          = null;
        String connectionErrorBefore    = null;
        int connectionStatus            = TunnelClient.CONNECT_OK;
        int connectionStatusBefore      = TunnelClient.CONNECT_OK;
        
        while ( clientRunning ) {
        
            if ( connectionStatus != connectionStatusBefore || ( connectionError != null && ! connectionError.equals( connectionErrorBefore ) ) ) {
                
                logger.info( "Last connection attempt unsuccessful: " + connectionError + ", retrying connection to " + this.host + ":" + this.port );
            }
            
            connectionStatusBefore  = connectionStatus;
            connectionErrorBefore   = connectionError;
            
            try {
                
                clientSocket = new Socket( this.host, this.port );
                commOut      = new ObjectOutputStream( clientSocket.getOutputStream() );
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
                
                connectionError = eConnect.getMessage();
                connectionStatus = TunnelClient.CONNECT_ERROR_REFUSED;
                
            } catch ( SocketException eSocket ) {
                
                connectionError = eSocket.getMessage();
                connectionStatus = TunnelClient.CONNECT_ERROR_OTHER;
            } catch ( IOException eIO ) {
                
                connectionError = eIO.getMessage();
                connectionStatus = TunnelClient.CONNECT_ERROR_OTHER;
            } catch ( Exception e ) {
                
                connectionError = e.getMessage();
                connectionStatus = TunnelClient.CONNECT_ERROR_OTHER;
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
                }
            
            } catch ( IOException e ) {
                
                logger.warn( e.getMessage() );
            }
            
            if ( connectionStatus != connectionStatusBefore || ( connectionError != null && ! connectionError.equals( connectionErrorBefore ) ) ) {

                for( TunnelClientListener listener : this.listeners ) {
                    listener.TunnelClientConnection( false, connectionError, connectionStatus );
                }
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
                listener.TunnelClientConnection( true, null, TunnelClient.CONNECT_OK );
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
    
    private synchronized void send( Protocol protocol ) {
        
        if ( clientSocket == null || ! clientSocket.isConnected() ) {
            
            return;
        }
        
        logger.debug( "Client sending: " + protocol );
        
        try {
            
            commOut.reset();
            commOut.writeObject( protocol );
            commOut.flush();
            
        } catch( Exception e ) {
            
            logger.error( "Error during send: " + e.getMessage() );
        }
    }
    
    public static void addTunnelClientListener( TunnelClientListener listener ) {
        
        instance.listeners.add( listener );
    }

    public static void removeTunnelClientListener( TunnelClientListener listener ) {
        
        instance.listeners.remove( listener );
    }

}
