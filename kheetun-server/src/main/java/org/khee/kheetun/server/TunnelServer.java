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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.server.comm.Protocol;
import org.khee.kheetun.server.manager.TunnelManager;

public class TunnelServer implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    
    private ServerSocket                    serverSocket;
    private Socket                          clientSocket        = null;
    private ObjectInputStream               commIn;
    private ObjectOutputStream              commOut;
    
    public TunnelServer( int port ) {
            
        try {
            serverSocket = new ServerSocket( port, 99, InetAddress.getLocalHost() );
            logger.info( "Listening on 127.0.0.1:" + port );
            
            while( true ) {
                
                Socket client = serverSocket.accept();
                
                new TunnelServer( client );
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
    
    public TunnelServer( Socket clientSocket ) {
        
        this.clientSocket   = clientSocket;
        
        Thread server = new Thread( this, "kheetun-server-thread-" + clientSocket.getInetAddress().toString() + "/" + clientSocket.getLocalPort() );
        server.start();
    }
    
    public TunnelServer() {
        
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
                    
                    this.handle( receive );
                    
                } catch ( ClassNotFoundException e ) {
                    logger.error( "Class error: " + e.getMessage() );
                } catch ( InvalidClassException eInvalid ) {
                    logger.error( "Invalid class: " + eInvalid.getMessage() );
                } catch ( EOFException eEof ) {
                    logger.error( "Client at " + clientSocket.getInetAddress().toString() + " unexpectedly closed connection: " + eEof.getMessage() );
                    break;
                }
                
            } while ( ( receive.getCommand() != Protocol.QUIT && receive.getCommand() != Protocol.DISCONNECT ) ); 
            
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
    
    private void handle( Protocol receive ) {
        
        logger.debug( "Server handling: " + receive );
        
        if ( receive.getTunnel() != null ) {
            
            int indexTunnel = TunnelManager.get( receive.getUser() ).getKnownTunnels().indexOf( receive.getTunnel() );
            if ( indexTunnel != -1 ) {
                receive.setTunnel( TunnelManager.get( receive.getUser() ).getKnownTunnels().get( indexTunnel ) );
            }
        }
        
        switch ( receive.getCommand() ) {
        
        case Protocol.ECHO:
            
            logger.info( "Client echo: " + receive.getString() );
            break;

        case Protocol.CONNECT:
            
            TunnelManager.get( receive.getUser() ).setServer( this );

            this.send( new Protocol( Protocol.ACCEPT ) );
            break;
            
        case Protocol.CONFIG:
            
            TunnelManager.get( receive.getUser() ).setConfig( receive.getConfig() );
            logger.trace( "Received config: " + receive.getConfig() );
            break;

        case Protocol.START:
            
            TunnelManager.get( receive.getUser() ).startTunnel( receive.getTunnel(), true );
            break;
            
        case Protocol.STOP:
            
            TunnelManager.get( receive.getUser() ).stopTunnel( receive.getTunnel(), true );
            break;
            
        case Protocol.STOPALL:
            
            TunnelManager.get( receive.getUser() ).stopAllTunnels();
            break;
            
        case Protocol.TOGGLE:
            
            
            TunnelManager.get( receive.getUser() ).toggleTunnel( receive.getTunnel() );
            break;
        
        case Protocol.QUIT:
            
            this.send( new Protocol( Protocol.QUIT ) );
            break;
            
        case Protocol.DISCONNECT:
            
            this.send( new Protocol( Protocol.DISCONNECT ) );
            break;
            

        default:
            break;
        }
    }
    
    public boolean isConnected() {
        
        return this.clientSocket.isConnected();
    }
    
    public synchronized void send( Protocol protocol ) {
        
        if ( clientSocket == null || ! clientSocket.isConnected() || clientSocket.isClosed() ) {
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
