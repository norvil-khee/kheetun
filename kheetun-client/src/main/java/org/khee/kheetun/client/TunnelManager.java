package org.khee.kheetun.client;

import java.util.ArrayList;

import org.khee.kheetun.client.config.Tunnel;

public class TunnelManager implements TunnelClientListener {
    
    private static TunnelManager    instance    = null;
    
    private ArrayList<String>       running     = new ArrayList<String>();   
    
    protected TunnelManager() {
        
        TunnelClient.addClientListener( this );
    }
    
    public static void init() {
        
        instance = new TunnelManager();
    }
    
    public static boolean isRunning( String signature ) {
        
        if ( instance == null ) {
            instance = new TunnelManager();
        }
        
        return instance.running.contains( signature );
    }

    public static boolean isRunning( Tunnel tunnel ) {
        
        if ( instance == null ) {
            instance = new TunnelManager();
        }
        
        return instance.running.contains( tunnel.getSignature() );
    }
    
    @Override
    public void connected() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void disconnected() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void error(Tunnel tunnel, String error) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void activeTunnels(ArrayList<String> signatures) {
        
        running.clear();
        
        for ( String signature : signatures ) {
            running.add( signature );
        }
    }
    
    @Override
    public void tunnelPing(String signature, long ping) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelStarted(String signature) {
        
        if ( ! running.contains( signature ) ) {
            running.add( signature );
        }
    }
    
    @Override
    public void tunnelStopped(String signature) {
        
        if ( running.contains( signature ) ) {
            running.remove( signature );
        }
    }
}
