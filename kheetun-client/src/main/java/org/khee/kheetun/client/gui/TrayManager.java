package org.khee.kheetun.client.gui;

public class TrayManager {

    private static TrayManager instance;
    private Tray tray;
    
    protected TrayManager() {
    }
    
    public static void setTray( Tray tray ) {
        
        if ( instance == null ) {
            instance = new TrayManager();
        }
        
        instance.tray = tray;
        
    }
    
    public static void setState( int state ) {
        
        if ( instance == null ) {
            instance = new TrayManager();
        }
        
        if ( instance.tray != null ) {
            instance.tray.setState( state );
        }
    }
}
