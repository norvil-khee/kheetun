package org.khee.kheetun.client.gui;

import java.awt.Point;
import java.util.HashMap;

public class TrayManager {

    private static TrayManager                          instance;
    
    private Tray                                        tray;
    private TrayMenu                                    menu;
    private HashMap<String, HashMap<Integer, String>>   errors = new HashMap<String, HashMap<Integer, String>>();
     
    
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
    
    public static void clearError( String scope, int id ) {
        
        if ( instance.errors.containsKey( scope ) ) {
            
            instance.errors.get( scope ).remove( id );
            
            if ( instance.errors.get( scope ).isEmpty() ) {
                
                TrayManager.clearErrors( scope );
            }
        }
    }
    
    public static void clearErrors( String scope ) {
        
        if ( instance.errors.containsKey( scope ) ) {
        
            instance.errors.remove( scope );
            
            if ( instance.errors.isEmpty() ) {
                
                TrayManager.setIcon( Imx.TRAY_OK );
                TrayManager.unblink();
            }
        }
    }
    
    public static void setError( String scope, int id, String message ) {
        
        if ( ! instance.errors.containsKey( scope ) ) {
            instance.errors.put( scope, new HashMap<Integer, String>() );
        }
        
        instance.errors.get( scope ).put( id, message );
        
        TrayManager.setIcon( Imx.TRAY_ERROR );
        TrayManager.blink();
    }
    
    public static void toggleMenu( Point p ) {
        
        instance.menu.toggle( p );
        instance.tray.unblink();
    }
    
}
