package org.khee.kheetun.client.config;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.khee.kheetun.client.gui.TableModelAttribute;
import org.khee.kheetun.client.gui.TableModelAttributeOrder;
import org.khee.kheetun.client.verify.VerifierFactory;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType( propOrder={"alias","user","hostname","sshKeyString","autostart","forwards"} )
@TableModelAttributeOrder( order={"active","alias","autostart","user","hostname","sshKey", "delete"} )
public class Tunnel implements Serializable {
    
    public static final long serialVersionUID = 42;
    
    @TableModelAttribute( name="Alias" )
    protected String alias;
    
    @TableModelAttribute( name="User" )
    private String user;
    
    @TableModelAttribute( name="Host" )
    private String hostname;
    
    @TableModelAttribute( name="SSH Key" )
    private File sshKey;
    
    private String sshKeyString;
    private String passPhrase;
    private String sshAgentSocket = System.getenv( "SSH_AUTH_SOCK" );
    
    private Integer ping;
    
    @TableModelAttribute( name="Status", editable=false )
    private Boolean active   = false;
    
    @TableModelAttribute( name="Delete" )
    private Object delete    = null;
    
    private ArrayList<Forward> forwards;

//    @TableModelAttribute( name="Autostart" )
    private Boolean autostart;
    
    public Tunnel() {
        forwards    = new ArrayList<Forward>();
        
        alias           = "";
        user            = "";
        hostname        = "";
        sshKeyString    = "";
        autostart       = false;
    }
    
    public Tunnel( Tunnel source ) {
        
        this.forwards   = new ArrayList<Forward>();
        
        this.alias          = source.alias;
        this.user           = source.user;
        this.hostname       = source.hostname;
        this.autostart      = source.autostart;
        this.sshKeyString   = source.sshKeyString;
        
        for( Forward forward : source.getForwards() ) {
            addForward( new Forward( forward ) );
        }
        
    }
    
    @XmlTransient
    public String getSignature() {
        
        String signature = getUser() + "@" + getHostname();
        
        for( Forward forward : getForwards() ) {
            
            signature += "+" + forward.getSignature();
        }
        
        return signature;
    }
    
    @XmlAttribute
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    @XmlAttribute
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    
    @XmlAttribute
    public String getHostname() {
        return hostname;
    }    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    @XmlAttribute
    public File getSshKey() {
        return sshKey;
    }
    public void setSshKey(File sshKey) {
        
        if ( sshKey != null ) {
            this.sshKeyString = sshKey.getAbsolutePath();
        } else {
            this.sshKeyString = "";
        }
        this.sshKey         = sshKey;
    }    
    
    @XmlAttribute( name="sshKey" )
    public String getSshKeyString() {
        return sshKeyString;
    }
    public void setSshKeyString(String sshKeyString) {
        
        if ( sshKeyString.length() > 0 ) {
            this.sshKey = new File( sshKeyString );
        } else {
            this.sshKey = null;
        }
        this.sshKeyString = sshKeyString;
    }

    @XmlTransient
    public String getPassPhrase() {
        return passPhrase;
    }
    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }
    
    @XmlTransient
    public String getSshAgentSocket() {
        return sshAgentSocket;
    }
    public void setSshAgentSocket(String sshAgentSocket) {
        this.sshAgentSocket = sshAgentSocket;
    }

    @XmlElement( name="forward" )
    public ArrayList<Forward> getForwards() {
        return forwards;
    }
    public void setForwards(ArrayList<Forward> forwards) {
        this.forwards = forwards;
    }
    
    public void addForward( Forward forward ) {
        forwards.add( forward );
    }
    
    @XmlAttribute
    public Boolean getAutostart() {
        return autostart;
    }
    public void setAutostart(Boolean autostart) {
        this.autostart = autostart;
    }
    
    public String getConnectionString() {
        return this.getUser() + "@" + this.getHostname();
    }
    
    public Integer getPing() {
        return ping;
    }
    public void setPing(Integer ping) {
        this.ping = ping;
    }

    @XmlTransient
    public Object getDelete() {
        return delete;
    }

    public void setDelete(Object delete) {
        this.delete = delete;
    }

    @XmlTransient
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public boolean isValid() {
        
        if ( this.getForwards().size() == 0 ) {
            return false;
        }
        
        for ( Forward forward : this.getForwards() ) {
            if ( ! forward.isValid() ) {
                return false;
            }
        }
        
        return
            VerifierFactory.getAliasVerifier().verify( this.getAlias() )
         && VerifierFactory.getUserVerifier().verify( this.getUser() )
         && VerifierFactory.getHostnameVerifier().verify( this.getHostname() )
         && VerifierFactory.getSshKeyVerifier().verify( this.getSshKeyString() );
       
    }
    
    @Override
    public boolean equals(Object obj) {
        
        Tunnel compare = (Tunnel)obj;
        
        ArrayList<Forward> forwards = this.getForwards();
        ArrayList<Forward> forwardsCompare = compare.getForwards();
        
        // not the same amount of forwards? not equal!
        //
        if ( forwards.size() != forwardsCompare.size() ) {
            return false;
        }
        
        // forwards differ? not equal!
        //
        for ( int index = 0 ; index < forwards.size() ; index++ ) {
            
            if ( ! forwards.get( index ).equals( forwardsCompare.get( index )) ) {
                return false;
            }
        }
        
        // some of the attributes differ? not equal!
        //
        return 
                this.getAutostart().equals( compare.getAutostart() )
             && this.getAlias().equals( compare.getAlias() ) 
             && this.getHostname().equals( compare.getHostname() )
             && this.getUser().equals( compare.getUser() )
             && this.getSshKeyString().equals( compare.getSshKeyString() );
    }
    

}
