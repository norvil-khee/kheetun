package org.khee.kheetun.client.config;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType( propOrder={"name","baseBindIp","tunnels"} )
@XmlRootElement
public class Profile implements Serializable {
    
    public static final long serialVersionUID = 42;
    
    private String                  name;
    private ArrayList<Tunnel>       tunnels;
    private String                  baseBindIp;
    private ArrayList<String>       errors       = new ArrayList<String>();
    private File                    configFile;
    private Long                    modified;
    private Boolean                 active       = true;
    
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

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile( File configFile ) {
        this.configFile = configFile;
    }
    
    @XmlAttribute
    public Boolean isActive() {
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

    public int hashCode() {
        
        Collections.sort( this.getTunnels(), new Comparator<Tunnel>() {
            @Override
            public int compare(Tunnel o1, Tunnel o2) {
                
                return o1.hashCode() > o2.hashCode() ? +1 : o1.hashCode() < o2.hashCode() ? -1 : 0;
            }
        });
        
        return new HashCodeBuilder( 13, 37 )
            .append( this.getTunnels().hashCode() )
            .hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if ( ! ( obj instanceof Profile ) ) {
            return false;
        }
        
        Profile compare = (Profile)obj;
        
        return this.hashCode() == compare.hashCode();
    }    
}
