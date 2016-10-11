package org.khee.kheetun.client.gui;

public class Tray implements Runnable {
    
    public static final int STATE_OFFLINE = 1;
    public static final int STATE_ONLINE  = 2;
    public static final int STATE_WARNING = 3;
    
    protected boolean blinking = false;
    
    public static Tray instance = null;
    
    public Tray() {

        instance = this;
    }
    
    protected void setState( int state ) { };
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }
    
    protected void blink() {
        
        blinking = true;
    }
    
    protected void unblink() {
        
        blinking = false;
    }    

}
