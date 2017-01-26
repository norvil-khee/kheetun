package org.khee.kheetun.client.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType( propOrder={"type","bindIp","bindPort","forwardedHost","forwardedPort","hostsEntry","comment"} )
public class Forward extends Base implements Serializable {
    
    public static final long serialVersionUID   = 42L;

    public static final String  REMOTE          = "remote";
    public static final String  LOCAL           = "local";

    /**
     * essential Forward data (used by equals)
     */
    private String              type            = LOCAL;
    private String              bindIp          = "127.0.0.1";
    private Integer             bindPort        = 0;
    private String              forwardedHost   = "";
    private Integer             forwardedPort   = 0;
    
    /**
     * meta Forward data (used by metaEquals)
     */
    private String              comment         = "";
    private Boolean             hostsEntry      = true;
    
    public Forward() {
    }
       
    public Forward( Forward source ) {
        
        this.type           = source.type;
        this.bindIp         = source.bindIp         == null ? null : new String( source.bindIp );
        this.bindPort       = source.bindPort       == null ? null : new Integer( source.bindPort );
        this.forwardedHost  = source.forwardedHost  == null ? null : new String( source.forwardedHost );
        this.forwardedPort  = source.forwardedPort  == null ? null : new Integer( source.forwardedPort );
        this.comment        = source.comment        == null ? null : new String( source.comment );
        this.hostsEntry     = new Boolean( source.hostsEntry );
    }
    
    @XmlTransient
    public String getSignature() {
        
        return getType() + ":" + getBindIp() + ":" + getBindPort() + ":" + getForwardedHost() + ":" + getForwardedPort();
    }
    
    @XmlAttribute
    public String getType() {
        return type;
    }
    public void setType(String type) {
        
        if ( type.equals( "L" ) || type.equals( "-L" ) || type.toLowerCase().equals( "local" ) ) {
            this.type = "local";
        } else if ( type.equals( "R" ) || type.equals( "-R" ) || type.toLowerCase().equals( "remote" ) ) {
            this.type = "remote";
        } else {
            this.type = "?";
        }
    }
    
    @XmlAttribute
    public String getBindIp() {
        if ( bindIp == null ) {
            return "127.0.0.1";
        }
        return bindIp;
    }
    public void setBindIp(String bindIp) {
        
        if ( bindIp == null ) {
            bindIp = "127.0.0.1";
        } else {
            this.bindIp = bindIp;
        }
    }
    
    
    @XmlAttribute( name="bindPort" )
    public Integer getBindPort() {
        return bindPort;
    }
    public void setBindPort(Integer bindPort) {
        this.bindPort = bindPort;
    }
    
    @XmlAttribute
    public String getForwardedHost() {
        return forwardedHost;
    }
    public void setForwardedHost(String forwardedHost) {
        this.forwardedHost = forwardedHost;
    }
    
    @XmlAttribute( name="forwardedPort" )
    public Integer getForwardedPort() {
        return forwardedPort;
    }
    public void setForwardedPort(Integer forwardedPort) {
        this.forwardedPort = forwardedPort;
    }
    
    @XmlAttribute
    public Boolean getHostsEntry() {
        return hostsEntry;
    }

    public void setHostsEntry(Boolean hostsEntry) {
        this.hostsEntry = hostsEntry;
    }

    @XmlAttribute
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public int hashCodeMeta() {
        
        return new HashCodeBuilder( 13, 39 )
            .append( this.getComment() )
            .append( this.getHostsEntry() )
            .toHashCode();
    }
    
    public boolean equalsMeta(Object obj) {
        
        if ( ! ( obj instanceof Forward ) ) {
            return false;
        }
        
        Forward compare = (Forward)obj;
        
        return this.hashCodeMeta() == compare.hashCodeMeta();
    }    
    
    @Override
    public int hashCode() {
        
        return new HashCodeBuilder( 13, 39 )
            .append( this.getType() )
            .append( this.getBindIp() )
            .append( this.getBindPort() )
            .append( this.getForwardedHost() )
            .append( this.getForwardedPort() )
            .toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if ( ! ( obj instanceof Forward ) ) {
            return false;
        }
        
        Forward compare = (Forward)obj;
        
        return this.hashCode() == compare.hashCode();
    }    
    
    @Override
    public String toString() {
        
        return "Forward[Type=" + this.type + " BindIP=" + this.bindIp + " BindPort=" + this.bindPort + " ForwardedHost=" + this.forwardedHost + " ForwardedPort=" + this.forwardedPort;
    }
}
