package org.khee.kheetun.client.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.khee.kheetun.client.verify.VerifierFactory;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType( propOrder={"type","bindIp","bindPort","forwardedHost","forwardedPort","hostsEntry","comment"} )
public class Forward implements Serializable {
    
    public static final long serialVersionUID = 42;

    public static final String REMOTE = "remote";
    public static final String LOCAL  = "local";
    
    private String type   = LOCAL;
    
    private String bindIp = "127.0.0.1";
    
    private Integer bindPort = 0;
    
    private String forwardedHost = "";
    
    private Integer forwardedPort = 0;
    
    private Boolean hostsEntry = true;

    private String comment = "";
    
    private Boolean active;
    
    private Object delete;    
    

    public Forward() {
    }
       
    public Forward( Forward source ) {
        
        this.type           = source.type;
        setBindIp( source.bindIp );
        this.bindPort       = new Integer( source.bindPort );
        this.forwardedHost  = source.forwardedHost;
        this.forwardedPort  = new Integer( source.forwardedPort );
        this.comment        = source.comment;
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

    @XmlTransient
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    @XmlTransient
    public Object getDelete() {
        return delete;
    }
    public void setDelete(Object delete) {
        this.delete = delete;
    }
    
    public boolean isValid() {
        
        return
            VerifierFactory.getIpAddressVerifier().verify( this.getBindIp() )
         && VerifierFactory.getPortVerifier().verify( this.getBindPort() )
         && VerifierFactory.getHostnameVerifier().verify( this.getForwardedHost() )
         && VerifierFactory.getPortVerifier().verify( this.getForwardedPort() );
    }
    
    
    @Override
    public boolean equals(Object obj) {
        
        return 
            this.getType().equals(          ((Forward)obj).getType()            ) &&
            this.getBindIp().equals(        ((Forward)obj).getBindIp()          ) &&
            this.getBindPort().equals(      ((Forward)obj).getBindPort()        ) &&
            this.getForwardedHost().equals( ((Forward)obj).getForwardedHost()   ) &&
            this.getForwardedPort().equals( ((Forward)obj).getForwardedPort()   ) &&
            this.getHostsEntry().equals(    ((Forward)obj).getHostsEntry()      ) &&
            this.getComment().equals(       ((Forward)obj).getComment()         );
    };
    
    


    
}
