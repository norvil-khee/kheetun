package org.khee.kheetun.client;

import java.io.File;
import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gnome.gtk.Gtk;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.gui.GtkTray;
import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.Tray;
import org.khee.kheetun.client.gui.TrayManager;
import org.khee.kheetun.client.gui.TrayMenu;

public class kheetun {

    private static Logger logger = LogManager.getLogger( "kheetun" );
    public static final String VERSION = "0.9.0";
    
    public static void main(String[] args) {
        
        Gtk.init( args );
        
        ConfigManager.init();
        TunnelManager.init();
        TunnelClient.init();
        HostPingDaemon.init();
        
        // disable DNS caching
        //
        Security.setProperty( "networkaddress.cache.ttl", "0" );
        Security.setProperty( "networkaddress.cache.negative.ttl", "0" );
        
        // disable xrender ( https://stackoverflow.com/questions/34188495/how-can-i-work-around-the-classcastexception-in-java2d-bug-id-7172749 )
        // described bug will happen here if you switch from two monitors to one, and after that back to two.
        //
        System.setProperty( "sun.java2d.xrender", "false" );
        
        logger.info( "Starting kheetun " + VERSION );
        
        new File( System.getProperty( "user.home" ) + "/.kheetun" ).mkdir();
        
        Tray        tray            = new GtkTray();
        TrayMenu    menu            = new TrayMenu();
        
        TrayManager.init();
        TrayManager.setTray( tray );
        TrayManager.setMenu( menu );
        TrayManager.setIcon( Imx.KHEETUN_OFF );

        ConfigManager.start();
        
        Gtk.main();
    }
}
