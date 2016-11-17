package org.khee.kheetun.client.gui;

import java.awt.Point;
import java.util.HashMap;

public class TrayManager {

    private static TrayManager          instance;
    
    private Tray                        tray;
    private TrayMenu                    menu;
    private HashMap<Integer, String>    errors = new HashMap<Integer, String>();
    
    protected TrayManager() {
        
    }
    
    public static void init() {
        
        instance = new TrayManager();
    }
    
    public static void setTray( Tray tray ) {
        
        instance.tray = tray;
    }
    
    public static void setMenu( TrayMenu menu ) {
        
        instance.menu = menu;
    }
    
    public static void setIcon( Imx icon ) {
        
        instance.tray.setIcon( icon );
    }
    
    public static void blink() {
        
        if ( ! instance.menu.isVisible() ) {
            instance.tray.blink();
        }
    }
    
    public static void unblink() {
        
        instance.tray.unblink();
    }
    
    public static void clearError( int id ) {
        
        instance.errors.remove( id );
        
        if ( instance.errors.isEmpty() ) {
       
            TrayManager.setIcon( Imx.TRAY_OK );
            TrayManager.unblink();
        }
    }
    
    public static void clearAllErrors() {
        
        instance.errors.clear();
        TrayManager.setIcon( Imx.TRAY_OK );
        TrayManager.unblink();
    }
    
    public static void setError( int id, String message ) {
        
        instance.errors.put( id, message );
        
        TrayManager.setIcon( Imx.TRAY_ERROR );
        TrayManager.blink();
    }
    
    public static void toggleMenu( Point p ) {
        
        instance.menu.toggle( p );
        instance.tray.unblink();
    }
    
}
