package org.khee.kheetun.client;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gnome.gtk.Gtk;
import org.khee.kheetun.client.gui.Gui;


public class kheetun {

    private static Logger logger = LogManager.getLogger( "kheetun" );
    public static final String VERSION = "0.6";
    
    public static void main(String[] args) {
        
        Gtk.init( args );
        
        logger.info( "Starting kheetun " + VERSION );
        
//        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//        } catch ( Exception e ) {
//            logger.fatal( e.getMessage() );
//            System.exit( 1 );
//        }
        
        new File( System.getProperty( "user.home" ) + "/.kheetun" ).mkdir();
        
        TunnelClient.init();
        
        Gui guiSwing = new Gui();
        
        guiSwing.show();
        
        Gtk.main();
    }
}
