package org.khee.kheetun.client;

import org.khee.kheetun.client.config.Tunnel;

public interface TunnelClientListener {
    
    public void TunnelClientConnection( boolean connected, String error, int status );
    public void TunnelClientTunnelStatus( Tunnel tunnel );
}
