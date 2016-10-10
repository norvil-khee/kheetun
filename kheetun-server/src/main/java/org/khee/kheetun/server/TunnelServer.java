package org.khee.kheetun.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.comm.Protocol;
import org.khee.kheetun.server.manager.TunnelManager;

public class TunnelServer implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    
    private ServerSocket                    serverSocket;
    private Socket                          clientSocket;
    private ObjectInputStream               commIn;
    private ObjectOutputStream              commOut;
    private HashMap<String, TunnelManager>  tunnelManagers;
    private TunnelManager                   tunnelManager;
    private TunnelServer                    parent;
    
    public TunnelServer( int port ) {

        tunnelManagers = new HashMap<String, TunnelManager>();
            
        try {
            serverSocket = new ServerSocket( port, 99, InetAddress.getLocalHost() );
            logger.info( "Listening on 127.0.0.1:" + port );
            
            while( true ) {
                
                Socket client = serverSocket.accept();
                
                new TunnelServer( client, this );
            }
        } catch ( BindException eBind ) {
            
            logger.fatal( "Failed to bind socket: " + eBind.getMessage() );
            System.exit( 1 );
            
        } catch ( IOException eIO ) {
            
            logger.fatal( "Socket IO exception after bind: " + eIO.getMessage() );
            System.exit( 1 );
            
        } catch ( Exception e ) {
            
            logger.error( e.getMessage() );
        }
    }
    
    public TunnelServer( Socket clientSocket, TunnelServer parent ) {
        
        this.clientSocket   = clientSocket;
        this.parent         = parent;
        
        Thread server = new Thread( this );
        server.start();
    }

    public void run() {
        
        logger.info( "Connection from " + clientSocket.getInetAddress().toString() );
        
        try {
            commOut = new ObjectOutputStream( clientSocket.getOutputStream() );
            commOut.flush();
            commIn  = new ObjectInputStream( clientSocket.getInputStream() );
            
            Protocol receive = null;
            
            do {
                try {
                    
                    receive = (Protocol)commIn.readObject();
                    handle( receive );
                    
                } catch ( ClassNotFoundException e ) {
                    logger.error( "Class error: " + e.getMessage() );
                } catch ( InvalidClassException eInvalid ) {
                    logger.error( "Invalid class: " + eInvalid.getMessage() );
                } catch ( EOFException eEof ) {
                    logger.error( "Client at " + clientSocket.getInetAddress().toString() + " unexpectedly closed connection: " + eEof.getMessage() );
                    break;
                }
                
            } while ( receive.getCommand() != Protocol.QUIT );
            
            logger.info( "Client at " + clientSocket.getInetAddress().toString() + " disconnected" );
            
            if ( commOut != null ) {
                commOut.close();
            }
            if ( commIn != null ) {
                commIn.close();
            }
            
            if ( clientSocket != null ) {
                clientSocket.close();
            }
            
        } catch ( SocketException eSocket ) {
            
            logger.error( "Client at " + clientSocket.getInetAddress().toString() + " got disconnected uncleanly: " + eSocket.getMessage() );
            
        } catch ( IOException eIO ) {
            
            logger.error( "Client at " + clientSocket.getInetAddress().toString() + " got disconnected uncleanly: " + eIO.getMessage() );
        } catch ( Exception e ) {
            
            e.printStackTrace();
            logger.error( e.getMessage() );
        }
        
        
    }
    
    private void createTunnelManager( String user ) {
        
        /*
         * create tunnel manager if not already existant for given user
         */
        if ( ! parent.tunnelManagers.containsKey( user ) ) {
            
            tunnelManager = new TunnelManager();
            parent.tunnelManagers.put( user, tunnelManager );
            
            logger.info( "Created a tunnel manager for user " + user );
        } else {

            logger.info( "There already is a tunnel manager for user " + user + ", using this one" );
            tunnelManager = parent.tunnelManagers.get( user );
        }
        
        tunnelManager.setServer( this );
    }
    
    private void handle( Protocol receive ) {
        
        logger.debug( "Server handling: " + receive );
        
        switch ( receive.getCommand() ) {
        
        case Protocol.ECHO:
            logger.info( "Client echo: " + receive.getString() );
            break;

        case Protocol.CONNECT:

            createTunnelManager( receive.getUser() );
            send( new Protocol( Protocol.ACCEPT ) );
            break;

        case Protocol.STARTTUNNEL:
            
            if ( tunnelManager.startTunnel( receive.getTunnel() ) ) {
                
                send( new Protocol( Protocol.TUNNELSTARTED, receive.getTunnel() ) );
            }
            break;
            
        case Protocol.STOPTUNNEL:
            
            if ( tunnelManager.stopTunnel( receive.getTunnel() ) ) {
            
                send( new Protocol( Protocol.TUNNELSTOPPED, receive.getTunnel() ) );
            }
            break;
        
        case Protocol.QUERYTUNNELS:
            
            send( new Protocol( Protocol.ACTIVETUNNELS, tunnelManager.getActiveTunnels() ) );
            break;
            
        case Protocol.QUIT:
            
            send( new Protocol( Protocol.QUIT ) );
            break;

        default:
            break;
        }
        
    }
    
    public synchronized void send( Protocol protocol ) {
        
        if ( clientSocket == null || ! clientSocket.isConnected() ) {
            return;
        }
        
        logger.debug( "Server sending: " + protocol );
        
        try {
            commOut.reset();
            commOut.writeObject( protocol );
            commOut.flush();
            
        } catch( Exception e ) {
            
            logger.error( "Error during send: " + e.getMessage() );
        }
    }
    

    
}
