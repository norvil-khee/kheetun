package org.khee.kheetun.server.daemon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.server.manager.TunnelManager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

public class PingChecker implements Runnable {
    
    private static Logger   logger = LogManager.getLogger( "kheetund" );
    
    private Tunnel          tunnel;
    private TunnelManager   tunnelManager;
    
    /**
     * ping live sessions
     * ping autostarting dead sessions 
     */
    public PingChecker( TunnelManager tunnelManager, Tunnel tunnel ) {

        this.tunnelManager  = tunnelManager;
        this.tunnel         = tunnel;
        
        this.tunnel.setPingChecker( this );
    }
    
    public void start() {
        
        Thread daemon = new Thread( this, "kheetund-ping-checker-" + this.tunnel.getAlias().toLowerCase() );
        daemon.start();
    }
    
    private void checkPing() {
        
        logger.debug( "Measuring ping of connected tunnel " + tunnel.getAlias() );
        
        try {
            
            Channel channel = this.tunnel.getSession().openChannel( "exec" );
            ((ChannelExec)channel).setCommand( "echo" );
            
            long pingStart = System.currentTimeMillis();
            channel.connect( 3000 );
            long pingStop = System.currentTimeMillis();
            channel.disconnect();
            
            this.tunnel.setPingFailures( 0 );
            this.tunnel.setPing( (int)(pingStop - pingStart) );
            logger.debug( "Ping of tunnel " + tunnel.getAlias() + " = " + this.tunnel.getPing() );
           
        } catch ( JSchException e ) {
            
            this.tunnel.setPingFailures( tunnel.getPingFailures() + 1 );
            logger.debug( "PING FAIL (ERROR) ( " + tunnel.getPingFailures() + "/3 ): " + this.tunnel.getAlias() );
        }
        
        this.tunnelManager.updatePing( this.tunnel );
    }
    
    
    @Override
    public void run() {
        
        logger.info( "Started ping daemon for tunnel " + this.tunnel.getAlias() );
        
        while( this.tunnel.getState() == Tunnel.STATE_RUNNING && this.tunnel.getSession() != null ) {
            
            this.checkPing();
                
            logger.trace( "PingDaemon sleeping for 2 seconds" );
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }

        }
        
        this.tunnel.setPingChecker( null );
        
        logger.info( "Stopped ping daemon for tunnel " + this.tunnel.getAlias() );
    }
}
