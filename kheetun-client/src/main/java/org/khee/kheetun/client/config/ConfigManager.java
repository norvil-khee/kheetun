package org.khee.kheetun.client.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.TunnelManager;
import org.khee.kheetun.client.verify.VerifierFactory;

public class ConfigManager implements Runnable {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );

    private static ConfigManager instance = null;
    
    private Config              config              = new Config();
    private GlobalConfig        globalConfig        = new GlobalConfig();
    private String              fingerprint         = "";
    private String              fingerprintGlobal   = "";
    private ArrayList<Tunnel>   tunnels             = new ArrayList<Tunnel>();
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
    
    private void stopStaleTunnels( Config oldConfig ) {
        
        if ( oldConfig == null ) {
            return;
        }
        
        for ( Profile oldProfile : oldConfig.getProfiles() ) {
            
            for ( Tunnel oldTunnel : oldProfile.getTunnels() ) {
                
                if ( ! TunnelManager.isRunning( oldTunnel ) ) {
                    continue;
                }
                
                boolean stale = true;
                
                for ( Profile newProfile : this.config.getProfiles() ) {
                    
                    if ( newProfile.isActive() ) {
                    
                        for ( Tunnel newTunnel : newProfile.getTunnels() ) {
                            
                            if ( newTunnel.equals( oldTunnel ) ) {
                                
                                stale = false;
                                break;
                            }
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
        
            if ( this.configChanged() ) {
                
                Config oldConfig = this.config;
                
                logger.info( "Configuration changed, updating" );
               
                this.config = Config.load();
                
                tunnels.clear();
                
                for ( Profile profile : this.config.getProfiles() ) {
                    
                    if ( ! profile.isActive() ) {
                        continue;
                    }
                    
                    for ( Tunnel tunnel : profile.getTunnels() ) {
                        tunnels.add( tunnel );
                    }
                }
                    
                this.stopStaleTunnels( oldConfig );
                
                for ( Profile profile : this.config.getProfiles() ) {
                    
                    this.validate( profile );
                }
                
                for ( ConfigManagerListener listener : this.listeners ) {
                    listener.configManagerConfigChanged( oldConfig, this.config, this.config.getErrors().isEmpty() );
                }
            }
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                logger.error( "Interrupted while sleeping in ConfigManager watcher" );
            }
        }
    }
    
    public synchronized boolean validate( Profile profile ) {
        
        ArrayList<String> binds = new ArrayList<String>();
        
        for ( Tunnel tunnel : profile.getTunnels() ) {
            
            binds.clear();
            
            if ( ! VerifierFactory.getAliasVerifier().verify( tunnel.getAlias() ) ) {
                
                profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid alias '" + tunnel.getAlias() + "'" );
            }
            
            if ( ! VerifierFactory.getHostnameVerifier().verify( tunnel.getHostname() ) ) {
                
                profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid hostname '" + tunnel.getHostname() + "'" );
            }
            
            if ( tunnel.getSshKey() != null ) {
                if ( ! VerifierFactory.getSshKeyVerifier().verify( tunnel.getSshKey().getAbsolutePath() ) ) {
                    
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid SSH key '" + tunnel.getSshKey().getAbsolutePath() + "'" );
                }
            }
            
            if ( ! VerifierFactory.getUserVerifier().verify( tunnel.getUser() ) ) {
                
                profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid user '" + tunnel.getUser() + "'" );
            }
            
            int f = 0;
            
            for ( Forward forward : tunnel.getForwards() ) {
                
                f++;
                
                if ( ! VerifierFactory.getPortVerifier().verify( forward.getBindPort() ) ) {
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid bind port '" + forward.getBindPort() + "'" );
                }

                if ( ! VerifierFactory.getPortVerifier().verify( forward.getForwardedPort() ) ) {
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid forwarded port '" + forward.getBindPort() + "'" );
                }
                
                if ( ! VerifierFactory.getHostnameVerifier().verify( forward.getForwardedHost() ) ) {
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid forwarded host '" + forward.getForwardedHost() + "'" );
                }

                if ( binds.contains( forward.getBindIp() + ":" + forward.getBindPort() ) ) {
                    
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": duplicate bind IP '" + forward.getBindIp() + "'" );
                    
                } else {

                    binds.add( forward.getBindIp() + ":" + forward.getBindPort() );
                }
                
                if ( ! VerifierFactory.getIpAddressVerifier().verify( forward.getBindIp() ) ) {
                    
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid bind IP '" + forward.getBindIp() + "'" );
                }
            }
        }
        
        return profile.getErrors().isEmpty();
    }
    
    public static Tunnel getTunnel( Tunnel tunnel ) {
        
        if ( instance.tunnels.contains( tunnel ) ) {
            return instance.tunnels.get( instance.tunnels.indexOf( tunnel ) );
        }
        
        return null;
    }
    
    public static ArrayList<Tunnel> getTunnels() {
        
        return instance.tunnels;
    }
    
    public static void addConfigManagerListener( ConfigManagerListener listener ) {
        
        instance.listeners.add( listener );
    }
    

}
