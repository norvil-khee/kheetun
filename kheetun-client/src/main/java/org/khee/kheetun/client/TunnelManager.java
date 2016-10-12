package org.khee.kheetun.client;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Tunnel;

public class TunnelManager {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private static TunnelManager                instance        = null;
    
    private ArrayList<String>                   activating      = new ArrayList<String>();
    private ArrayList<String>                   deactivating    = new ArrayList<String>();
    private ArrayList<String>                   running         = new ArrayList<String>();   
    private ArrayList<TunnelManagerListener>    listeners       = new ArrayList<TunnelManagerListener>();
    private ArrayList<String>                   ignoreAutostart = new ArrayList<String>();
    
    private boolean                             connected       = false;
    
    protected TunnelManager() {
    }
    
    public static void init() {
        
        instance = new TunnelManager();
    }
    
    public static void startTunnel( Tunnel tunnel ) {
        
        if ( ! instance.connected ) {
            return;
        }
        
        if ( ! instance.activating.contains( tunnel.getSignature() ) ) {
            
            logger.info( "Starting tunnel: " + tunnel.getAlias() );
            logger.debug( tunnel.getSignature() );
            
            if ( instance.deactivating.contains( tunnel.getSignature() ) ) {
                instance.deactivating.remove( tunnel.getSignature() );
            }
            
            instance.activating.add( tunnel.getSignature() );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelActivating( tunnel.getSignature() );
            }
            
            TunnelClient.sendStartTunnel( tunnel );
        }
    }
    
    public static void stopTunnel( Tunnel tunnel ) {
        
        if ( ! instance.connected ) {
            return;
        }
        
        if ( ! instance.deactivating.contains( tunnel.getSignature() ) ) {
            
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
            
            TunnelManager.stopped( tunnel.getSignature() );

            for ( TunnelManagerListener listener : instance.listeners ) {
                logger.debug( "Notifying " + listener );
                listener.tunnelManagerTunnelError( tunnel.getSignature(), error );
            }
        }
    }
    
    public static void enableAutostart( Tunnel tunnel ) {
        
        if ( instance.ignoreAutostart.contains( tunnel.getSignature() ) ) {
            
            instance.ignoreAutostart.remove( tunnel.getSignature() ); 

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartEnabled( tunnel );
            }
        }
    }
    
    public static void disableAutostart( Tunnel tunnel ) {
        
        if ( ! instance.ignoreAutostart.contains( tunnel.getSignature() ) ) {
            
            instance.ignoreAutostart.add( tunnel.getSignature() ); 

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerAutostartDisabled( tunnel );
            }
        }
    }
    
    public static void started( String signature ) {
        
        if ( ! instance.running.contains( signature ) ) {
            
            if ( instance.activating.contains( signature ) ) {
                instance.activating.remove( signature );
            }
            
            instance.running.add( signature );
            
            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelActivated( signature );
            }
        }        
    }
    
    
    public static void stopped( String signature ) {
        
        if ( instance.running.contains( signature ) ) {
            
            if ( instance.deactivating.contains( signature ) ) {
                instance.deactivating.remove( signature );
            }
            
            instance.running.remove( signature );

            for ( TunnelManagerListener listener : instance.listeners ) {
                listener.tunnelManagerTunnelDeactivated( signature );
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

    public static void online() {
        
        instance.connected = true;

        for ( TunnelManagerListener listener : instance.listeners ) {
            listener.tunnelManagerOnline();
        }
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
    
}
