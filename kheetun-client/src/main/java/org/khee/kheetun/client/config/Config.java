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
    
    public ArrayList<Tunnel> findTunnels( String signature ) {
        
        ArrayList<Tunnel> tunnels = new ArrayList<Tunnel>();
        
        for( Profile profile : getProfiles() ) {
            
            for ( Tunnel tunnel : profile.getTunnels() ) {
                
                if ( tunnel.getSignature().equals( signature ) ) {
                    tunnels.add( tunnel );
                }
            }
        }
        
        return tunnels;
    }
    
    public Integer getPort() {
        
        return new Integer( this.properties.getProperty( "port" ) );
    }
    
    public void setPort(Integer port) {
        
        this.properties.setProperty( "port", port.toString() );
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
    
    
    public static Config load() throws JAXBException, IOException {
        
        // handle deprecation
        //
        File fileConfig_0_9_0 = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.xml" );
        
        if ( fileConfig_0_9_0.exists() ) {
            
            logger.info( "Deprecation: updating configuration from 0.9.0 to current" );
            
            Config oldConfig = Config_0_9_0.load( fileConfig_0_9_0 );
            
            oldConfig.save();
            
            fileConfig_0_9_0.renameTo( new File( System.getProperty( "user.home") + "/.kheetun/kheetun.xml.deprecated" ) );
        }
        
        Config config = new Config();
        
        File configDirectory = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d" );
        
        if ( configDirectory.exists() ) {
            
            config.properties.load( new FileInputStream( new File ( System.getProperty( "user.home") + "/.kheetun/kheetun.conf" ) ) );
            
            JAXBContext context = JAXBContext.newInstance( Profile.class );
            Unmarshaller u = context.createUnmarshaller();
            
            for ( File profileFile : configDirectory.listFiles() ) {
                
                Profile profile = (Profile)u.unmarshal( profileFile );
                
                config.profiles.add( profile );
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
