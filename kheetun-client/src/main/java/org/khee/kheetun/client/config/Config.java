package org.khee.kheetun.client.config;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.verify.VerifierFactory;

public class Config implements Serializable {
    
    public static final long serialVersionUID = 77L;
    
    private static  Logger              logger      = LogManager.getLogger( "kheetun" );
    
    private         ArrayList<Profile>      profiles        = new ArrayList<Profile>();
    private         ArrayList<String>       errors          = new ArrayList<String>();
    private         HashMap<String, String> localBinds      = new HashMap<String, String>(); 
    private         HashMap<String, String> remoteBinds     = new HashMap<String, String>();

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
    
    public ArrayList<String> getErrors() {
        return errors;
    }

    public void addError( String error ) {
        this.errors.add( error );
    }
    
    public int hashCode() {
        
        return new HashCodeBuilder( 13, 37 )
            .append( this.getProfiles().hashCode() )
            .hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if ( ! ( obj instanceof Config ) ) {
            return false;
        }
        
        Config compare = (Config)obj;
        
        return this.hashCode() == compare.hashCode();
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
                    config.validateProfile( profile );
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
                
                if ( profile.getErrors().isEmpty() ) {
                
                    String filename = profile.getConfigFile() != null ? profile.getConfigFile().getName() : profile.getName().toLowerCase() + ".xml";
                    
                    File file = new File( System.getProperty( "user.home") + "/.kheetun/kheetun.d/" + filename );
                
                    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
                    m.marshal( profile, file );
                }
            }
            
        } catch ( JAXBException e ) {

            logger.error( e.getMessage() );
            
        } 
    }
    
    public void validateProfile( Profile profile ) {
        
        for ( Tunnel tunnel : profile.getTunnels() ) {
            
            if ( ! VerifierFactory.getAliasVerifier().verify( tunnel.getAlias() ) ) {
                
                profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid alias '" + tunnel.getAlias() + "'" );
            }
            
            if ( ! VerifierFactory.getHostnameVerifier().verify( tunnel.getHostname() ) ) {
                
                profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid hostname '" + tunnel.getHostname() + "'" );
            }
            
            if ( tunnel.getSshKey() != null ) {
                if ( ! VerifierFactory.getSshKeyVerifier().verify( tunnel.getSshKey().getAbsolutePath() ) ) {
                    
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid SSH key '" + tunnel.getSshKey().getAbsolutePath() + "'" );
                }
            }
            
            if ( ! VerifierFactory.getUserVerifier().verify( tunnel.getUser() ) ) {
                
                profile.addError( "Tunnel '" + tunnel.getAlias() +"': invalid user '" + tunnel.getUser() + "'" );
            }
            
            int f = 0;
            
            for ( Forward forward : tunnel.getForwards() ) {
                
                f++;
                
                if ( ! VerifierFactory.getPortVerifier().verify( forward.getBindPort() ) ) {
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid bind port '" + forward.getBindPort() + "'" );
                }

                if ( ! VerifierFactory.getPortVerifier().verify( forward.getForwardedPort() ) ) {
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid forwarded port '" + forward.getBindPort() + "'" );
                }
                
                if ( ! VerifierFactory.getHostnameVerifier().verify( forward.getForwardedHost() ) ) {
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid forwarded host '" + forward.getForwardedHost() + "'" );
                }
                
                String forwardId = profile.getConfigFile().getName() + ": " + tunnel.getAlias() + ", Forward " + f;
                        
                if ( forward.getType().equals( Forward.LOCAL ) ) {
                    
                    String localBind = forward.getBindIp() + ":" + forward.getBindPort();
                    
                    if ( localBinds.containsKey( localBind ) ) {
                        
                        profile.addError( forwardId + ": duplicate bind: " + localBind + ", already used in " + localBinds.get( localBind )  );
                        
                    } else {
    
                        localBinds.put( localBind, forwardId );
                    }
                    
                } else {
                    
                    String remoteBind = tunnel.getHostname() + ":" + forward.getBindIp() + ":" + forward.getBindPort();
                    
                    if ( remoteBinds.containsKey( remoteBind ) ) {
                        
                        profile.addError( forwardId + ": duplicate bind: '" + remoteBind + ", already used in " + remoteBinds.get( remoteBind ) );
                        
                    } else {
    
                        remoteBinds.put( remoteBind, forwardId );
                    }
                }
                
                if ( ! VerifierFactory.getIpAddressVerifier().verify( forward.getBindIp() ) ) {
                    
                    profile.addError( "Tunnel '" + tunnel.getAlias() +"', Forward " + f + ": invalid bind IP '" + forward.getBindIp() + "'" );
                }
            }
        }
        
    }
    
    public void loopTunnels( boolean activeOnly, TunnelLoop loop ) {
        
        for ( Profile profile : this.profiles ) {
            
            if ( activeOnly && ( ! profile.isActive() || ! profile.getErrors().isEmpty() ) ) {
                
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


