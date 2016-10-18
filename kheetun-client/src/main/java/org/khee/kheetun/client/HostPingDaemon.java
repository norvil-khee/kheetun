package org.khee.kheetun.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;

public class HostPingDaemon implements Runnable, ConfigManagerListener {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );

    private Config config;
    private Thread thread;
    private boolean running = false;
    private static HostPingDaemon instance = null;
    
    protected HostPingDaemon() {
        
        ConfigManager.addConfigManagerListener( this );
    }
    
    public static void init() {
        
        instance = new HostPingDaemon();
    }
    
    private void setConfig( Config config ) {
        
        instance.config = config;
        instance.running = false;
        
        try {
            
            if ( instance.thread != null && instance.thread.isAlive() ) {
                instance.thread.join();
            }
            
            instance.thread = new Thread( instance, "kheetun-hostping-thread" );
            instance.running = true;
            instance.thread.start();
        } catch ( InterruptedException e ) {
            
            logger.error( "Failed to (re)start host ping thread: " + e.getMessage() );
        }
        
    }
    
    @Override
    public void configManagerConfigChanged( Config config ) {
    }
    
    @Override
    public void configManagerConfigInvalid(Config config, ArrayList<String> errorStack) {
    }
    
    @Override
    public void configManagerConfigValid(Config config) {
        
        this.setConfig( config );
    }
    
    @Override
    public void run() {
        
        while( running ) {
            
            for ( Profile profile : config.getProfiles() ) {
                
                if ( ! running || ! TunnelManager.isConnected() ) {
                    break;
                }
                
                for ( Tunnel tunnel : profile.getTunnels() ) {
                    
                    if ( ! running || ! TunnelManager.isConnected()  ) {
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
