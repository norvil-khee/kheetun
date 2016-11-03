package org.khee.kheetun.client.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.compat.Config_0_9_0;

public class Config {
    
    private static  Logger              logger      = LogManager.getLogger( "kheetun" );
    
    private         ArrayList<Profile>  profiles    = new ArrayList<Profile>();
    private         Properties          properties  = new Properties();
    private         ArrayList<String>   errors      = new ArrayList<String>();

    
    public Config() { 
        
        properties.setProperty( "port", "7779" );
    }
    
    public Config( Config source ) {
        
        this.profiles = new ArrayList<Profile>();
        this.profiles = source.getProfiles();
        
        for ( Profile profile : source.getProfiles() ) {
            addProfile( new Profile( profile ) );
        }
    }
    
    public ArrayList<Profile> getProfiles() {
        return profiles;
    }
    public void setProfiles(ArrayList<Profile> profiles) {
        this.profiles = profiles;
    }
    public void addProfile( Profile profile ) {
        profiles.add( profile );
    }
    
    public boolean isValid() {
        
        for( Profile profile : getProfiles() ) {
            
            if ( ! profile.isValid() ) {
                return false;
            }
        }
        
        return this.getPort() != null;
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
    
    public ArrayList<String> getErrors() {
        return errors;
    }

    public void addError( String error ) {
        this.errors.add( error );
    }

    public boolean equals(Object obj) {
        
        Config compare = (Config)obj;
        
        ArrayList<Profile> profiles = this.getProfiles();
        ArrayList<Profile> profilesCompare = compare.getProfiles();
        
        // not the same amount of profiles? not equal!
        //
        if ( profiles.size() != profilesCompare.size() ) {
            return false;
        }
        
        // profiles differ? not equal!
        //
        for ( int index = 0 ; index < profiles.size() ; index++ ) {
            
            if ( ! profiles.get( index ).equals( profilesCompare.get( index )) ) {
                return false;
            }
        }
        
        return this.getPort().equals( compare.getPort() );
    }
    
    
    public static Config load() {
        
        // handle deprecation
        //
        File fileConfig_0_9_0 = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.xml" );
        
        if ( fileConfig_0_9_0.exists() ) {
            
            logger.info( "Deprecation: updating configuration from 0.9.0 to current" );
            
            try {
                Config oldConfig = Config_0_9_0.load( fileConfig_0_9_0 );
                oldConfig.save();
                
                fileConfig_0_9_0.renameTo( new File( System.getProperty( "user.home") + "/.kheetun/kheetun.xml.deprecated" ) );
                
            } catch ( JAXBException eJAX ) {

                String error = ( eJAX.getCause() != null ? eJAX.getCause().getLocalizedMessage() : eJAX.getLocalizedMessage() );
                logger.error( "Error while reading deprecated config: " + error );
            }
        }
        
        Config config = new Config();
        
        File configDirectory = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d" );
        
        if ( configDirectory.exists() ) {
            
            try {
                
                config.properties.load( new FileInputStream( new File ( System.getProperty( "user.home") + "/.kheetun/kheetun.conf" ) ) );
                
                if ( config.getPort() == null ) {
                    config.addError( "Port is undefined" );
                }
            
            } catch ( IOException eIO ) {
                
                logger.error( "IO Error while loading general config: " + eIO.getLocalizedMessage() ) ;
                config.addError( "IO exception: " + eIO.getLocalizedMessage() );
            }
            
            
            try {

                JAXBContext context = JAXBContext.newInstance( Profile.class );
                Unmarshaller u = context.createUnmarshaller();
            
                for ( File profileFile : configDirectory.listFiles() ) {
                    
                    if ( ! profileFile.getAbsolutePath().matches( ".*\\.xml$" ) ) {
                        continue;
                    }
                    
                    Profile profile;
                    
                    try {
                    
                        profile = (Profile)u.unmarshal( profileFile );

                    } catch ( JAXBException eJAX ) {

                        String error = ( eJAX.getCause() != null ? eJAX.getCause().getLocalizedMessage() : eJAX.getLocalizedMessage() );
                        
                        logger.error( "Error in profile while loading " + profileFile.getName() + ": " + error ) ;
                        
                        profile = new Profile();
                        profile.addError( error );
                    }
                    
                    profile.setConfigFile( profileFile.getName() );
                    config.profiles.add( profile );
                }
                
            } catch ( JAXBException eJAX ) {
                
                String error = ( eJAX.getCause() != null ? eJAX.getCause().getLocalizedMessage() : eJAX.getLocalizedMessage() );
                
                logger.error( "Error creating marshaller while loading config: " + error ) ;
                config.addError( error );
            }
        }
        
        return config;
    }
    
    
    public void save() {
        
        File configDirectory = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d" );
        
        if ( ! configDirectory.exists() ) {
            configDirectory.mkdir();
        }
        
        try {
            
            properties.store( new FileOutputStream( new File( System.getProperty( "user.home") + "/.kheetun/kheetun.conf" ) ), "global settings for kheetun client" );
            
            JAXBContext context = JAXBContext.newInstance( Profile.class );
            Marshaller m = context.createMarshaller();
            
            for ( Profile profile : this.profiles ) {
                
                File file = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d/" + profile.getName().toLowerCase() + ".xml" );
            
                m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
                m.marshal( profile, file );
            }
            
        } catch ( JAXBException e ) {

            logger.error( e.getMessage() );
            
        } catch ( IOException eIO ) {
            
            logger.error( eIO.getMessage() );
        }
    }
    
}
