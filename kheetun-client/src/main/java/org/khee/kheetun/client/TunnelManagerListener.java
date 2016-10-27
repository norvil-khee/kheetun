package org.khee.kheetun.client;

import org.khee.kheetun.client.config.Tunnel;

public interface TunnelManagerListener {
    
    public void tunnelManagerOnline();
    public void tunnelManagerOffline();
    
    public void tunnelManagerTunnelActivating( Tunnel tunnel );
    public void tunnelManagerTunnelActivated( Tunnel tunnel );
    public void tunnelManagerTunnelDeactivating( Tunnel tunnel );
    public void tunnelManagerTunnelDeactivated( Tunnel tunnel );
    
    public void tunnelManagerAutostartHostAvailable( Tunnel tunnel );
    public void tunnelManagerAutostartHostUnavailable( Tunnel tunnel );
    public void tunnelManagerAutostartDisabled( Tunnel tunnel );
    public void tunnelManagerAutostartEnabled( Tunnel tunnel );
    
    public void tunnelManagerTunnelError( Tunnel tunnel, String error );
    
    public void tunnelManagerTunnelPing( Tunnel tunnel, long ping );
}
