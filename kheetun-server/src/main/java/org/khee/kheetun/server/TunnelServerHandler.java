package org.khee.kheetun.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.comm.Protocol;
import org.khee.kheetun.server.manager.TunnelManager;

public class TunnelServerHandler implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );

    private TunnelServer    server;
    private TunnelManager   tunnelManager;
    private Protocol        protocol;
    
    public TunnelServerHandler( TunnelServer server, TunnelManager tunnelManager, Protocol protocol ) {
        
        this.server = server;
        this.tunnelManager = tunnelManager;
        this.protocol = protocol;
        
        Thread handler = new Thread( this, "kheetund-server-handler" );
        handler.start();
    }
    
    
    @Override
    public void run() {
        
        this.handle( protocol );
    }
    
    private void handle( Protocol receive ) {
        
        logger.debug( "Server handling: " + receive );
        
        switch ( receive.getCommand() ) {
        
        case Protocol.ECHO:
            logger.info( "Client echo: " + receive.getString() );
            break;

        case Protocol.CONNECT:

            server.createTunnelManager( receive.getUser() );
            server.send( new Protocol( Protocol.ACCEPT ) );
            break;

        case Protocol.STARTTUNNEL:
            
            tunnelManager.startTunnel( receive.getTunnel() );
                
            break;
            
        case Protocol.STOPTUNNEL:
            
            this.tunnelManager.stopTunnel( receive.getTunnel() );
            
            break;
            
        case Protocol.STOPALLTUNNELS:
            
            this.tunnelManager.stopAllTunnels();
            
            break;
        
        case Protocol.QUERYTUNNELS:
            
            server.send( new Protocol( Protocol.ACTIVETUNNELS, tunnelManager.getActiveTunnels() ) );
            break;
            
        case Protocol.QUIT:
            
            server.send( new Protocol( Protocol.QUIT ) );
            break;
            
        case Protocol.DISCONNECT:
            
            server.send( new Protocol( Protocol.DISCONNECT ) );
            break;
            

        default:
            break;
        }
        
    }

}
