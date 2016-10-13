package org.khee.kheetun.server.manager;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.comm.Protocol;
import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.server.SSHSocketAgentConnector;
import org.khee.kheetun.server.TunnelServer;
import org.khee.kheetun.server.daemon.PingDaemon;
import org.khee.kheetun.server.daemon.PingDaemonListener;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class TunnelManager implements PingDaemonListener {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    
    public static final long serialVersionUID = 42;
    
    private HashMap<String, Session> activeTunnels;
    private HashMap<String, PingDaemon> pingDaemons;
    private JSch jsch;
    private IdentityRepository identities;
    private TunnelServer server;

    
    public TunnelManager() {
        
        activeTunnels   = new HashMap<String, Session>();
        pingDaemons     = new HashMap<String, PingDaemon>();
        jsch            = new JSch();
    }
    
    
    public void setServer(TunnelServer server) {
        this.server = server;
    }
    
    @Override
    public void PingFailed( PingDaemon daemon, Tunnel tunnel, Session session ) {
        
        if ( ! activeTunnels.containsKey( tunnel.getSignature() ) ) {
            return;
        }
        
        stopTunnel( tunnel );
        server.send( new Protocol( Protocol.TUNNELSTOPPED, tunnel ) );
        server.send( new Protocol( Protocol.ERROR, tunnel, "Ping timeout" ) );
        daemon.stop();
    }
    
    @Override
    public void PingUpdate( Tunnel tunnel, long ping ) {
        
        if ( ! activeTunnels.containsKey( tunnel.getSignature() ) ) {
            return;
        }
        
        server.send( new Protocol( Protocol.PING, tunnel, ping ) );
    }
    
    private boolean setupPublickeyAuth( Tunnel tunnel ) {
        
        try {
            
            jsch.removeAllIdentity();
            
            if ( tunnel.getPassPhrase() != null ) {
                
                jsch.addIdentity( tunnel.getSshKeyString(), tunnel.getPassPhrase().getBytes() );
                
            } else {
                jsch.addIdentity( tunnel.getSshKeyString() );
            }
        } catch ( JSchException e ) {
            
            server.send( new Protocol( Protocol.ERROR, tunnel, "Failed to setup session using public key auth: " + e.getMessage() ) );
            return false;
        }
        
        return true;
    }

    private boolean setupSshAgentAuth( Tunnel tunnel ) {
        
        try {
            JNAUSocketFactory udsf = new JNAUSocketFactory();
            Connector con = new SSHSocketAgentConnector( udsf, tunnel.getSshAgentSocket() );

            identities = new RemoteIdentityRepository( con );

            jsch.setIdentityRepository( identities );
            
        } catch ( AgentProxyException eAgent ) {
            
            server.send( new Protocol( Protocol.ERROR, tunnel, "Failed to setup session using ssh agent auth: " + eAgent.getMessage() ) );
            return false;
        }
        
        return true;
    }
    
    public ArrayList<String> getActiveTunnels() {
        
        return new ArrayList<String>( activeTunnels.keySet() );
    }
    
    public boolean stopTunnel( Tunnel tunnel ) {

        if ( ! activeTunnels.containsKey( tunnel.getSignature() ) ) {
            return false;
        }

        Session session = activeTunnels.get( tunnel.getSignature() );
        
        if ( session != null ) {

            if ( session.isConnected() ) {
            
                session.disconnect();
            }
                
            ArrayList<Forward> hostEntries = new ArrayList<Forward>();
            
            for ( Forward forward : tunnel.getForwards() ) {
                
                if ( forward.getType().equals( Forward.LOCAL ) && forward.getHostsEntry() ) {
                    hostEntries.add( forward );
                }
            }
            
            if ( hostEntries.size() > 0 ) {
                
                HostsManager.removeForwards( hostEntries );
            }
        }
        
        logger.debug( "Removing tunnel from active tunnels: " + tunnel.getSignature() );
        
        activeTunnels.remove( tunnel.getSignature() );
        pingDaemons.get( tunnel.getSignature() ).stop();
        pingDaemons.remove( tunnel.getSignature() );
        
        return true;
    }
    
    public boolean startTunnel( Tunnel tunnel ) {
        
        /*
         * check if this tunnel is active already
         * and if so, if session is still okay
         */
        if ( activeTunnels.containsKey( tunnel.getSignature() ) ) {
            
            Session session = activeTunnels.get( tunnel.getSignature() );
            
            if ( session != null && session.isConnected() ) {
                
                server.send( new Protocol( Protocol.ERROR, tunnel, "Tunnel already active" ) );
                return false;
            }
        }
        
        Session session = null;
        
        try {
            
            if ( tunnel.getSshKeyString().length() > 0 ) {
                if ( ! setupPublickeyAuth( tunnel ) ) {
                    return false;
                }
            } else {
                if ( ! setupSshAgentAuth( tunnel ) ) {
                    return false;
                }
            }
            
            session = jsch.getSession( tunnel.getUser(), tunnel.getHostname(), 22 );
            session.setConfig( "PreferredAuthentications", "publickey" );
            session.setConfig( "StrictHostKeyChecking", "no" );
            
            if ( session.isConnected() ) {
                server.send( new Protocol( Protocol.ECHO, "Already open!" ) );
                return false;
            }
            
            session.setTimeout( 10000 );
            session.connect();
            tunnel.setPassPhrase( "" );
            
            PingDaemon pingd = new PingDaemon( tunnel, session );
            pingd.addPingDaemonListener( this );
            pingDaemons.put( tunnel.getSignature(), pingd );
            
            ArrayList<Forward> hostEntries = new ArrayList<Forward>();
            
            for ( Forward forward : tunnel.getForwards() ) {
                
                if ( forward.getType().equals( Forward.REMOTE ) ) {
                    
                    session.setPortForwardingR( forward.getBindIp(), forward.getBindPort(), forward.getForwardedHost(), forward.getForwardedPort() );
                } else {
                    
                    session.setPortForwardingL( forward.getBindIp(), forward.getBindPort(), forward.getForwardedHost(), forward.getForwardedPort() );
          
                    if ( forward.getHostsEntry() ) {
                        hostEntries.add( forward );
                    }
                }
            }
            
            
            if ( hostEntries.size() > 0 ) {
                
                HostsManager.addForwards( hostEntries );
            }
            
        } catch ( JSchException eJSch ) {
            
            server.send( new Protocol( Protocol.ERROR, tunnel, "SSH Session connection failed: " + eJSch.getMessage() ) );
            
            if ( session != null && session.isConnected() ) {
                
                session.disconnect();
            }
            
            return false;
            
        } 
        
        activeTunnels.put( tunnel.getSignature(), session );
        logger.info( "Started tunnel with signature: " + tunnel.getSignature() );
        
        return true;
    }
}


