package org.khee.kheetun.client.config;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config implements Serializable {
    
    public static final long serialVersionUID = 77L;
    
    private static  Logger              logger      = LogManager.getLogger( "kheetun" );
    
    private         ArrayList<Profile>  profiles    = new ArrayList<Profile>();
    private         ArrayList<String>   errors      = new ArrayList<String>();

    public class TunnelLoop {
        
        public void execute( Tunnel tunnel ) { };
    }
    
    public Config() { 
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
        
        return true;
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
        
        return true;
    }
    
    
    public static Config load() {
        
        Config config = new Config();
        
        File configDirectory = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d" );
        
        if ( configDirectory.exists() ) {
            
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
                    
                    profile.setConfigFile( profileFile );
                    profile.setModified( profileFile.lastModified() );
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
            
            JAXBContext context = JAXBContext.newInstance( Profile.class );
            Marshaller m = context.createMarshaller();
            
            for ( Profile profile : this.profiles ) {
                
                String filename = profile.getConfigFile() != null ? profile.getConfigFile().getName() : profile.getName().toLowerCase() + ".xml";
                
                File file = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d/" + filename );
            
                m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
                m.marshal( profile, file );
            }
            
        } catch ( JAXBException e ) {

            logger.error( e.getMessage() );
            
        } 
    }
    
    public void loopTunnels( boolean activeOnly, TunnelLoop loop ) {
        
        for ( Profile profile : this.profiles ) {
            
            if ( activeOnly && ! profile.isActive() ) {
                
                continue;
            }
            
            for ( Tunnel tunnel : profile.getTunnels() ) {
                
                loop.execute( tunnel );
            }
        }
    }
    
    @Override
    public String toString() {
        
        String s = "Config[";
        
        StringBuilder sb = new StringBuilder();
        
        for ( Profile profile : this.profiles ) {
            sb.append( profile );
            sb.append( "," );
        }
        
        s += "Profiles=" + sb.toString();
        s += "]";
        
        return s;
    }
    
}


