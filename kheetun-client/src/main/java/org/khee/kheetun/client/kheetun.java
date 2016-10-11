package org.khee.kheetun.client;

import java.io.File;
import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gnome.gtk.Gtk;
import org.khee.kheetun.client.gui.Gui;


public class kheetun {

    private static Logger logger = LogManager.getLogger( "kheetun" );
    public static final String VERSION = "0.6";
    
    public static void main(String[] args) {
        
        Gtk.init( args );
        
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
        
        TunnelClient.init();
        
        Gui guiSwing = new Gui();
        
        guiSwing.show();
        
        Gtk.main();
    }
}
