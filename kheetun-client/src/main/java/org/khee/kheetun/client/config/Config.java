package org.khee.kheetun.client.config;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@XmlRootElement
@XmlType( propOrder={"port","profiles"} )
public class Config {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private ArrayList<Profile> profiles;
    private Integer port = 7779;
    
    public Config() {
        profiles = new ArrayList<Profile>();
    }
    
    public Config( Config source ) {
        
        this.profiles = new ArrayList<Profile>();
        this.port     = source.getPort();
        
        for ( Profile profile : source.getProfiles() ) {
            addProfile( new Profile( profile ) );
        }
    }
    
    @XmlElement( name="profile" )
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
    
    @XmlElement( name="port" )
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    @Override
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
    
    
    public static Config load( File file ) throws JAXBException {
        
        Config config = new Config();
        
        JAXBContext context = JAXBContext.newInstance( Config.class );
        Unmarshaller u = context.createUnmarshaller();
        
        config = (Config)u.unmarshal( file );

        return config;
    }
    
    
    public void save( File file ) {
        
        try {
            JAXBContext context = JAXBContext.newInstance( Config.class );
            Marshaller m = context.createMarshaller();

            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            m.marshal( this, file );
            
        } catch ( Exception e ) {
            
            logger.error( e.getMessage() );
        }        
    }
    
}
