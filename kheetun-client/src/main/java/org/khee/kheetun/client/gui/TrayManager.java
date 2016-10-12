package org.khee.kheetun.client.gui;

import java.awt.Component;
import java.awt.Point;

public class TrayManager {

    private static TrayManager  instance;
    
    private Tray                tray;
    private TrayMenu            menu;
    private Component           configDialog;
    
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
    
    public static void setConfigDialog( Component configDialog ) {
        
        instance.configDialog = configDialog;
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
    
    public static void showConfigDialog() {
        
        instance.configDialog.revalidate();
        instance.configDialog.setVisible( true );
    }
    
    public static void toggleMenu( Point p ) {
        
        instance.menu.toggle( p );
        instance.tray.unblink();
    }
    
}
