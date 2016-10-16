package org.khee.kheetun.client.config;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.TunnelManager;

public class ConfigManager implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );

    private static ConfigManager instance = null;
    
    private Config  config;
    private Config  backup;
    private long    lastModified = -1;
    
    private ArrayList<ConfigManagerListener> listeners  = new ArrayList<ConfigManagerListener>();
    
    protected ConfigManager() {
    }
    
    public static void init() {
        instance = new ConfigManager();
    }
    
    public static void start() {
        
        Thread configWatcher = new Thread( instance, "kheetun-configwatcher-thread" );
        configWatcher.start();
    }
    
    private void stopStaleTunnels( Config newConfig ) {
        
        if ( config == null ) {
            return;
        }
        
        for ( Profile oldProfile : config.getProfiles() ) {
            
            for ( Tunnel oldTunnel : oldProfile.getTunnels() ) {
                
                if ( ! TunnelManager.isRunning( oldTunnel.getSignature() ) ) {
                    continue;
                }
                
                boolean stale = true;
                
                for ( Profile newProfile : newConfig.getProfiles() ) {
                    
                    for ( Tunnel newTunnel : newProfile.getTunnels() ) {
                        
                        if ( newTunnel.getSignature().equals( oldTunnel.getSignature() ) ) {
                            
                            stale = false;
                            break;
                        }
                    }
                    
                    if ( ! stale ) {
                        break;
                    }
                }
                
                if ( stale ) {
                    logger.info( "Stopping stale tunnel after config change: " + oldTunnel.getAlias() );
                    TunnelManager.stopTunnel( oldTunnel );
                }
            }
        }
    }
    
    @Override
    public void run() {
        
        File f = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.xml" );
        
        while( true ) {
        
            if ( f.lastModified() != this.lastModified && f.exists() ) {
                
                logger.info( "Configuration changed, updating" );
                
                this.lastModified = f.lastModified();
                
                try {
                    
                    Config newConfig = Config.load( f );
                    
                    this.stopStaleTunnels( newConfig );
                    
                    for ( ConfigManagerListener listener : this.listeners ) {
                        listener.configManagerConfigChanged( newConfig );
                    }
                    
                    backup = config;
                    config = newConfig;
                    
                } catch ( JAXBException e ) {
                    
                    logger.error( "Error while loading configuration: " + e.getMessage() );
                }
            }
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                logger.error( "Interrupted while sleeping in ConfigManager watcher" );
            }
        }
    }
    
    public static void addConfigManagerListener( ConfigManagerListener listener ) {
        
        instance.listeners.add( listener );
    }
    

}
