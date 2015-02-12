package org.khee.kheetun.client.gui;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.TunnelClient;

import org.khee.kheetun.client.config.Config;

public class Gui {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private ConfigFrame dialogConfig;
    
    public Gui() {
        
        TrayIcon tray = new TrayIcon( Imx.KHEETUN.s24.getImage() );
        
        tray.setImageAutoSize( true );
        
        dialogConfig = new ConfigFrame();

        TrayMenu menu = new TrayMenu( tray, dialogConfig );
        
        
        File defaultConfig = new File( System.getProperty( "user.home" ) + "/.kheetun.xml" );
        
        Config config = new Config();
        
        if ( defaultConfig.canRead() ) {
            
            try {
                config = Config.load( defaultConfig );
                
            } catch( JAXBException e ) {
                
                logger.warn( "Could not load default configuration: " + e.getMessage() );
                e.printStackTrace();
            }
        }
        
        dialogConfig.setConfig( config );
        menu.buildMenu( config );

        try {
            SystemTray.getSystemTray().add( tray );
        } catch ( Exception e ) {
            logger.error( e.getMessage() );
            System.exit( 1 );
        }
        
        TunnelClient.init();
        TunnelClient.connect();
    }
    
    public void show() {
        
    }
}


