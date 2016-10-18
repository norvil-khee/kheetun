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
    private boolean                         connected       = false;
    
    
    protected TunnelClient() {

        ConfigManager.addConfigManagerListener( this );
    }
    
    public static void init() {
        
        instance = new TunnelClient();
    }
    
    @Override
    public void configManagerConfigChanged( Config config ) {
    }
    
    @Override
    public void configManagerConfigInvalid( Config config, ArrayList<String> errorStack ) {
        
        this.stopClient();
    }
    
    @Override
    public void configManagerConfigValid(Config config) {
        
        if ( ! this.port.equals( config.getPort() ) || ! connected ) {
            
            logger.info( "Port changed from " + this.port + " to " + config.getPort() );
            
            this.stopClient();
            
            if ( client != null && client.isAlive() ) {
                try {
                    client.join();
                } catch ( InterruptedException e ) {
                    
                    logger.error( "Interrupted while waiting for client thread to stop" );
                }
            }
            
            this.port = config.getPort();
            this.client = new Thread( this, "kheetun-client-thread" );
            this.client.start();
        }
    }
 
    public static void disconnect() {
        assert( instance != null );
        
        instance.stopClient();
    }
    
    public void stopClient() {
        
        if ( client != null && client.isAlive() ) {
            send( new Protocol( Protocol.QUIT ) );
            
            instance.clientRunning = false;
        }
    }
    
    private void closeSocket() {
        
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
                } while ( receive.getCommand() != Protocol.QUIT && clientRunning );
                
            } catch ( ConnectException eConnect ) {
                
                logger.error( "Could not connect to kheetun server: " + eConnect.getMessage() );
                
            } catch ( SocketException eSocket ) {
                
                logger.error( "Socket error: " + eSocket.getMessage() + ", disconnected" );
            } catch ( IOException eIO ) {
                
                logger.error( "Socket IO error: " + eIO.getMessage() + ", disconnected" );
            } catch ( Exception e ) {
                
                logger.warn( e.getMessage() );
            }
    
            closeSocket();
            connected = false;
            logger.info( "Disconnected from kheetun server, retrying in 2 seconds" );
            
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
            
            connected = true;
            
            logger.info( "Connected to kheetun server" );
            
            send( new Protocol( Protocol.ECHO, "Its cool you accepted me!" ) );
            
            TunnelClient.sendQueryTunnels();
            TunnelManager.online();
            break;
            
        case Protocol.TUNNELSTARTED:
            
            TunnelManager.started( receive.getTunnel().getSignature() );
            break;
            
        case Protocol.TUNNELSTOPPED:
            
            TunnelManager.stopped( receive.getTunnel().getSignature() );
            break;

        case Protocol.ERROR:
            logger.error( "Error on server: " + receive.getString() );
            
            TunnelManager.raiseError( receive.getTunnel(), receive.getString() );
            break;
            
        case Protocol.ACTIVETUNNELS:
            
            TunnelManager.refreshActivated( receive.getSignatures() );
            break;
        
        case Protocol.PING:
            
            TunnelManager.updatePing( receive.getTunnel().getSignature(), receive.getNumber() );
            break;
            
        default:
            break;
        }
    }
    
    
    public static void sendQueryTunnels() {
        
        if ( instance == null || ! instance.connected ) {
            return;
        }
        
        instance.send( new Protocol( Protocol.QUERYTUNNELS ) );
    }
    
    public static void sendStartTunnel( Tunnel tunnel ) {
        
        if ( instance == null || ! instance.connected ) {
            logger.warn( "No connection" );
            return;
        }
        
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
                
                Tunnel protectedTunnel = new Tunnel( tunnel );
                
                JPasswordField password = new JPasswordField();
                
                int answer = JOptionPane.showConfirmDialog( null, password, "Give me the passphrase", JOptionPane.OK_CANCEL_OPTION );
                
                if ( answer == JOptionPane.OK_OPTION ) {
                    
                    protectedTunnel.setPassPhrase( new String( password.getPassword() ) );
                } else {
                    protectedTunnel.setPassPhrase( "" );
                    return;
                }
                
                if ( ! keypair.decrypt( tunnel.getPassPhrase() ) ) {
                    JOptionPane.showMessageDialog( null , "I'm afraid that passphrase does not compute :]" );
                    protectedTunnel.setPassPhrase( "" );
                    return;
                }
                
                tunnel = protectedTunnel;
            }
        }
        
        
        instance.send(new Protocol( Protocol.STARTTUNNEL, tunnel,  "" ));
        
//        instance.send( new Protocol( Protocol.STARTTUNNEL, tunnel, System.getenv( "SSH_AUTH_SOCK" ) ) );
        
        tunnel.setPassPhrase( "" );
    }
    
    public static void sendStopTunnel( Tunnel tunnel ) {
        
        if ( instance == null || ! instance.connected ) {
            logger.warn( "No connection" );
            return;
        }
        
        instance.send( new Protocol( Protocol.STOPTUNNEL, tunnel ) );
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
