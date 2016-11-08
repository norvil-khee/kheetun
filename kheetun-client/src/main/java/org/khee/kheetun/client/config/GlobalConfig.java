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
    
    public static final String          SORT_ALPHABETICAL_ASC   = "alphabetical_asc";
    public static final String          SORT_ALPHABETICAL_DESC  = "alphabetical_desc";
    public static final String          SORT_MODIFIED_ASC       = "modified_asc";
    public static final String          SORT_MODIFIED_DESC      = "modified_desc";

    private         ArrayList<String>   errors      = new ArrayList<String>();


    public GlobalConfig() {
        
        properties.setProperty( "port", "7779" );
        properties.setProperty( "sortorder", "alphabetic_asc" );    
    }
    
    public static GlobalConfig load() {
        
        GlobalConfig globalConfig = new GlobalConfig();
        
        try {
            
            globalConfig.properties.load( new FileInputStream( new File ( System.getProperty( "user.home") + "/.kheetun/kheetun.conf" ) ) );
            
            if ( globalConfig.getPort() == null ) {
                globalConfig.setPort( 7779 );
            }
            
            if ( globalConfig.getSortOrder() == null ) {
                globalConfig.setSortOrder( GlobalConfig.SORT_ALPHABETICAL_ASC );
            }
        
        } catch ( IOException eIO ) {
            
            logger.error( "IO Error while loading general config: " + eIO.getLocalizedMessage() ) ;
            globalConfig.addError( "IO exception: " + eIO.getLocalizedMessage() );
        }
        
        return globalConfig;
    }
    
    public void save() {
        
        File configDirectory = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d" );
        
        if ( ! configDirectory.exists() ) {
            configDirectory.mkdir();
        }
        
        try {
            
            properties.store( new FileOutputStream( new File( System.getProperty( "user.home") + "/.kheetun/kheetun.conf" ) ), "global settings for kheetun client" );
            
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
        
        return this.properties.getProperty( "sortorder" );
    }
    
    public void setSortOrder( String sortorder ) {
        
        this.properties.setProperty( "sortorder", sortorder );
    }
    
    public Integer getPort() {
        
        try {
            
            return new Integer( this.properties.getProperty( "port" ) );
            
        } catch ( NumberFormatException eNumber ) {
            
            return null;
        }
    }
    
    public void setPort(Integer port) {
        
        this.properties.setProperty( "port", port.toString() );
    }

}
