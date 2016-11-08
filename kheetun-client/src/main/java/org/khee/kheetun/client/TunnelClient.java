package org.khee.kheetun.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.comm.Protocol;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.GlobalConfig;
import org.khee.kheetun.client.config.Tunnel;

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
    private Integer                         port            = -1;
    private Semaphore                       sender          = new Semaphore( 1 );
    
    protected TunnelClient() {

        ConfigManager.addConfigManagerListener( this );
    }
    
    public static void init() {
        
        instance = new TunnelClient();
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
                        handle( receive );
                        
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
                
                logger.warn( e.getMessage() );
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

            TunnelManager.offline();            
            
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
        
        // translate tunnels to tunnels known by configuration
        // unknown tunnels are stale tunnels
        //
        if ( receive.getTunnel() != null ) {
            
            if ( ConfigManager.getTunnel( receive.getTunnel() ) != null ) {
                receive.setTunnel( ConfigManager.getTunnel( receive.getTunnel() ) );
            } else {
                logger.info( "Stale tunnel: " + receive.getTunnel() );
            }
        }
        
        if ( receive.getTunnels().size() > 0 ) {
            
            ArrayList<Tunnel> tunnels = new ArrayList<Tunnel>();
            
            for ( Tunnel tunnel : receive.getTunnels() ) {
                
                if ( ConfigManager.getTunnel( tunnel ) != null ) {
                    tunnels.add( ConfigManager.getTunnel( tunnel ) );
                } else {
                    logger.info( "Stale tunnel: " + tunnel );
                }
            }
            
            receive.setTunnels( tunnels );
        }
        
        switch ( receive.getCommand() ) {

        case Protocol.ECHO:
            logger.info( "Server echo: " + receive.getString() );
            break;

        case Protocol.ACCEPT:
            
            logger.info( "Connected to kheetun server" );
            
            send( new Protocol( Protocol.ECHO, "Its cool you accepted me!" ) );
            
            TunnelClient.sendQueryTunnels();
            TunnelManager.online();
            break;
            
        case Protocol.TUNNELSTARTED:
            
            TunnelManager.started( receive.getTunnel() );
            break;
            
        case Protocol.TUNNELSTOPPED:
            
            TunnelManager.stopped( receive.getTunnel() );
            break;

        case Protocol.ERROR:
            logger.error( "Error on server: " + receive.getString() );
            
            TunnelManager.raiseError( receive.getTunnel(), receive.getString() );
            break;
            
        case Protocol.ACTIVETUNNELS:
            
            TunnelManager.refreshActivated( receive.getTunnels() );
            break;
        
        case Protocol.PING:
            
            TunnelManager.updatePing( receive.getTunnel(), receive.getNumber() );
            break;
            
        case Protocol.QUIT:
            
            logger.info( "Received QUIT command, bye!" );
            System.exit( 0 );
            break;
            
        default:
            break;
        }
    }
    
    public static void sendQuit() {
        
        instance.send( new Protocol( Protocol.QUIT ) );
    }
    
    public static void sendQueryTunnels() {
        
        instance.send( new Protocol( Protocol.QUERYTUNNELS ) );
    }
    
    public static void sendStartTunnel( Tunnel tunnel ) {
        
        if ( tunnel.getSshKey() != null ) {

            KeyPair keypair = null;
            try {
                keypair = KeyPair.load( new JSch(), tunnel.getSshKeyString() );
            } catch ( JSchException e ) {
                
                logger.error( e.getMessage() );
                logger.debug( "", e );
                TunnelManager.raiseError( tunnel, e.getMessage() );
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
        
        instance.send(new Protocol( Protocol.STARTTUNNEL, tunnel,  "" ));
        tunnel.setPassPhrase( "" );
    }
    
    public static void sendStopTunnel( Tunnel tunnel ) {
        
        instance.send( new Protocol( Protocol.STOPTUNNEL, tunnel ) );
    }
    
    public static void sendStopAllTunnels() {
        
        instance.send( new Protocol( Protocol.STOPALLTUNNELS ) );
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

}
