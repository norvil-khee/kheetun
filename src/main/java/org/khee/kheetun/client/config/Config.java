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
import org.khee.kheetun.client.verify.VerifierAlias;
import org.khee.kheetun.client.verify.VerifierHostname;
import org.khee.kheetun.client.verify.VerifierIpAddress;
import org.khee.kheetun.client.verify.VerifierPort;
import org.khee.kheetun.client.verify.VerifierSshKey;
import org.khee.kheetun.client.verify.VerifierUser;

public class Config implements Serializable {
    
    public static final long serialVersionUID = 77L;
    
    private static  Logger                      logger              = LogManager.getLogger( "kheetun" );
    
    public static final File                    CONFIG_DIRECTORY    = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.d" );
    
    private         ArrayList<Profile>          profiles            = new ArrayList<Profile>();
    private         ArrayList<String>           errors              = new ArrayList<String>();
    
    public class TunnelLoop {
        
        public void execute( Tunnel tunnel ) { };
    }
    
    public Config() { 
    }
    
    public Config( Config source ) {
        
        for ( Profile profile : source.profiles ) {
            
            this.profiles.add( new Profile( profile ) );
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
    
    public void removeProfileById( Integer id ) {
        
        int index = 0;
        
        while( index < this.profiles.size() && this.profiles.get( index ).getId() != id ) {
            
            index++;
        }
        
        if ( index < this.profiles.size() ) {
            
            this.profiles.remove( index );
        }
    }    
    
    public ArrayList<String> getErrors() {
        return errors;
    }

    public void addError( String error ) {
        this.errors.add( error );
    }
    
    public int hashCodeMeta() {
        
        HashCodeBuilder h = new HashCodeBuilder( 13, 33 );
        
        for ( Profile profile : this.getProfiles() ) {
           h.append( profile.hashCodeMeta() );
        }
        
        return h.toHashCode();
    }
    
    public boolean equalsMeta(Object obj) {
        
        if ( ! ( obj instanceof Config ) ) {
            return false;
        }
        
        Config compare = (Config)obj;
        
        return this.hashCodeMeta() == compare.hashCodeMeta();
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
                        profile.addError( "configFile", error );
                    }
                    
                    profile.setConfigFile( profileFile );
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
        
        if ( ! Config.CONFIG_DIRECTORY.exists() ) {
            Config.CONFIG_DIRECTORY.mkdir();
        }
        
        /*
         * backup all previous config files
         */
        File[] files = Config.CONFIG_DIRECTORY.listFiles();
        
        for ( File file : files ) {
            
            if ( ! file.getAbsolutePath().matches( ".*\\.xml$" ) ) {
                continue;
            }
            
            file.renameTo( new File( file.getAbsolutePath() + ".bak" ) );
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
    
    public void validate() {
        
        for ( Profile profile : this.profiles ) {
            
            this.validateProfile( profile );
        }
    }
    
    public ArrayList<String> getAllErrors() {
        
        ArrayList<String> errors = new ArrayList<String>();
        
        for ( Profile profile : this.profiles ) {
            
            String profileName = ( profile.getName() == null || profile.getName().length() == 0 ? "[no name]" : profile.getName() );
            
            for ( String error : profile.getReadableErrorList() ) {
                
                errors.add( "Profile " +  profileName + ": " + error );
            }
            
            for ( Tunnel tunnel : profile.getTunnels() ) {
                
                String tunnelName = ( tunnel.getAlias() == null || tunnel.getAlias().length() == 0 ? "[no alias]" : tunnel.getAlias() );
                
                for ( String error : tunnel.getReadableErrorList() ) {
                    
                    errors.add( "Profile " + profileName + ", Tunnel " + tunnelName + ": " + error );
                }
                
                int f = 0;
                
                for ( Forward forward : tunnel.getForwards() ) {
                    
                    f++;
                    
                    for ( String error : forward.getReadableErrorList() ) {
                        
                        errors.add( "Profile " + profileName + ", Tunnel " + tunnelName + ", Forward " + f + ": " + error );
                    }
                }
            }
            
        }
        
        return errors;
    }
    
    public void validateProfile( Profile profile ) {
        
        HashMap<String, String> localBinds      = new HashMap<String, String>(); 
        HashMap<String, String> remoteBinds     = new HashMap<String, String>();
        
        profile.clearErrors();
        
        profile.addError( "name", VerifierAlias.getInstance().verify( profile.getName() ));
//        profile.addError( "configFile", VerifierConfigFile.getInstance().verify( profile.getConfigFile() ) );
        
        for ( Tunnel tunnel : profile.getTunnels() ) {
            
            tunnel.clearErrors();
            
            tunnel.addError( "alias",       VerifierAlias.getInstance().verify( tunnel.getAlias() ) );
            tunnel.addError( "user",        VerifierUser.getInstance().verify( tunnel.getUser() ) );
            tunnel.addError( "hostname",    VerifierHostname.getInstance().verify( tunnel.getHostname() ) );
            tunnel.addError( "port",        VerifierPort.getInstance().verify( tunnel.getPort() ) );
            tunnel.addError( "sshKey",      VerifierSshKey.getInstance().verify( tunnel.getSshKey() ) );
            
            int f = 0;
            
            for ( Forward forward : tunnel.getForwards() ) {
                
                f++;
                
                forward.clearErrors();

                forward.addError( "bindIp",         VerifierIpAddress.getInstance().verify( forward.getBindIp() ) );
                forward.addError( "bindPort",       VerifierPort.getInstance().verify( forward.getBindPort() ) );

                forward.addError( "forwardedHost",  VerifierHostname.getInstance().verify( forward.getForwardedHost() ) );
                forward.addError( "forwardedPort",  VerifierPort.getInstance().verify( forward.getForwardedPort() ) );
                
                String forwardId = profile.getName() + ": " + tunnel.getAlias() + ", Forward " + f;
                        
                if ( forward.getType().equals( Forward.LOCAL ) ) {
                    
                    String localBind = forward.getBindIp() + ":" + forward.getBindPort();
                    
                    if ( localBinds.containsKey( localBind ) ) {
                        
                        forward.addError( "bindIp", forwardId + ": duplicate bind: " + localBind + ", already used in " + localBinds.get( localBind )  );
                        forward.addError( "bindPort", forwardId + ": duplicate bind: " + localBind + ", already used in " + localBinds.get( localBind )  );
                        
                    } else {
    
                        localBinds.put( localBind, forwardId );
                    }
                    
                } else {
                    
                    String remoteBind = tunnel.getHostname() + ":" + forward.getBindIp() + ":" + forward.getBindPort();
                    
                    if ( remoteBinds.containsKey( remoteBind ) ) {
                        
                        forward.addError( "bindIp", forwardId + ": duplicate bind: '" + remoteBind + ", already used in " + remoteBinds.get( remoteBind ) );
                        forward.addError( "bindPort", forwardId + ": duplicate bind: '" + remoteBind + ", already used in " + remoteBinds.get( remoteBind ) );
                        
                    } else {
    
                        remoteBinds.put( remoteBind, forwardId );
                    }
                }
            }
        }
    }
    
    public String getUniqueProfileName() {
        
        ArrayList<String> profileNames = new ArrayList<String>();
        
        for ( Profile profile : this.profiles ) {
            profileNames.add( profile.getName() );
        }
        
        int number = 1;
        
        while ( profileNames.contains( "Profile" + number ) ) {
            number++;
        }
        
        return "Profile" + number;
    }
    
    public String getUniqueTunnelName( Profile profile ) {
        
        ArrayList<String> tunnelNames = new ArrayList<String>();
        
        for ( Tunnel tunnel : profile.getTunnels() ) {
            tunnelNames.add( tunnel.getAlias() );
        }
        
        int number = 1;
        
        while ( tunnelNames.contains( "Tunnel" + number ) ) {
            number++;
        }
        
        return "Tunnel" + number;
    }
    
    public ArrayList<Profile> getAllProfiles() {
        
        return this.profiles;
    }
    
    public ArrayList<Tunnel> getAllTunnels() {
        
        ArrayList<Tunnel> tunnels = new ArrayList<Tunnel>();
        
        for( Profile profile : this.profiles ) {
            
            tunnels.addAll( profile.getTunnels() );
        }
        
        return tunnels;
    }
    
    public ArrayList<Forward> getAllForwards() {
        
        ArrayList<Forward> forwards = new ArrayList<Forward>();
        
        for ( Tunnel tunnel : this.getAllTunnels() ) {
            
            forwards.addAll( tunnel.getForwards() );
        }
        
        return forwards;
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


