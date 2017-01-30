package org.khee.kheetun.client;

import java.io.File;
import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gnome.gtk.Gtk;
import org.khee.kheetun.Kheetun;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.dialog.Dialog;
import org.khee.kheetun.client.gui.tray.GtkTray;
import org.khee.kheetun.client.gui.tray.Tray;
import org.khee.kheetun.client.gui.tray.TrayManager;
import org.khee.kheetun.client.gui.tray.TrayMenu;

public class KheetunClient {
    
    static {
        System.setProperty( "log4j.configurationFile", "log4j2.client.xml" );
    }
    private static Logger logger = LogManager.getLogger( KheetunClient.class );
    
    public static void main(String[] args) {
        
        ConfigManager.init();

        System.setProperty( "sun.java2d.xrender", "false" );

        Gtk.init( args );
        
        TrayManager.init();
        ConfigManager.init();
        TunnelClient.init();
        
        // disable DNS caching
        //
        Security.setProperty( "networkaddress.cache.ttl", "0" );
        Security.setProperty( "networkaddress.cache.negative.ttl", "0" );
        
        // disable xrender ( https://stackoverflow.com/questions/34188495/how-can-i-work-around-the-classcastexception-in-java2d-bug-id-7172749 )
        // described bug will happen here if you switch from two monitors to one, and after that back to two.
        //
        System.setProperty( "sun.java2d.xrender", "false" );
        
        logger.info( "Starting kheetun " + Kheetun.VERSION );
        
        new File( System.getProperty( "user.home" ) + "/.kheetun" ).mkdir();
        new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.d" ).mkdir();
        
        Tray        tray            = new GtkTray();
        TrayMenu    menu            = new TrayMenu();
        Dialog      dialog          = Dialog.getInstance();
        
        TrayManager.setTray( tray );
        TrayManager.setMenu( menu );
        TrayManager.setDialog( dialog );
        TrayManager.setIcon( Imx.TRAY.size( 32 ) );

        ConfigManager.start();
        
        Gtk.main();
    }
}
