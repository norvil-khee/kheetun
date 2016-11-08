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
import org.khee.kheetun.client.verify.VerifierFactory;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType( propOrder={"alias","user","hostname","sshKeyString","autostart","forwards"} )
public class Tunnel implements Serializable {
    
    public static final long serialVersionUID = 42;
    
    private String              alias;
    private String              user;
    private String              hostname;
    private File                sshKey;
    private String              sshKeyString;
    private String              passPhrase;
    private String              sshAgentSocket  = System.getenv( "SSH_AUTH_SOCK" );
    private ArrayList<Forward>  forwards;
    private Boolean             autostart       = false;
    private boolean             restart         = false;
    private int                 failures        = 0;
    private int                 maxFailures     = 3;
    
    public Tunnel() {
        forwards    = new ArrayList<Forward>();
        
        alias           = "";
        user            = "";
        hostname        = "";
        sshKeyString    = "";
        autostart       = false;
        restart         = false;
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

    public String getPassPhrase() {
        return passPhrase;
    }
    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }
    
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
    
    public Boolean getRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }
    
    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }
    
    @XmlAttribute(required=false)
    public int getMaxFailures() {
        return maxFailures;
    }

    public void setMaxFailures(int maxFailures) {
        this.maxFailures = maxFailures;
    }

    public String getConnectionString() {
        return this.getUser() + "@" + this.getHostname();
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
    
    public int hashCode() {
        
        return new HashCodeBuilder( 13, 37 )
            .append( this.getUser() )
            .append( this.getHostname() )
            .append( this.getForwards().hashCode() )
            .hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if ( ! ( obj instanceof Tunnel ) ) {
            return false;
        }
        
        Tunnel compare = (Tunnel)obj;
        
        return this.hashCode() == compare.hashCode();
    }
    
    @Override
    public String toString() {
        
        String s = "Tunnel[Alias=" + this.alias + " Autostart=" + this.autostart + " Restart=" + this.restart + " ";
        
        StringBuilder sb = new StringBuilder();
        
        for ( Forward forward : this.forwards ) {
            sb.append( forward );
            sb.append( "," );
        }
        
        s += "Forwards=" + sb.toString();
        s += "]";
        
        return s;
    }

}
