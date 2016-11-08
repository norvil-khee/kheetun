package org.khee.kheetun.client;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.GlobalConfig;
import org.khee.kheetun.client.config.Tunnel;

public class TunnelManager implements ConfigManagerListener {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private static TunnelManager                instance            = null;
    
    private ArrayList<Tunnel>                   activating          = new ArrayList<Tunnel>();
    private ArrayList<Tunnel>                   deactivating        = new ArrayList<Tunnel>();
    private ArrayList<Tunnel>                   running             = new ArrayList<Tunnel>();   
    private ArrayList<Tunnel>                   ignoreAutostart     = new ArrayList<Tunnel>();
    private ArrayList<Tunnel>                   stale               = new ArrayList<Tunnel>();
    private ArrayList<Tunnel>                   queued              = new ArrayList<Tunnel>();
    private ArrayList<TunnelManagerListener>    listeners           = new ArrayList<TunnelManagerListener>();
    
    private boolean                             connected           = false;
    
    protected TunnelManager() {
        
        ConfigManager.addConfigManagerListener( this );
    }
    
    public static void init() {
        
        instance = new TunnelManager();
    }
    
    public synchronized static void startTunnel( Tunnel tunnel ) {
        
        logger.info( "A tunnel is requested to start: " + tunnel );
        
        if ( ! instance.connected ) {
            return;
        }
        
        if ( ! instance.running.contains( tunnel ) && ! instance.activating.contains( tunnel ) ) {
            
            logger.info( "Starting tunnel: " + tunnel.getAlias() );
            logger.debug( tunnel );
            
            if ( instance.deactivating.contains( tunnel ) ) {
                instance.deactivating.remove( tunnel );
            }
            
            instance.activating.add( tunnel );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelActivating( tunnel );
            }
            
            if ( ! instance.stale.isEmpty() ) {
                
                logger.info( "Queueing start of tunnel " + tunnel.getAlias() + " as there are still running stale tunnels on daemon" );
                instance.queued.add( tunnel );
            } else {
                
                TunnelClient.sendStartTunnel( tunnel );
            }
        }
    }
    
    public synchronized static void stopTunnel( Tunnel tunnel ) {
        
        logger.info( "A tunnel is requested to stop: " + tunnel );

        if ( ! instance.connected ) {
            return;
        }
        
        if ( instance.running.contains( tunnel ) && ! instance.deactivating.contains( tunnel ) ) {
            
            logger.info( "Stopping tunnel: " + tunnel.getAlias() );
            logger.debug( tunnel );
            
            if ( instance.activating.contains( tunnel ) ) {
                instance.activating.remove( tunnel );
            }
            
            instance.deactivating.add( tunnel );
            
            if ( tunnel.getRestart() ) {
                logger.info( "Deactivating restart of tunnel " + tunnel.getAlias() );
                tunnel.setRestart( false );
            } else {
                logger.info( "Well there is no restart set for this tunnel: " + tunnel.getAlias() );
            }
            
            TunnelClient.sendStopTunnel( tunnel );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelDeactivating( tunnel );
            }
            
        }
    }
    
    public static void stopAllTunnels() {
        
        ArrayList<Tunnel> runningTunnels = new ArrayList<Tunnel>( instance.running );
        
        for ( Tunnel tunnel : runningTunnels ) {
            
            TunnelManager.disableAutostart( tunnel );
            TunnelManager.stopTunnel( tunnel );
        }
    }
    
    public static void setStale( Tunnel tunnel ) {
        
        if ( ! instance.stale.contains( tunnel ) ) {
            
            logger.info( "Marking tunnel " + tunnel.getAlias() + " as stale" );
            instance.stale.add( tunnel );
        }
    }
    
    public static void raiseError( Tunnel tunnel, String error ) {
        
        if ( tunnel != null ) {
            
            if ( instance.activating.contains( tunnel ) ) {
                logger.debug( "Remove activating tunnel because of error" );
                instance.activating.remove( tunnel );
            }
            
            if ( instance.deactivating.contains( tunnel ) ) {
                logger.debug( "Remove deactivating tunnel because of error" );
                instance.deactivating.remove( tunnel );
            }
            
            TunnelManager.stopped( tunnel );

            tunnel.setFailures( tunnel.getFailures() + 1 );
            logger.info( "Tunnel " + tunnel.getAlias() + " failures: " + tunnel.getFailures() + "/" + tunnel.getMaxFailures() );
            
            if ( tunnel.getFailures() >= tunnel.getMaxFailures() ) {
                
                error += " (gave up after " + tunnel.getMaxFailures() + " attempts)"; 
                        
                tunnel.setRestart( false );
                TunnelManager.disableAutostart( tunnel );
            }

            for ( TunnelManagerListener listener : instance.listeners ) {
                logger.debug( "Notifying " + listener );
                listener.tunnelManagerTunnelError( tunnel, error );
            }
        }
    }
    
    public synchronized static void enableAutostart( Tunnel tunnel ) {
        
        if ( instance.ignoreAutostart.contains( tunnel ) ) {
            
            instance.ignoreAutostart.remove( tunnel ); 

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartEnabled( tunnel );
            }
        }
    }
    
    public synchronized static void enableAutostartAll() {
        
        ArrayList<Tunnel> tunnels = new ArrayList<Tunnel>( instance.ignoreAutostart );
        
        for ( Tunnel tunnel : tunnels ) {
            TunnelManager.enableAutostart( tunnel );
        }
    }
    
    public synchronized static void disableAutostart( Tunnel tunnel ) {
        
        if ( ! tunnel.getAutostart() ) {
            return;
        }
        
        logger.info( "Disable autostart temporary for tunnel " + tunnel.getAlias() );
        
        if ( ! instance.ignoreAutostart.contains( tunnel ) ) {
            
            instance.ignoreAutostart.add( tunnel ); 

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartDisabled( tunnel );
            }
        } else {
            
            logger.warn( "Autostart already temporarily disabled for tunnel " + tunnel.getAlias() );
        }
    }
    
    public synchronized static void resetAllFailures() {
        
        for ( Tunnel tunnel : ConfigManager.getTunnels() ) {
            tunnel.setFailures( 0 );
        }
    }
       
    public static void quit() {
        
        if ( instance.connected ) {
            
            TunnelClient.sendQuit();
        } else {
            
            logger.info( "Bye!" );
            System.exit( 0 );
        }
    }
    
    public static void started( Tunnel tunnel ) {
        
        logger.info( "A tunnel started: " + tunnel );
        
        if ( ! instance.running.contains( tunnel ) ) {
            
            if ( instance.activating.contains( tunnel ) ) {
                instance.activating.remove( tunnel );
            }
            
            if ( instance.queued.contains( tunnel ) ) {
                instance.queued.remove( tunnel );
            }
            
            tunnel.setRestart( true );
            tunnel.setFailures( 0 );
            instance.running.add( tunnel );
            
            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelActivated( tunnel );
            }
        }        
    }
    
    
    public static void stopped( Tunnel tunnel ) {
        
        logger.info( "A tunnel stopped: " + tunnel );
        
        if ( instance.running.contains( tunnel ) ) {
            
            if ( instance.deactivating.contains( tunnel ) ) {
                instance.deactivating.remove( tunnel );
            }
            
            instance.running.remove( tunnel );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelDeactivated( tunnel );
            }
            
            if ( instance.stale.contains( tunnel ) ) {
                
                logger.info( "Stale tunnel " + tunnel.getAlias() + " stopped" );
                instance.stale.remove( tunnel );
                
                // start queued tunnels that were queued because they waited for stale tunnels to be stopped
                //
                if ( instance.stale.isEmpty() && ! instance.queued.isEmpty() ) {
                    
                    for ( Tunnel queued : instance.queued ) {
                        
                        TunnelManager.startTunnel( queued );
                    }
                }
            }
            
        }      
    }
    
    public static void refreshActivated( ArrayList<Tunnel> tunnels ) {

        instance.running.clear();
        
        ArrayList<Tunnel> knownTunnels = new ArrayList<Tunnel>( ConfigManager.getTunnels() );
        
        for ( Tunnel tunnel : tunnels ) {
            
            if ( knownTunnels.contains( tunnel ) ) {
                
                knownTunnels.remove( tunnel );
            
                if ( instance.activating.contains( tunnel ) ) {
                    instance.activating.remove( tunnel );
                }
                
                if ( instance.deactivating.contains( tunnel ) ) {
                    instance.deactivating.remove( tunnel );
                }
                
                tunnel.setRestart( true );
                instance.running.add( tunnel );
    
                for ( TunnelManagerListener listener : instance.listeners ) {
                    listener.tunnelManagerTunnelActivated( tunnel );
                }
            }
        }
        
        // remaining tunnels are deactivated
        //
        for ( Tunnel tunnel : knownTunnels ) {
            
            if ( instance.activating.contains( tunnel ) ) {
                instance.activating.remove( tunnel );
            }
            
            if ( instance.deactivating.contains( tunnel ) ) {
                instance.deactivating.remove( tunnel );
            }
            
            if ( instance.running.contains( tunnel ) ) {
                instance.running.remove( tunnel );
            }

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelDeactivated( tunnel );
            }
        }
    }
    
    public static void updatePing( Tunnel tunnel, long ping ) {
        
        for ( TunnelManagerListener listener : instance.listeners ) {
            listener.tunnelManagerTunnelPing( tunnel, ping );
        }
    }
    
    public static void offline() {
        
        instance.connected = false;
        
        for ( TunnelManagerListener listener : instance.listeners ) {
            listener.tunnelManagerOffline();
        }
    }

    public synchronized static void online() {
        
        instance.connected = true;

        instance.activating.clear();
        instance.deactivating.clear();
        instance.running.clear();
        instance.ignoreAutostart.clear();

        for ( TunnelManagerListener listener : instance.listeners ) {
            listener.tunnelManagerOnline();
        }
        
        // clear stale tunnels, now that we are connected
        //
        if ( ! instance.stale.isEmpty() ) {
            
            for ( Tunnel tunnel : instance.stale ) {
                
                logger.info( "Stopping stale tunnel after connect: " + tunnel.getAlias() );
                
                TunnelClient.sendStopTunnel( tunnel );
            }
        }
    }
    
    public static boolean isStale( Tunnel tunnel ) {
        
        return instance.stale.contains( tunnel ); 
    }
    
    public static void autostartHostAvailable( Tunnel tunnel ) {
        
        if ( ! instance.running.contains( tunnel ) && ! instance.ignoreAutostart.contains( tunnel ) ) {
            
            logger.debug( "Trigger tunnel start: " + tunnel.getAlias() );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartHostAvailable( tunnel );
            }

            TunnelManager.startTunnel( tunnel );
        }

    }
    
    public static void autostartHostUnavailable( Tunnel tunnel ) {

        if ( ! instance.ignoreAutostart.contains( tunnel ) ) {
            
            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartHostUnavailable( tunnel );
            }
        }
    }
    
    public static boolean isRunning( String signature ) {
        
        return instance.running.contains( signature );
    }

    public static boolean isRunning( Tunnel tunnel ) {
        
        return instance.running.contains( tunnel );
    }
    
    public static boolean isConnected() {
        
        return instance.connected;
    }
    
    public static boolean isBusy( Tunnel tunnel ) {
        
        return instance.activating.contains( tunnel ) || instance.deactivating.contains( tunnel );
    }
    
    public static boolean isAutostartDisabled( Tunnel tunnel ) {
        
        return instance.ignoreAutostart.contains( tunnel );
    }
    
    public static void addTunnelManagerListener( TunnelManagerListener listener ) {
        
        instance.listeners.add( listener );
    }
    
    @Override
    public void configManagerGlobalConfigChanged(GlobalConfig oldConfig,
            GlobalConfig newConfig, boolean valid) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void configManagerConfigChanged( Config oldConfig, Config newConfig, boolean valid ) {
        
        if ( valid ) {

            instance.activating.clear();
            instance.deactivating.clear();
            instance.running.clear();
            instance.ignoreAutostart.clear();
            TunnelClient.sendQueryTunnels();
        }
    }
    
}
