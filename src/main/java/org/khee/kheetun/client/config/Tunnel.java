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
import org.khee.kheetun.server.daemon.AutostartDaemon;
import org.khee.kheetun.server.daemon.PingChecker;

import com.jcraft.jsch.Session;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType( propOrder={"alias","user","hostname","sshKeyString","autostart","forwards"} )
public class Tunnel implements Serializable {
    
    public static final long serialVersionUID = 77L;
    
    public static final int     STATE_STARTING      = 100;
//    public static final int     STATE_STARTED       = 200;
    public static final int     STATE_RUNNING       = 250;
    public static final int     STATE_STOPPING      = 300;
    public static final int     STATE_STOPPED       = 400;
    
    public static final int     STATE_AUTO_OFF      = 950;
    public static final int     STATE_AUTO_ON       = 900;
    public static final int     STATE_AUTO_WAIT     = 500;
    public static final int     STATE_AUTO_AVAIL    = 600;
    
    private String              alias;
    private String              user;
    private String              hostname;
    private int                 port                = 22;
    private File                sshKey;
    private String              sshKeyString;
    private String              passPhrase;
    private String              sshAgentSocket      = System.getenv( "SSH_AUTH_SOCK" );
    private ArrayList<Forward>  forwards;
    private Boolean             autostart           = false;
    private boolean             restart             = false;
    private int                 state               = Tunnel.STATE_STOPPED;
    private int                 autoState           = Tunnel.STATE_AUTO_OFF;
    private String              error               = null;
    private int                 failures            = 0;
    private int                 maxFailures         = 3;
    private int                 ping                = 0;
    private int                 pingFailures        = 0;
    private String              info                = null;
    
    private transient Session             session             = null;
    private transient PingChecker         pingChecker         = null;
    private transient AutostartDaemon     autostartDaemon     = null;
    
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
    
    @XmlAttribute
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
        return this.autostart == null ? false : this.autostart;
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
    
    public  void increaseFailures() {
        this.failures++;
    }
    
    @XmlAttribute(required=false)
    public int getMaxFailures() {
        return maxFailures;
    }

    public void setMaxFailures(int maxFailures) {
        this.maxFailures = maxFailures;
    }
    
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getConnectionString() {
        return this.getUser() + "@" + this.getHostname();
    }
    
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
    
    public int getAutoState() {
        return autoState;
    }

    public void setAutoState(int autoState) {
        this.autoState = autoState;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }
    
    public int getPingFailures() {
        return pingFailures;
    }

    public void setPingFailures(int pingFailures) {
        this.pingFailures = pingFailures;
    }
    
    public void increasePingFailures() {
        this.pingFailures++;
    }
    
    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
    
    public PingChecker getPingChecker() {
        return pingChecker;
    }

    public void setPingChecker(PingChecker pingChecker) {
        
        this.pingChecker = pingChecker;
    }

    public AutostartDaemon getAutostartDaemon() {
        return autostartDaemon;
    }

    public void setAutostartDaemon(AutostartDaemon autostartDaemon) {
        
        this.autostartDaemon = autostartDaemon;
    }
    
    public int hashCode() {
        
        return new HashCodeBuilder( 13, 37 )
            .append( this.getUser() )
            .append( this.getHostname() )
            .append( this.getPort() )
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
        
        String s = "Tunnel[Alias=" + this.alias + ", State=" + this.state + ", Error=" + this.error + ", Ping=" + this.ping + "]";
        
        return s;
    }
    
    public String toDebugString() {
        
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
