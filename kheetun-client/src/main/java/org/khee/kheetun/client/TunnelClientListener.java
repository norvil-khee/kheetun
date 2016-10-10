package org.khee.kheetun.client;

import java.util.ArrayList;

import org.khee.kheetun.client.config.Tunnel;

public interface TunnelClientListener {

    public void error( Tunnel tunnel, String error );
    public void connected();
    public void disconnected();
    public void tunnelStarted( String signature );
    public void tunnelStopped( String signature );
    public void activeTunnels( ArrayList<String> signatures );
    public void tunnelPing( String signature, long ping );
}
