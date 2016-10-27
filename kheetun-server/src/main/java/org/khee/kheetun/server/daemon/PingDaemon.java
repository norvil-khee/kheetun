package org.khee.kheetun.server.daemon;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Tunnel;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class PingDaemon implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    
    private Boolean                         isRunning = true;
    private Session                         session;
    private Tunnel                          tunnel;
    private int                             failCount = 0;
    private ArrayList<PingDaemonListener>   listeners = new ArrayList<PingDaemonListener>();
    
    public PingDaemon( Tunnel tunnel, Session session ) {
        this.tunnel  = tunnel;
        this.session = session;
        
        Thread daemon = new Thread( this, "kheetun-ping-thread" );
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
            
            if ( session.isConnected() ) {
                
                try {
                    
                    Channel channel = session.openChannel( "exec" );
                    ((ChannelExec)channel).setCommand( "echo" );
                    
                    long pingStart = System.currentTimeMillis();
                    channel.connect( 3000 );
                    channel.disconnect();
                    long pingStop = System.currentTimeMillis();
                    
                    for( PingDaemonListener listener : listeners ) {
                        
                        listener.PingUpdate( tunnel, pingStop - pingStart );
                    }

                    failCount = 0;
                    
                } catch ( JSchException e ) {
                    
                    logger.error( "Exception while measuring ping: " + e.getMessage() );
                    logger.debug( "PING FAIL (ERROR) ( " + failCount + "/3 ): " + tunnel );
                    failCount++;
                }

            } else {
                
                logger.debug( "PING FAIL (DISCONNECT) ( " + failCount + "/3 ): " + tunnel );
                failCount++;
            }
            
            if ( failCount > 0 ) {

                for( PingDaemonListener listener : listeners ) {
                    
                    listener.PingUpdate( tunnel, -failCount );
                }                
            }

            if ( failCount > 3 ) {
                
                for( PingDaemonListener listener : listeners ) {
                    listener.PingFailed( this, tunnel, session );
                }
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
