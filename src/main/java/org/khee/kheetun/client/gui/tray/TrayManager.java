package org.khee.kheetun.client.gui.tray;

import java.awt.Point;
import java.util.HashMap;

import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.dialog.Dialog;

public class TrayManager {

    private static TrayManager                          instance;
    
    private Tray                                        tray;
    private TrayMenu                                    menu;
    private Dialog                                      dialog;
    private HashMap<Integer, String>                    errors = new HashMap<Integer, String>();
     
    
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
    
    public static void setDialog( Dialog dialog ) {
        
        instance.dialog = dialog;
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
        
        if ( instance.errors.containsKey( id ) ) {
            
            instance.errors.remove( id );
            
            if ( instance.errors.isEmpty() ) {
                
                instance.errors.clear();
                TrayManager.setIcon( Imx.TRAY.size( 32 ) );
                TrayManager.unblink();
            }
        }
    }
    
    public static void setError( int id, String message ) {
        
        instance.errors.put( id, message );
        
        TrayManager.setIcon( Imx.TRAY_RED.size( 32 ) );
        TrayManager.blink();
    }
    
    public static void toggleMenu( Point p ) {
        
        instance.menu.toggle( p );
        instance.tray.unblink();
    }
    
    public static void toggleDialog() {
        
        instance.dialog.toggle();
    }
    
}
