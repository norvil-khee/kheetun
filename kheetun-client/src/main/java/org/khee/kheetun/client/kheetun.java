package org.khee.kheetun.client;

import java.io.File;

import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.gui.Gui;


public class kheetun {

    private static Logger logger = LogManager.getLogger( "kheetun" );
    public static final String VERSION = "0.5";
    
    public static void main(String[] args) {
        
        logger.info( "Starting kheetun " + VERSION );
        
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch ( Exception e ) {
            logger.fatal( e.getMessage() );
            System.exit( 1 );
        }
        
        new File( System.getProperty( "user.home" ) + "/.kheetun" ).mkdir();
        
        TunnelClient.init();
        
        Gui guiSwing = new Gui();
        
        guiSwing.show();
    }
}
