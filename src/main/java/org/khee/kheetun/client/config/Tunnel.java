package org.khee.kheetun.client.config;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.server.daemon.AutostartDaemon;
import org.khee.kheetun.server.daemon.PingChecker;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
@XmlType( propOrder={"alias","user","hostname","sshKeyString","autostart","pingTimeout", "maxPingFailures","forwards"} )
public class Tunnel extends Base implements Serializable {
    
    public static final long serialVersionUID = 77L;
    
    private transient static Logger logger = LogManager.getLogger( "kheetun" );
    
    public static final int     STATE_STARTING      = 100;
    public static final int     STATE_RUNNING       = 200;
    public static final int     STATE_STOPPING      = 300;
    public static final int     STATE_STOPPED       = 400;
    
    public static final int     STATE_AUTO_OFF      = 950;
    public static final int     STATE_AUTO_ON       = 900;
    public static final int     STATE_AUTO_WAIT     = 500;
    public static final int     STATE_AUTO_AVAIL    = 600;
    

    /**
     * essential Tunnel data (used by equals)
     */
    private String              user;
    private String              hostname;
    private Integer             port                = 22;
    private ArrayList<Forward>  forwards;
    
    /**
     * meta Tunnel data (used by metaEquals)
     */
    private String              alias;
    private File                sshKey;
    private String              sshKeyString;
    private String              passPhrase;
    private String              sshAgentSocket      = System.getenv( "SSH_AUTH_SOCK" );
    private Boolean             autostart           = false;
    private Integer             pingTimeout         = 3000;
    private Integer             maxPingFailures     = 3;

    private boolean             restart             = false;
    private int                 state               = Tunnel.STATE_STOPPED;
    private int                 autoState           = Tunnel.STATE_AUTO_OFF;
    private String              error               = null;
    private int                 failures            = 0;
    private int                 ping                = 0;
    private int                 pingFailures        = 0;
    private String              info                = null;
    private Integer             maxFailures         = 3;
    
    private transient Session               session             = null;
    private transient PingChecker           pingChecker         = null;
    private transient AutostartDaemon       autostartDaemon     = null;
    private transient ChannelShell          shellChannel        = null;  
    private Semaphore           lock                = new Semaphore(1);
    
    public Tunnel() {
        forwards    = new ArrayList<Forward>();
        
        alias           = "";
        user            = "";
        hostname        = "";
        sshKeyString    = "";
        autostart       = false;
        restart         = false;
    }
    
    public Tunnel( Tunnel source ) {
        
        this.user           = source.user           == null ? null : new String( source.user );
        this.hostname       = source.hostname       == null ? null : new String( source.hostname );
        this.port           = source.port           == null ? null : new Integer( source.port );
        
        this.forwards       = new ArrayList<Forward>();
        for ( Forward forward : source.forwards ) {
            
            this.forwards.add( new Forward( forward ) );
        }
        
        this.alias          = source.alias          == null ? null : new String( source.alias );
        this.sshKey         = source.sshKey         == null ? null : new File( source.sshKey.getAbsolutePath() );
        this.sshKeyString   = source.sshKeyString   == null ? null : new String( source.sshKeyString );
        this.sshAgentSocket = source.sshAgentSocket == null ? null : new String( source.sshAgentSocket );
        this.passPhrase     = source.passPhrase     == null ? null : new String( source.passPhrase );
        this.autostart      = new Boolean( source.autostart );
        this.pingTimeout    = source.pingTimeout    == null ? null : new Integer( source.pingTimeout );
        this.maxPingFailures= source.maxPingFailures== null ? null : new Integer( source.maxPingFailures );
    }
    
    public void lock() {
        try {
            this.lock.acquire();
        } catch ( InterruptedException eInterrupted ) {
            logger.error( "Interrupted while trying to lock tunnel " + this.getAlias() );
        }
    }
    
    public void unlock() {
        this.lock.release();
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
    public Integer getPort() {
        return port;
    }

    public void setPort( Integer port ) {
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
    public Integer getMaxFailures() {
        return maxFailures;
    }

    public void setMaxFailures(Integer maxFailures) {
        this.maxFailures = maxFailures;
    }
    
    @XmlAttribute(required=false)
    public Integer getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(Integer pingTimeout) {
        this.pingTimeout = pingTimeout;
    }
    
    @XmlAttribute(required=false)
    public Integer getMaxPingFailures() {
        return maxPingFailures;
    }

    public void setMaxPingFailures(Integer maxPingFailures) {
        this.maxPingFailures = maxPingFailures;
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
    
    public ChannelShell getShellChannel() {
        return shellChannel;
    }

    public void setShellChannel(ChannelShell shellChannel) {
        this.shellChannel = shellChannel;
    }

    public int hashCodeMeta() {
        
        HashCodeBuilder h = new HashCodeBuilder( 13, 33 )
            .append( this.getAlias() )
            .append( this.getSshKey() )
            .append( this.getSshKeyString() )
            .append( this.getSshAgentSocket() )
            .append( this.getPassPhrase() )
            .append( this.getAutostart() )
            .append( this.getPingTimeout() )
            .append( this.getPingFailures() );
        
        for ( Forward forward : this.getForwards() ) {
           h.append( forward.hashCodeMeta() );
        }
        
        return h.toHashCode();
    }
    
    public boolean equalsMeta(Object obj) {
        
        if ( ! ( obj instanceof Tunnel ) ) {
            return false;
        }
        
        Tunnel compare = (Tunnel)obj;
        
        return this.hashCodeMeta() == compare.hashCodeMeta();
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
