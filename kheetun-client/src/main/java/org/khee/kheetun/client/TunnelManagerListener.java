package org.khee.kheetun.client;

import org.khee.kheetun.client.config.Tunnel;

public interface TunnelManagerListener {
    
    public void tunnelManagerOnline();
    public void tunnelManagerOffline();
    
    public void tunnelManagerTunnelActivating( String signature );
    public void tunnelManagerTunnelActivated( String signature );
    public void tunnelManagerTunnelDeactivating( String signature );
    public void tunnelManagerTunnelDeactivated( String signature );
    
    public void tunnelManagerAutostartHostAvailable( Tunnel tunnel );
    public void tunnelManagerAutostartHostUnavailable( Tunnel tunnel );
    public void tunnelManagerAutostartDisabled( Tunnel tunnel );
    public void tunnelManagerAutostartEnabled( Tunnel tunnel );
    
    public void tunnelManagerTunnelError( String signature, String error );
    
    public void tunnelManagerTunnelPing( String signature, long ping );
}
