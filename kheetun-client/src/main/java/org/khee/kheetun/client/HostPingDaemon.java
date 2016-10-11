package org.khee.kheetun.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;

public class HostPingDaemon implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );

    private Config config;
    private Thread thread;
    private boolean running = false;
    private static HostPingDaemon instance = null;
    
    protected HostPingDaemon() {
        
        this.thread = new Thread( this );
    }
    
    public static void setConfig( Config config ) {
        
        if ( instance == null ) {
            instance = new HostPingDaemon();
        }

        instance.config = config;
        instance.running = false;
        
        try {
            
            if ( instance.thread.isAlive() ) {
                instance.thread.join();
            }
            instance.running = true;
            instance.thread.start();
        } catch ( InterruptedException e ) {
            
            logger.error( "Failed to (re)start host ping thread: " + e.getMessage() );
        }
        
    }
    
    @Override
    public void run() {
        
        while( running ) {
            
            for ( Profile profile : config.getProfiles() ) {
                
                if ( ! running ) {
                    break;
                }
                
                for ( Tunnel tunnel : profile.getTunnels() ) {
                    
                    if ( ! running ) {
                        break;
                    }
                    
                    if ( ! tunnel.getAutostart() || TunnelManager.isRunning( tunnel ) ) {
                        continue;
                    }
                    
                    try {
                        
                        // try an SSH connect
                        // TODO: make port configureable
                        //
                        Socket socket = new Socket( tunnel.getHostname(), 22 );
                        
                        if ( socket.isConnected() ) {
                            
                            logger.debug( "Host " + tunnel.getHostname() + " is reachable" );
                            
                            TunnelManager.autostartHostAvailable( tunnel );
                        } else {
                            
                            logger.trace( "Host " + tunnel.getHostname() + " is not reachable" );
                            
                            TunnelManager.autostartHostUnavailable( tunnel );
                        }
                        
                        socket.close();
                        
                    } catch ( UnknownHostException e ) {
                        
                        logger.trace( "Host " + tunnel.getHostname() + " is not reachable (unknown host)" );
                        TunnelManager.autostartHostUnavailable( tunnel );
                        
                    } catch ( IOException eIO ) {
                        
                        logger.trace( "Host " + tunnel.getHostname() + " is not reachable (IO error: " + eIO.getMessage() + ")" );
                        TunnelManager.autostartHostUnavailable( tunnel );
                    }
                }
            }
            
            try {
                Thread.sleep( 1000 );
            } catch ( InterruptedException e ) {
                logger.error( "Error while trying thread sleep in host ping daemon: " + e.getMessage() );
            }
        }
    }
}
