package org.khee.kheetun.client.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType( propOrder={"name","baseBindIp","tunnels"} )
@XmlRootElement
public class Profile {
    
    private String                  name;
    private ArrayList<Tunnel>       tunnels;
    private String                  baseBindIp;
    private ArrayList<String>       errors       = new ArrayList<String>();
    private String                  configFile;
    private Long                    modified;
    private Boolean                 active;
    
    public Profile() {
        tunnels = new ArrayList<Tunnel>();
    }
    
    @XmlAttribute
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @XmlAttribute
    public String getBaseBindIp() {
        return baseBindIp;
    }
    public void setBaseBindIp(String baseBindIp) {
        this.baseBindIp = baseBindIp;
    }

    @XmlElement( name="tunnel" )
    public ArrayList<Tunnel> getTunnels() {
        return tunnels;
    }
    public void setTunnels(ArrayList<Tunnel> tunnels) {
        this.tunnels = tunnels;
    }
    
    public void addTunnel( Tunnel tunnel ) {
        tunnels.add( tunnel );
    }
    
    public ArrayList<String> getErrors() {
        return errors;
    }

    public void addError( String error ) {
        this.errors.add( error );
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }
    
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Long getModified() {
        return modified;
    }

    public void setModified( Long modified ) {
        this.modified = modified;
    }

    public boolean isValid() {
        
        for( Tunnel tunnel : getTunnels() ) {
            
            if ( ! tunnel.isValid() ) {
                return false;
            }
        }
        return true; 
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if ( obj == null ) {
            return false;
        }
        
        Profile compare = (Profile)obj;
        
        ArrayList<Tunnel> tunnels = this.getTunnels();
        ArrayList<Tunnel> tunnelsCompare = compare.getTunnels();
        
        // not the same amount of tunnels? not equal!
        //
        if ( tunnels.size() != tunnelsCompare.size() ) {
            return false;
        }
        
        // tunnels differ? not equal!
        //
        for ( int index = 0 ; index < tunnels.size() ; index++ ) {
            
            if ( ! tunnels.get( index ).equals( tunnelsCompare.get( index )) ) {
                return false;
            }
        }
        
        return ( 
                this.getName().equals( compare.getName() )
           && ( ( this.getBaseBindIp() == null && compare.getBaseBindIp() == null ) || this.getBaseBindIp().equals( compare.getBaseBindIp() ) ) 
        );
    }

}
