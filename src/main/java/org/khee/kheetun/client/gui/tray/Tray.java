package org.khee.kheetun.client.gui.tray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.gui.Imx;

public class Tray implements Runnable {
    
    protected static Logger logger = LogManager.getLogger( "kheetun" );

    public static final int STATE_OFFLINE = 1;
    public static final int STATE_ONLINE  = 2;
    public static final int STATE_WARNING = 3;
    
    protected boolean blinking = false;
    
    public static Tray instance = null;
    
    public Tray() {

        instance = this;
    }
    
    protected void setState( int state ) { };
    
    protected void setIcon( Imx icon ) { };
    
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
