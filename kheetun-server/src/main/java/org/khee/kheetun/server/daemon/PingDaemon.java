package org.khee.kheetun.server.daemon;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Tunnel;

import com.jcraft.jsch.Session;

public class PingDaemon implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    
    private Boolean isRunning = true;
    private Session session;
    private Tunnel tunnel;
    private int failCount = 0;
    private ArrayList<PingDaemonListener> listeners = new ArrayList<PingDaemonListener>();
    
    public PingDaemon( Tunnel tunnel, Session session ) {
        this.tunnel  = tunnel;
        this.session = session;
        
        Thread daemon = new Thread( this );
        daemon.start();
    }
    
    
    @Override
    public void run() {
        
        while( isRunning ) {
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }

            
            if ( ! session.isConnected() ) {
                
                if ( ++failCount > 3 ) {
                
                    for( PingDaemonListener listener : listeners ) {
                        
                        listener.PingFailed( this, tunnel, session );
                    }
                }
                logger.debug( "PING FAIL ( " + failCount + "/3 ): " + tunnel.getSignature() );
            } else {
                
                failCount = 0;
                logger.debug( "PING OK: " + tunnel.getSignature() );
            }
            
        }
    }
    
    public void stop() {
        isRunning = false;
    }
    
    public void addPingDaemonListener( PingDaemonListener listener ) {
    
        listeners.add( listener );
    }
    

}
