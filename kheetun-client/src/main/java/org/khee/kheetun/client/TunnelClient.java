package org.khee.kheetun.client;

import java.awt.Component;
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
import org.khee.kheetun.client.config.Tunnel;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;


public class TunnelClient implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private Socket                          clientSocket;
    private Integer                         port;
    private Protocol                        receive;
    private ObjectInputStream               commIn;
    private ObjectOutputStream              commOut;
    private static TunnelClient             instance;
    private Thread                          client;
    private boolean                         clientRunning;
    private ArrayList<TunnelClientListener> listeners;
    private Semaphore                       sender = new Semaphore( 1 );
    private boolean                         connected = false;
    
    
    protected TunnelClient() {

        client              = new Thread( this );
    }
    
    public static void init() {
        
        /*
         * if there already was an instance, dont forget to copy the action listeners
         */
        ArrayList<TunnelClientListener> oldListeners = new ArrayList<TunnelClientListener>();
        if ( instance != null && instance.listeners.size() > 0 ) {
             oldListeners = new ArrayList<TunnelClientListener>( instance.listeners );
        }
        
        instance = new TunnelClient();
        instance.listeners = oldListeners;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    
    public static void connect( Integer port ) {
        
        instance.startClient( port );
    }
    
    public static void disconnect() {
        assert( instance != null );
        
        instance.stopClient();
    }
    
    public static void addClientListener( TunnelClientListener listener ) {
        assert( instance != null );
        
        instance.listeners.add( listener );
    }
    
    public void startClient( Integer port ) {
        
        if ( ! client.isAlive() ) {
            
            setPort( port );
            client.start();
        }
    }
    
    public void stopClient() {
        
        if ( client.isAlive() ) {
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
        
        for ( TunnelClientListener listener : listeners ) {
            listener.disconnected();
        }
        
    }
    
    public void run() {
        
        clientRunning   = true;
        
        logger.info( "Connecting to kheetun server" );
        
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
//                    Thread.sleep( 1000 );
                    
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
        
        clientRunning   = false;

        
    }
    
    private void handle( Protocol receive ) {
        
        logger.debug( "Client handling " + receive );
        
        switch ( receive.getCommand() ) {

        case Protocol.ECHO:
            logger.info( "Server echo: " + receive.getString() );
            break;

        case Protocol.ACCEPT:
            
            connected = true;
            
            send( new Protocol( Protocol.ECHO, "Its cool you accepted me!" ) );

            for ( TunnelClientListener listener : listeners ) {
                listener.connected();
            }
            break;
            
        case Protocol.TUNNELSTARTED:

            for ( TunnelClientListener listener : listeners ) {
                listener.tunnelStarted( receive.getTunnel().getSignature() );
            }
            break;
            
        case Protocol.TUNNELSTOPPED:
            
            for ( TunnelClientListener listener : listeners ) {
                listener.tunnelStopped( receive.getTunnel().getSignature() );
            }
            break;

        case Protocol.ERROR:
            logger.error( "Error on server: " + receive.getString() );
            for ( TunnelClientListener listener : listeners ) {
                listener.error( receive.getTunnel(), receive.getString() );
            }
            break;
            
        case Protocol.ACTIVETUNNELS:
            
            for ( TunnelClientListener listener : listeners ) {
                listener.activeTunnels( receive.getSignatures() );
            }
            break;
        
        case Protocol.PING:
            
            for ( TunnelClientListener listener : listeners ) {
                listener.tunnelPing( receive.getTunnel().getSignature(), receive.getNumber() );
            }
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
    
    public static void sendStartTunnel( Component sender, Tunnel tunnel ) {
        
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
                JOptionPane.showMessageDialog( sender, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
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
        
        if ( ! clientSocket.isConnected() ) {
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
