package org.khee.kheetun.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
    private ArrayList<HostPingDaemonListener> listeners = new ArrayList<HostPingDaemonListener>();
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
                        
                        // disable DNS caching
                        //
                        java.security.Security.setProperty( "networkaddress.cache.ttl", "0" );
                        java.security.Security.setProperty( "networkaddress.cache.negative.ttl", "0" );
                        
                        InetAddress address = InetAddress.getByName( tunnel.getHostname() );
                        
                        if ( address.isReachable( 2000 ) ) {
                            
                            logger.debug( "Host " + tunnel.getHostname() + " is reachable ( timeout = " + java.security.Security.getProperty( "networkaddress.cache.ttl" ) + " )" );
                            for ( HostPingDaemonListener listener : listeners ) {
                                listener.hostReachable( tunnel );
                            }
                        } else {
                            
                            logger.debug( "Host " + tunnel.getHostname() + " is not reachable" );
                        }
                        
                    } catch ( UnknownHostException e ) {
                        
                        
                    } catch ( IOException eIO ) {
                        
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
    
    public static void addHostPingDaemonListener( HostPingDaemonListener listener ) {
        
        if ( instance == null ) {
            
            instance = new HostPingDaemon();
        }
        
        instance.listeners.add( listener );
    }
    
}
