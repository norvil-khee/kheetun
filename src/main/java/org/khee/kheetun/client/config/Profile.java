package org.khee.kheetun.client.config;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

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
public class Profile extends Base implements Serializable {
    
    public static final long serialVersionUID = 42;
    
    /**
     * essential Profile data (used by equals)
     */
    private ArrayList<Tunnel>       tunnels;
    
    /**
     * meta Profile data (used by metaEquals)
     */
    private String                  name;
    private String                  baseBindIp;
    private File                    configFile;
    private Boolean                 active       = true;
    
    public Profile() {
        this.name = "";
        tunnels = new ArrayList<Tunnel>();
    }
    
    public Profile( Profile source ) {
        
        tunnels = new ArrayList<Tunnel>();
        for ( Tunnel tunnel : source.tunnels ) {
            
            this.tunnels.add( new Tunnel( tunnel ) );
        }
        
        this.name       = source.name       == null ? null : new String( source.name );
        this.baseBindIp = source.baseBindIp == null ? null : new String( source.baseBindIp );
        this.configFile = source.configFile == null ? null : new File( source.configFile.getAbsolutePath() );
        this.active     = new Boolean( source.active );
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
    
    public void removeTunnelById( Integer id ) {
        
        int index = 0;
        
        while( index < this.tunnels.size() && this.tunnels.get( index ).getId() != id ) {
            
            index++;
        }
        
        if ( index < this.tunnels.size() ) {
            
            this.tunnels.remove( index );
        }
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

    public int hashCodeMeta() {
        
        HashCodeBuilder h = new HashCodeBuilder( 13, 39 )
            .append( this.getName() )
            .append( this.getBaseBindIp() )
            .append( this.getConfigFile() )
            .append( this.isActive() );
        
        for ( Tunnel tunnel : this.getTunnels() ) {
            h.append( tunnel.hashCodeMeta() );
        }
        
        return h.toHashCode();
    }
    
    public boolean equalsMeta(Object obj) {
        
        if ( ! ( obj instanceof Profile ) ) {
            return false;
        }
        
        Profile compare = (Profile)obj;
        
        return this.hashCodeMeta() == compare.hashCodeMeta();
    }  
    
    public int hashCode() {
        
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
