package org.khee.kheetun.client.compat;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Profile;

@XmlRootElement(name="config")
@XmlType( propOrder={"port","profiles"} )
public class Config_0_9_0 {
    
    private ArrayList<Profile> profiles;
    private Integer port = 7779;
    
    public Config_0_9_0() {
        profiles = new ArrayList<Profile>();
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
    
    @XmlElement( name="port" )
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public static Config load( File file ) throws JAXBException {
        
        JAXBContext context = JAXBContext.newInstance( Config_0_9_0.class );
        Unmarshaller u = context.createUnmarshaller();
        
        Config_0_9_0 config_0_9_0 = (Config_0_9_0)u.unmarshal( file );
        
        Config config = new Config();
        
        config.setProfiles( config_0_9_0.getProfiles() );
        config.setPort( config_0_9_0.getPort() );

        return config;
    }
}
