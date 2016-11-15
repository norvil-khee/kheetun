package org.khee.kheetun.client.gui;

import java.awt.Component;
import java.awt.Point;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrayManager {

    private static Logger logger = LogManager.getLogger( "kheetun" );

    private static TrayManager          instance;
    
    private Tray                        tray;
    private TrayMenu                    menu;
    private Component                   configDialog;
    private HashMap<Integer, String>    messages = new HashMap<Integer, String>();
    
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
    
    public static void clearMessage( int id ) {
        
        logger.debug( "TrayManager remove message: " + id );

        instance.messages.remove( id );
        
        if ( instance.messages.isEmpty() ) {
       
            TrayManager.setIcon( Imx.KHEETUN_ON );
        }
    }
    
    public static void clearMessages() {
        
        instance.messages.clear();
    }
    
    public static void setMessage( int id, String message ) {
        
        logger.debug( "TrayManager adding message: " + id + "/" + message );
        
        instance.messages.put( id, message );
        
        TrayManager.setIcon( Imx.KHEETUN_WARNING );
    }
    
    public static void showConfigDialog() {
        
        instance.configDialog.revalidate();
        instance.configDialog.setVisible( true );
    }
    
    public static void toggleMenu( Point p ) {
        
        instance.menu.toggle( p );
        instance.tray.unblink();
    }
    
    public static void buildMenu() {
        
        instance.menu.buildMenu();
    }
    
}
