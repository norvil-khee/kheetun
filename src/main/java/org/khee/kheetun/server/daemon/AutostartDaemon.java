package org.khee.kheetun.server.daemon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.server.manager.TunnelManager;

public class AutostartDaemon implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    
    private TunnelManager                   tunnelManager;
    private Tunnel                          tunnel;
    private String                          id;
    private boolean                         running         = true;
    
    public AutostartDaemon( TunnelManager tunnelManager, Tunnel tunnel ) {
        
        if ( tunnel.getAutostartDaemon() != null ) {
            logger.info( "Restarting autostart daemon for tunnel "  +tunnel.getAlias() );
            tunnel.getAutostartDaemon().stop();
        }

        this.tunnel         = tunnel;
        this.tunnelManager  = tunnelManager;
        this.id             = tunnel.getAlias();
        
        this.tunnel.setAutostartDaemon( this );
    }
    
    public void stop() {
        
        this.running = false;
    }
    
    public void start() {
        
        Thread daemon = new Thread( this, "kheetund-autostart-daemon-" + this.tunnel.getAlias().toLowerCase() );
        daemon.start();
    }
    
    public void checkAutostart() {
        
        try {
            
            // try an SSH connect
            //
            logger.trace( "Try SSH connect to " + tunnel.getHostname() );
            Socket socket = new Socket();
            socket.connect( new InetSocketAddress( tunnel.getHostname(), tunnel.getPort() ), 2000 ); 
            
            if ( socket.isConnected() ) {
                
                logger.debug( "Host " + tunnel.getHostname() + " is reachable" );
                tunnel.setInfo( null );
                tunnel.setAutoState( Tunnel.STATE_AUTO_AVAIL );
                
            } else {
                
                logger.trace( "Host " + tunnel.getHostname() + " is not reachable" );
                
                tunnel.setAutoState( Tunnel.STATE_AUTO_WAIT );
                tunnel.setInfo( tunnel.getHostname() + ": not reachable" );
            }
            
            socket.close();
            
        } catch ( UnknownHostException e ) {
            
            logger.trace( "Host " + tunnel.getHostname() + " is not reachable (unknown host)" );

            tunnel.setAutoState( Tunnel.STATE_AUTO_WAIT );
            tunnel.setInfo( tunnel.getHostname() + ": unknown host" );
            
        } catch ( IOException eIO ) {
            
            logger.trace( "Host " + tunnel.getHostname() + " is not reachable (IO error: " + eIO.getMessage() + ")" );
            tunnel.setAutoState( Tunnel.STATE_AUTO_WAIT );
            tunnel.setInfo( tunnel.getHostname() + ": IO error: " + eIO.getLocalizedMessage() );
        
        }
        
        this.tunnelManager.updateAutostart( this.tunnel );
    }
    
    @Override
    public void run() {
        
        logger.info( "Started autostart daemon for tunnel " + this.id );
        
        while( this.running && this.tunnel.getAutoState() != Tunnel.STATE_AUTO_OFF && this.tunnel.getState() != Tunnel.STATE_RUNNING ) {
            
            this.checkAutostart();

            logger.trace( "AutostartDaemon sleeping for 2 seconds" );
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                logger.warn( "Sleep interrupted" );
            }
        }
        
        if ( this.tunnel.getAutostartDaemon() != null && this.tunnel.getAutostartDaemon() == this ) {
            this.tunnel.setAutostartDaemon( null );
        }
        
        logger.info( "Stopped autostart daemon for tunnel " + this.id );
    }
}
