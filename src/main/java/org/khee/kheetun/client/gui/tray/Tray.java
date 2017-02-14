package org.khee.kheetun.client.gui.tray;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.gui.Imx;

public abstract class Tray {
    
    protected static Logger logger = LogManager.getLogger( "kheetun" );

    public static final int STATE_OFFLINE = 1;
    public static final int STATE_ONLINE  = 2;
    public static final int STATE_WARNING = 3;
    
    protected boolean   blinking = false;
    protected Imx       icon;
    private Timer       timer;
    
    public static Tray instance = null;
    
    public Tray() {

        instance = this;
    }
    
    protected void setIcon( Imx icon ) { 

        this.icon = icon;
        
        this.processSetIcon( icon );
        
    };
    
    protected abstract void processSetIcon( Imx icon );
    
    protected Imx getIcon() {

        return this.icon;
    }
    
    protected void blink() {
        
        if ( ! this.blinking ) {
            
            this.blinking = true;
            
            this.timer = new Timer( "kheetun-tray-blink-timer" );
            
            this.timer.schedule( new TimerTask() {
                
                @Override
                public void run() {
                    
                    Imx oldIcon = Tray.this.getIcon();
                    
                    Tray.this.setIcon( Imx.NONE );
                    
                    try {
                        Thread.sleep( 500 );
                    } catch ( InterruptedException eInterrupted ) {
                        
                        logger.error( "Interrupted while trying to sleep in tray blink thread: ", eInterrupted );
                    }
                    
                    Tray.this.setIcon( oldIcon );

                    try {
                        Thread.sleep( 500 );
                    } catch ( InterruptedException eInterrupted ) {
                        
                        logger.error( "Interrupted while trying to sleep in tray blink thread: ", eInterrupted );
                    }
                }
            }, 500, 500 );
        }
    }
    
    protected void unblink() {
        
        if ( this.blinking ) {
            
            this.timer.cancel();
            this.blinking = false;
        }
    }    

}
