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
    private String          id;
    private boolean         running         = true;
    
    public PingChecker( TunnelManager tunnelManager, Tunnel tunnel ) {
        
        if ( tunnel.getPingChecker() != null ) {
            logger.info( "Restarting ping checker for tunnel "  +tunnel.getAlias() );
            tunnel.getPingChecker().stop();
        }
        
        this.tunnelManager  = tunnelManager;
        this.tunnel         = tunnel;
        this.id             = tunnel.getAlias();
        
        this.tunnel.setPingChecker( this );
        this.tunnel.setPingFailures( 0 );
    }
    
    public void stop() {
        
        this.running = false;
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
            
            this.tunnel.increasePingFailures();
            logger.debug( "PING FAIL (ERROR) ( " + tunnel.getPingFailures() + "/3 ): " + this.tunnel.getAlias() );
        }
        
        this.tunnelManager.updatePing( this.tunnel );
    }
    
    
    @Override
    public void run() {
        
        logger.info( "Started ping daemon[" + this + "] for tunnel " + this.id );
        
        while( this.running && this.tunnel.getState() == Tunnel.STATE_RUNNING && this.tunnel.getSession() != null && this.tunnel.getPingFailures() < 3 ) {
            
            this.checkPing();
                
            logger.trace( "PingDaemon sleeping for 2 seconds" );
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                logger.warn( "Sleep interrupted" );
            }
        }
        
        if ( this.tunnel.getPingChecker() != null && this.tunnel.getPingChecker() == this ) {
            this.tunnel.setPingChecker( null );
        }
        
        logger.info( "Stopped ping daemon[ " + this + "] for tunnel " + this.id );
    }
}
