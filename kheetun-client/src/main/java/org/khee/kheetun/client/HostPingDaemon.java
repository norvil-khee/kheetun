package org.khee.kheetun.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.GlobalConfig;
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
        
        logger.info( "Start host ping due to config change" );
        
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
    public void configManagerGlobalConfigChanged(GlobalConfig oldConfig,
            GlobalConfig newConfig, boolean valid) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void configManagerConfigChanged( Config oldConfig, Config newConfig, boolean valid ) {
        
        if ( valid ) {

            this.setConfig( newConfig );
        }
    }
    
    @Override
    public void run() {
        
        while( running ) {
            
            for ( Profile profile : config.getProfiles() ) {
                
                if ( ! profile.isActive() ) {
                    continue;
                }
                
                if ( ! running || ! TunnelManager.isConnected() ) {
                    logger.trace( "Break requested or TunnelManager is not connected, breaking host ping for profile " + profile.getName() );
                    break;
                }
                
                if ( ! profile.getErrors().isEmpty() ) {
                    logger.trace( "Skipping host pings for profile " + profile.getName() + " because it has errors" );
                    continue;
                }
                
                for ( Tunnel tunnel : profile.getTunnels() ) {
                    
                    if ( ! running || ! TunnelManager.isConnected()  ) {
                        logger.trace( "Break requested or TunnelManager is not connected, breaking host ping" );
                        break;
                    }
                    
                    if ( TunnelManager.isRunning( tunnel ) ) {
                        
                        logger.trace( "Tunnel " + tunnel.getAlias() + " is running -> no host ping required" );
                        continue;
                    }

                    if ( ! tunnel.getAutostart() && ! tunnel.getRestart() ) {

                        logger.trace( "Tunnel " + tunnel.getAlias() + " has neither auto- nor restart set -> no host ping required" );
                        continue;
                    }
                    
                    try {
                        
                        // try an SSH connect
                        // TODO: make port configureable
                        //
                        logger.trace( "Try SSH connect to " + tunnel.getHostname() );
                        Socket socket = new Socket();
                        socket.connect( new InetSocketAddress( tunnel.getHostname(), 22 ), 2000 ); 
                        
                        if ( socket.isConnected() ) {
                            
                            logger.debug( "Host " + tunnel.getHostname() + " is reachable" );
                            
                            tunnel.setInfo( null );
                            TunnelManager.autostartHostAvailable( tunnel );
                        } else {
                            
                            logger.trace( "Host " + tunnel.getHostname() + " is not reachable" );
                            
                            tunnel.setInfo( tunnel.getHostname() + ": not reachable" );
                            TunnelManager.autostartHostUnavailable( tunnel );
                        }
                        
                        socket.close();
                        
                    } catch ( UnknownHostException e ) {
                        
                        logger.trace( "Host " + tunnel.getHostname() + " is not reachable (unknown host)" );
                        tunnel.setInfo( tunnel.getHostname() + ": unknown host" );
                        TunnelManager.autostartHostUnavailable( tunnel );
                        
                    } catch ( IOException eIO ) {
                        
                        logger.trace( "Host " + tunnel.getHostname() + " is not reachable (IO error: " + eIO.getMessage() + ")" );
                        tunnel.setInfo( tunnel.getHostname() + ": IO error: " + eIO.getLocalizedMessage() );
                        TunnelManager.autostartHostUnavailable( tunnel );
                    }
                }
            }
            
            logger.trace( "Host ping sleep 1 second" );
            try {
                Thread.sleep( 1000 );
            } catch ( InterruptedException e ) {
                logger.error( "Error while trying thread sleep in host ping daemon: " + e.getMessage() );
            }
        }
        
        logger.trace( "Host ping thread ended" );
    }
}
