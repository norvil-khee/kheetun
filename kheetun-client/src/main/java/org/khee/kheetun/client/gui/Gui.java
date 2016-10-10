package org.khee.kheetun.client.gui;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.HostPingDaemon;
import org.khee.kheetun.client.TunnelClient;
import org.khee.kheetun.client.config.Config;

public class Gui {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private ConfigFrame dialogConfig;
    
    public Gui() {
        
        dialogConfig = new ConfigFrame();

        TrayMenu menu = new TrayMenu( dialogConfig );
        Tray     tray = new GtkTray( menu );
        
        tray.setState( GtkTray.STATE_OFFLINE );
        
        File defaultConfig = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.xml" );
        
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
        
        HostPingDaemon.setConfig( config );
        
        TrayManager.setTray( tray );
        
        menu.buildMenu( config );
        
        TunnelClient.init();
        TunnelClient.connect( config.getPort() );
    }
    
    public void show() {
        
    }
}


