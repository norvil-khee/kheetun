package org.khee.kheetun.client;

import org.khee.kheetun.client.config.Tunnel;

public interface HostPingDaemonListener {
    
    public void hostReachable( Tunnel tunnel );
}
