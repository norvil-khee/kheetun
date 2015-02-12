package org.khee.kheetun.client;

import java.util.ArrayList;

public interface TunnelClientListener {

    public void error( String error );
    public void connected();
    public void disconnected();
    public void tunnelStarted( String signature );
    public void tunnelStopped( String signature );
    public void activeTunnels( ArrayList<String> signatures );
}
