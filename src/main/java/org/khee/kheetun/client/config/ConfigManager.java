package org.khee.kheetun.client.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.TunnelClient;

public class ConfigManager implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );

    private static ConfigManager instance = null;
    
    private Config              config              = null;
    private GlobalConfig        globalConfig        = new GlobalConfig();
    private String              fingerprint         = "";
    private String              fingerprintGlobal   = "";
    private File                configDirectory     = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.d" );
    private File                globalConfigFile    = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.conf" );

    
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
    
    public static Config getConfig() {
        
        return instance.config;
    }
    
    public static GlobalConfig getGlobalConfig() {
        
        return instance.globalConfig;
    }
    
    private boolean globalConfigChanged() {
    
        String fingerprint = "";
        
        if ( globalConfigFile.exists() ) {
            
            fingerprint += globalConfigFile.lastModified();
        }
        
        logger.debug( "ConfigManager: fingerprintGlobal ( " + fingerprint + "/" + this.fingerprintGlobal + ")" );
            
        if ( this.fingerprintGlobal.equals( fingerprint ) ) {
            return false;
        }
        
        this.fingerprintGlobal = fingerprint;

        return true;
    }
  
    
    private boolean configChanged() {
        
        String fingerprint = "";
        
        if ( configDirectory.exists() ) {
            
            File[] files = configDirectory.listFiles();
            
            Arrays.sort( files );
           
            for ( File file : files ) {
                
                if ( ! file.getAbsolutePath().matches( ".*\\.xml$" ) ) {
                    continue;
                }
                
                fingerprint += file.getAbsolutePath() + file.lastModified();
            }
        }
            
        logger.debug( "ConfigManager: fingerprint ( " + fingerprint + "/" + this.fingerprint + ")" );
            
        if ( this.fingerprint.equals( fingerprint ) ) {
            return false;
        }
        
        this.fingerprint = fingerprint;

        return true;
    }
    
    @Override
    public void run() {
        
        while( true ) {
            
            if ( this.globalConfigChanged() ) {
                
                GlobalConfig oldConfig = this.globalConfig;

                logger.info( "Global configuration changed, updating" );
                
                this.globalConfig = GlobalConfig.load();
                
                for ( ConfigManagerListener listener : this.listeners ) {
                    listener.configManagerGlobalConfigChanged( oldConfig, this.globalConfig, this.globalConfig.getErrors().isEmpty() );
                }
            }
            
            if ( TunnelClient.isConnected() ) {
        
                if ( this.configChanged() ) {
                    
                    Config oldConfig = this.config;
                    
                    logger.info( "Configuration directory changed" );
                   
                    this.config = Config.load();
                    
                    for ( ConfigManagerListener listener : this.listeners ) {
                        listener.configManagerConfigChanged( oldConfig, this.config, this.config.getErrors().isEmpty() );
                    }
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
