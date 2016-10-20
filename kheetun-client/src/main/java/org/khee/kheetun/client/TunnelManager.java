package org.khee.kheetun.client;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.Tunnel;

public class TunnelManager implements ConfigManagerListener {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private static TunnelManager                instance        = null;
    
    private ArrayList<String>                   activating      = new ArrayList<String>();
    private ArrayList<String>                   deactivating    = new ArrayList<String>();
    private ArrayList<String>                   running         = new ArrayList<String>();   
    private ArrayList<TunnelManagerListener>    listeners       = new ArrayList<TunnelManagerListener>();
    private ArrayList<String>                   ignoreAutostart = new ArrayList<String>();
    private ArrayList<Tunnel>                   stale           = new ArrayList<Tunnel>();
    private ArrayList<Tunnel>                   queued          = new ArrayList<Tunnel>();
    
    private boolean                             connected       = false;
    
    protected TunnelManager() {
        
        ConfigManager.addConfigManagerListener( this );
    }
    
    public static void init() {
        
        instance = new TunnelManager();
    }
    
    public synchronized static void startTunnel( Tunnel tunnel ) {
        
        if ( ! instance.connected ) {
            return;
        }
        
        if ( ! instance.running.contains( tunnel.getSignature() ) && ! instance.activating.contains( tunnel.getSignature() ) ) {
            
            logger.info( "Starting tunnel: " + tunnel.getAlias() );
            logger.debug( tunnel.getSignature() );
            
            if ( instance.deactivating.contains( tunnel.getSignature() ) ) {
                instance.deactivating.remove( tunnel.getSignature() );
            }
            
            instance.activating.add( tunnel.getSignature() );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelActivating( tunnel.getSignature() );
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
        
        if ( ! instance.connected ) {
            return;
        }
        
        if ( instance.running.contains( tunnel.getSignature() ) && ! instance.deactivating.contains( tunnel.getSignature() ) ) {
            
            logger.info( "Stopping tunnel: " + tunnel.getAlias() );
            logger.debug( tunnel.getSignature() );
            
            if ( instance.activating.contains( tunnel.getSignature() ) ) {
                instance.activating.remove( tunnel.getSignature() );
            }
            
            instance.deactivating.add( tunnel.getSignature() );
            TunnelClient.sendStopTunnel( tunnel );
            
            // since this is a manual stop, we deactivate autostarting this tunnel again
            //
            TunnelManager.disableAutostart( tunnel );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelDeactivating( tunnel.getSignature() );
            }
            
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
            
            if ( instance.activating.contains( tunnel.getSignature() ) ) {
                logger.debug( "Remove activating tunnel because of error" );
                instance.activating.remove( tunnel.getSignature() );
            }
            
            if ( instance.deactivating.contains( tunnel.getSignature() ) ) {
                logger.debug( "Remove deactivating tunnel because of error" );
                instance.deactivating.remove( tunnel.getSignature() );
            }
            
            TunnelManager.stopped( tunnel );

            for ( TunnelManagerListener listener : instance.listeners ) {
                logger.debug( "Notifying " + listener );
                listener.tunnelManagerTunnelError( tunnel.getSignature(), error );
            }
        }
    }
    
    public synchronized static void enableAutostart( Tunnel tunnel ) {
        
        if ( instance.ignoreAutostart.contains( tunnel.getSignature() ) ) {
            
            instance.ignoreAutostart.remove( tunnel.getSignature() ); 

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartEnabled( tunnel );
            }
        }
    }
    
    public synchronized static void disableAutostart( Tunnel tunnel ) {
        
        if ( ! tunnel.getAutostart() ) {
            return;
        }
        
        logger.info( "Disable autostart temporary for tunnel " + tunnel.getAlias() );
        
        if ( ! instance.ignoreAutostart.contains( tunnel.getSignature() ) ) {
            
            instance.ignoreAutostart.add( tunnel.getSignature() ); 

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartDisabled( tunnel );
            }
        } else {
            
            logger.warn( "Autostart already temporarily disabled for tunnel " + tunnel.getAlias() );
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
        
        if ( ! instance.running.contains( tunnel.getSignature() ) ) {
            
            if ( instance.activating.contains( tunnel.getSignature() ) ) {
                instance.activating.remove( tunnel.getSignature() );
            }
            
            if ( instance.queued.contains( tunnel ) ) {
                instance.queued.remove( tunnel );
            }
            
            instance.running.add( tunnel.getSignature() );
            
            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelActivated( tunnel.getSignature() );
            }
        }        
    }
    
    
    public static void stopped( Tunnel tunnel ) {
        
        if ( instance.running.contains( tunnel.getSignature() ) ) {
            
            if ( instance.deactivating.contains( tunnel.getSignature() ) ) {
                instance.deactivating.remove( tunnel.getSignature() );
            }
            
            instance.running.remove( tunnel.getSignature() );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelDeactivated( tunnel.getSignature() );
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
    
    public static void refreshActivated( ArrayList<String> signatures ) {

        instance.running.clear();
        
        for ( String signature : signatures ) {
            
            if ( instance.activating.contains( signature ) ) {
                instance.activating.remove( signature );
            }
            
            if ( instance.deactivating.contains( signature ) ) {
                instance.deactivating.remove( signature );
            }
            
            instance.running.add( signature );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelActivated( signature );
            }
        }
    }
    
    public static void updatePing( String signature, long ping ) {
        
        for ( TunnelManagerListener listener : instance.listeners ) {
            listener.tunnelManagerTunnelPing( signature, ping );
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
        
        if ( ! instance.running.contains( tunnel.getSignature() ) && ! instance.ignoreAutostart.contains( tunnel.getSignature() ) ) {
            
            logger.debug( "Trigger tunnel start: " + tunnel.getAlias() );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartHostAvailable( tunnel );
            }

            TunnelManager.startTunnel( tunnel );
        }

    }
    
    public static void autostartHostUnavailable( Tunnel tunnel ) {

        if ( ! instance.ignoreAutostart.contains( tunnel.getSignature() ) ) {
            
            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartHostUnavailable( tunnel );
            }
        }
    }
    
    public static boolean isRunning( String signature ) {
        
        return instance.running.contains( signature );
    }

    public static boolean isRunning( Tunnel tunnel ) {
        
        return instance.running.contains( tunnel.getSignature() );
    }
    
    public static boolean isConnected() {
        
        return instance.connected;
    }
    
    public static boolean isBusy( Tunnel tunnel ) {
        
        return instance.activating.contains( tunnel.getSignature() ) || instance.deactivating.contains( tunnel.getSignature() );
    }
    
    public static void addTunnelManagerListener( TunnelManagerListener listener ) {
        
        instance.listeners.add( listener );
    }
    
    @Override
    public void configManagerConfigChanged(Config config) {
    }
    
    @Override
    public void configManagerConfigInvalid(Config config, ArrayList<String> errorStack) {
    }
    
    @Override
    public void configManagerConfigValid(Config config) {
        
        instance.activating.clear();
        instance.deactivating.clear();
        instance.running.clear();
        instance.ignoreAutostart.clear();
        TunnelClient.sendQueryTunnels();
    }
    
}
