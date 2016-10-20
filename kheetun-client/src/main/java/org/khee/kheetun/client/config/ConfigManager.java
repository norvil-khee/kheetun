package org.khee.kheetun.client.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.TunnelManager;
import org.khee.kheetun.client.verify.VerifierFactory;

public class ConfigManager implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );

    private static ConfigManager instance = null;
    
    private Config              config;
    private String              fingerprint     = "";
    private ArrayList<String>   errorStack      = new ArrayList<String>();
    private File                configDirectory = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.d" );

    
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
                    TunnelManager.setStale( oldTunnel );
                }
            }
        }
    }
    
    private boolean configChanged() {
        
        boolean deprecatedConfigExists = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.xml" ).exists();
        
        if ( deprecatedConfigExists ) {
            return true;
        }
        
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
        
            if ( this.configChanged() ) {
                
                logger.info( "Configuration changed, updating" );
               
                Config newConfig = new Config();
                
                try {
                    
                    newConfig = Config.load();
                    
                } catch ( JAXBException eJAX ) {
                    
                    String error = ( eJAX.getCause() != null ? eJAX.getCause().getLocalizedMessage() : eJAX.getLocalizedMessage() );
                    
                    this.errorStack.add( "Configuration XML: " + error );
                    
                    logger.error( "Error while loading configuration: " + error );
                    
                    for ( ConfigManagerListener listener : this.listeners ) {
                        
                        listener.configManagerConfigInvalid( null, this.errorStack );
                    }
                    
                } catch ( Exception e ) {
                    
                    this.errorStack.add( "Configuration: " + e.getLocalizedMessage() );
                    
                    logger.error( "Error while loading configuration: " + e.getLocalizedMessage() );
                    logger.debug( "", e );

                    for ( ConfigManagerListener listener : this.listeners ) {
                        
                        listener.configManagerConfigInvalid( null, this.errorStack );
                    }
                }
                
                this.stopStaleTunnels( newConfig );
                
                if ( ! this.validate( newConfig ) ) {
                    
                    for ( String error : this.errorStack ) {
                        
                        logger.error( "Config error: " + error );
                    }
                    
                    for ( ConfigManagerListener listener : this.listeners ) {
                        
                        listener.configManagerConfigInvalid( newConfig, this.errorStack );
                    }
                    
                    newConfig = new Config();
                    
                } else {
                    
                    for ( ConfigManagerListener listener : this.listeners ) {
                        
                        listener.configManagerConfigValid( newConfig );
                    }
                }
                
                for ( ConfigManagerListener listener : this.listeners ) {
                    listener.configManagerConfigChanged( newConfig );
                }
                
                config = newConfig;
                
            }
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                logger.error( "Interrupted while sleeping in ConfigManager watcher" );
            }
        }
    }
    
    public synchronized boolean validate( Config config ) {
        
        errorStack.clear();
        ArrayList<String> binds = new ArrayList<String>();
        
        for ( Profile profile : config.getProfiles() ) {
            
            for ( Tunnel tunnel : profile.getTunnels() ) {
                
                binds.clear();
                
                if ( ! VerifierFactory.getAliasVerifier().verify( tunnel.getAlias() ) ) {
                    
                    errorStack.add( "Tunnel '" + tunnel.getAlias() +"': invalid alias '" + tunnel.getAlias() + "'" );
                }
                
                if ( ! VerifierFactory.getHostnameVerifier().verify( tunnel.getHostname() ) ) {
                    
                    errorStack.add( "Tunnel '" + tunnel.getAlias() +"': invalid hostname '" + tunnel.getHostname() + "'" );
                }
                
                if ( tunnel.getSshKey() != null ) {
                    if ( ! VerifierFactory.getSshKeyVerifier().verify( tunnel.getSshKey().getAbsolutePath() ) ) {
                        
                        errorStack.add( "Tunnel '" + tunnel.getAlias() +"': invalid SSH key '" + tunnel.getSshKey().getAbsolutePath() + "'" );
                    }
                }
                
                if ( ! VerifierFactory.getUserVerifier().verify( tunnel.getUser() ) ) {
                    
                    errorStack.add( "Tunnel '" + tunnel.getAlias() +"': invalid user '" + tunnel.getUser() + "'" );
                }
                
                int f = 0;
                
                for ( Forward forward : tunnel.getForwards() ) {
                    
                    f++;
                    
                    if ( ! VerifierFactory.getPortVerifier().verify( forward.getBindPort() ) ) {
                        errorStack.add( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid bind port '" + forward.getBindPort() + "'" );
                    }

                    if ( ! VerifierFactory.getPortVerifier().verify( forward.getForwardedPort() ) ) {
                        errorStack.add( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid forwarded port '" + forward.getBindPort() + "'" );
                    }
                    
                    if ( ! VerifierFactory.getHostnameVerifier().verify( forward.getForwardedHost() ) ) {
                        errorStack.add( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid forwarded host '" + forward.getForwardedHost() + "'" );
                    }

                    if ( binds.contains( forward.getBindIp() + ":" + forward.getBindPort() ) ) {
                        
                        errorStack.add( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": duplicate bind IP '" + forward.getBindIp() + "'" );
                        
                    } else {

                        binds.add( forward.getBindIp() + ":" + forward.getBindPort() );
                    }
                    
                    if ( ! VerifierFactory.getIpAddressVerifier().verify( forward.getBindIp() ) ) {
                        
                        errorStack.add( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid bind IP '" + forward.getBindIp() + "'" );
                    }
                }
            }
        }
        
        return errorStack.isEmpty();
    }
    
    public static void addConfigManagerListener( ConfigManagerListener listener ) {
        
        instance.listeners.add( listener );
    }
    

}
