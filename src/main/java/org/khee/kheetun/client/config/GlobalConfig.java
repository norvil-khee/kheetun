package org.khee.kheetun.client.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalConfig {
    
    private static  Logger              logger      = LogManager.getLogger( "kheetun" );
    
    private         Properties          properties  = new Properties();
    
    public static final File            CONFIG_FILE             = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.conf" );

    public static final String          SORT_ALPHABETICAL_ASC   = "alphabetical_asc";
    public static final String          SORT_ALPHABETICAL_DESC  = "alphabetical_desc";
    public static final String          SORT_MODIFIED_ASC       = "modified_asc";
    public static final String          SORT_MODIFIED_DESC      = "modified_desc";
    
    public static final String          DEFAULT_HOST            = "127.0.0.1";
    public static final Integer         DEFAULT_PORT            = 7779;
    public static final String          DEFAULT_SORTORDER       = SORT_ALPHABETICAL_ASC;
    public static final String          DEFAULT_STOPONEXIT      = "ask";

    private         ArrayList<String>   errors      = new ArrayList<String>();


    public GlobalConfig() {
        
        properties.setProperty( "host",         GlobalConfig.DEFAULT_HOST );  
        properties.setProperty( "port",         GlobalConfig.DEFAULT_PORT.toString() );
        properties.setProperty( "sortorder",    GlobalConfig.DEFAULT_SORTORDER );
        properties.setProperty( "stopOnExit",   GlobalConfig.DEFAULT_STOPONEXIT );
    }
    
    public static GlobalConfig load() {
        
        GlobalConfig globalConfig = new GlobalConfig();
        
        if ( GlobalConfig.CONFIG_FILE.exists() ) {
            
            try {
                
                globalConfig.properties.load( new FileInputStream( new File ( System.getProperty( "user.home") + "/.kheetun/kheetun.conf" ) ) );
                
            } catch ( IOException eIO ) {
                
                logger.error( "IO Error while loading general config: " + eIO.getLocalizedMessage() ) ;
                globalConfig.addError( "IO exception: " + eIO.getLocalizedMessage() );
            }
        }
        
        return globalConfig;
    }
    
    public void save() {
        
        File configDirectory = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d" );
        
        if ( ! configDirectory.exists() ) {
            configDirectory.mkdir();
        }
        
        try {
            
            properties.store( new FileOutputStream( GlobalConfig.CONFIG_FILE ), "Global settings for kheetun client" );
            
        } catch ( IOException eIO ) {
            
            logger.error( eIO.getMessage() );
        }
    }
    
    public ArrayList<String> getErrors() {
        return errors;
    }

    public void addError( String error ) {
        this.errors.add( error );
    }

    
    public String getSortOrder() {
        
        return ( this.properties.getProperty( "sortorder" ) != null ? this.properties.getProperty( "sortorder" ) : GlobalConfig.DEFAULT_SORTORDER );
    }
    
    public void setSortOrder( String sortorder ) {
        
        this.properties.setProperty( "sortorder", sortorder );
    }
    
    public Integer getPort() {
        
        try {
            
            return new Integer( this.properties.getProperty( "port" ) );
            
        } catch ( NumberFormatException eNumber ) {
            
            return GlobalConfig.DEFAULT_PORT;
        }
    }
    
    public void setPort( Integer port ) {
        
        this.properties.setProperty( "port", port.toString() );
    }
    
    public String getHost() {
        
        return ( this.properties.getProperty( "host" ) != null ? this.properties.getProperty( "host" ) : GlobalConfig.DEFAULT_HOST );
    }
    
    public void setHost( String host ) {
        
        this.properties.setProperty( "host", host );
    }
    

    public String getStopOnExit() {
        
        return ( this.properties.getProperty( "stopOnExit" ) != null ? this.properties.getProperty( "stopOnExit" ) : GlobalConfig.DEFAULT_STOPONEXIT );
    }
    
    public void setStopOnExit( String stopOnExit ) {
        
        this.properties.setProperty( "stopOnExit", stopOnExit );
    }

}
