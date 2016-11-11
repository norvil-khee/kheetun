package org.khee.kheetun.server.manager;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.server.SSHSocketAgentConnector;
import org.khee.kheetun.server.TunnelServer;
import org.khee.kheetun.server.comm.Protocol;
import org.khee.kheetun.server.daemon.AutostartDaemon;
import org.khee.kheetun.server.daemon.PingChecker;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class TunnelManager {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    
    public static final long serialVersionUID = 42;
    
    private static HashMap<String, TunnelManager> instances = new HashMap<String, TunnelManager>();
    
    private JSch                        jsch                = new JSch();
    private IdentityRepository          identities;
    private TunnelServer                server              = new TunnelServer();
    private Config                      config              = new Config();
    private String                      id;
    private ArrayList<Tunnel>           knownTunnels        = new ArrayList<Tunnel>();

    public TunnelManager( String id ) {

        this.id     = id;
    }
    
    public static TunnelManager get( String id ) {
        
        if ( ! TunnelManager.instances.containsKey( id ) ) {
            
            logger.info( "Creating new TunnelManager " + id );
            TunnelManager.instances.put( id, new TunnelManager( id ) );
            
        }
        
        return TunnelManager.instances.get( id );
    }
    
    public String getId() {
        
        return this.id;
    }
    
    public void setConfig( Config config ) {
        
        // merge configurations:
        //
        // run through current config and memorize all tunnels
        // if one of these tunnels is encountered in new config, update session and restart values
        // and delete from memorized list
        // remainder of memorized list are "stale" tunnels which will be stopped before new config is applied
        //
        ArrayList<Tunnel> oldTunnels = new ArrayList<Tunnel>();
        
        this.config.loopTunnels( false, this.config.new TunnelLoop() {
            
            @Override
            public void execute(Tunnel tunnel) {
                
                oldTunnels.add( tunnel );
            }
        } );
        
        knownTunnels.clear();
        
        config.loopTunnels( false, config.new TunnelLoop() {
            
            @Override
            public void execute(Tunnel tunnel) {
                
                TunnelManager.this.knownTunnels.add( tunnel );
                
                if ( oldTunnels.contains( tunnel ) ) {
                    
                    Tunnel oldTunnel = oldTunnels.get( oldTunnels.indexOf( tunnel ) );
                    
                    tunnel.setSession( oldTunnel.getSession() );
                    tunnel.setRestart( oldTunnel.getRestart() );
                    tunnel.setPingFailures( oldTunnel.getPingFailures() );
                    tunnel.setFailures( oldTunnel.getFailures() );
                    tunnel.setPingChecker( oldTunnel.getPingChecker() );
                    tunnel.setAutostartDaemon( oldTunnel.getAutostartDaemon() );
                    tunnel.setState( oldTunnel.getState() );
                    tunnel.setAutoState( oldTunnel.getAutoState() );
                    
                    oldTunnels.remove( tunnel );
                }
            }
        } );
        
        // stop remaining stale tunnels
        //
        for ( Tunnel staleTunnel : oldTunnels ) {
            
            logger.info( "Stopping stale tunnel " + staleTunnel.getAlias() );
            this.stopTunnel( staleTunnel, true );
        }
        
        this.config = config;
        
        // setup autostarting
        //
        this.config.loopTunnels( true, this.config.new TunnelLoop() {
            
            @Override
            public void execute(Tunnel tunnel) {
                
                if ( tunnel.getAutostart() && tunnel.getSession() == null && tunnel.getAutostartDaemon() == null ) { 
                    
                    logger.info( "Setup autostart checker for tunnel " + tunnel.getAlias() );
                    TunnelManager.this.enableAutostart( tunnel );
                }
            }
        } );
        
    }
    
    public Config getConfig() {
        
        return this.config;
    }
    
    public ArrayList<Tunnel> getKnownTunnels() {
        
        return this.knownTunnels;
    }
    
    public void setServer( TunnelServer server ) {
        
        this.server = server;
    }
    
    public TunnelServer getServer() {
        
        return this.server;
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
            
            this.failTunnel( tunnel, "Failed to setup session using public key auth: " + e.getMessage() );
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
            
            this.failTunnel( tunnel, "Failed to setup session using ssh agent auth: " + eAgent.getMessage() );
            return false;
        }
        
        return true;
    }
    
    public void stopAllTunnels() {
        
        this.config.loopTunnels( true, this.config.new TunnelLoop() {
        
            @Override
            public void execute(Tunnel tunnel) {
                
                TunnelManager.this.stopTunnel( tunnel, true );
            }
        });
    }
    
    public void toggleTunnel( Tunnel tunnel ) {
        
        if ( tunnel.getSession() == null ) {
            
            this.startTunnel( tunnel, true );
        } else {
            
            this.stopTunnel( tunnel, true );
        }
    }
    
    public void stopTunnel( Tunnel tunnel, boolean manually ) {
        
        if ( tunnel.getSession() == null ) {

            logger.debug( "Tunnel " + tunnel.getAlias() + " already stopped" );
            return;
        }
        
        if ( tunnel.getState() != Tunnel.STATE_RUNNING ) {
            logger.info( "Tunnel " + tunnel.getAlias() + " is not started, will not stop" );
            return;
        }
        
        // disable autostarting if called manually
        //
        if ( manually ) {
            
            this.disableAutostart( tunnel );
        }
        
        logger.info( "Stopping tunnel " + tunnel.getAlias() );
        tunnel.setState( Tunnel.STATE_STOPPING );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );

        Session session = tunnel.getSession();
        
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
        
        tunnel.setSession( null );
        
        if ( ! manually && ( tunnel.getAutostart() || tunnel.getRestart() ) ) {
            
            AutostartDaemon autostartDaemon = new AutostartDaemon( this, tunnel );
            autostartDaemon.start();
        }
        
        logger.info( "Stopped tunnel: " + tunnel.getAlias() );
        
        tunnel.setState( Tunnel.STATE_STOPPED );
        server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
    }
    
    public void startTunnel( Tunnel tunnel, boolean manually ) {

        if ( tunnel.getSession() != null && tunnel.getSession().isConnected() ) {
            
            logger.debug( "Tunnel " + tunnel.getAlias() + " already started" );
            return;
        }
        
        if ( tunnel.getState() != Tunnel.STATE_STOPPED ) {
            logger.info( "Tunnel " + tunnel.getAlias() + " is not stopped, will not start" );
            return;
        }
        
        // reset fail count if started manually, also reenable autostart
        //
        if ( manually ) {
            
            tunnel.setFailures( 0 );
        }
        
        logger.info( "Starting tunnel: " + tunnel.getAlias() );
        
        tunnel.setError( null );
        tunnel.setState( Tunnel.STATE_STARTING );
        server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        tunnel.setSession( null );
        
        Session session = null;
        
        try {
            
            if ( tunnel.getSshKeyString().length() > 0 ) {
                if ( ! setupPublickeyAuth( tunnel ) ) {
                    return;
                }
            } else {
                if ( ! setupSshAgentAuth( tunnel ) ) {
                    return;
                }
            }
            
            session = jsch.getSession( tunnel.getUser(), tunnel.getHostname(), tunnel.getPort() );
            session.setConfig( "PreferredAuthentications", "publickey" );
            session.setConfig( "StrictHostKeyChecking", "no" );
            
            session.setTimeout( 10000 );
            session.connect();
            
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
            
            if ( session != null && session.isConnected() ) {
                
                session.disconnect();
            }
            
            this.failTunnel( tunnel, "SSH Session connection failed: " + eJSch.getMessage() );

            tunnel.setState( Tunnel.STATE_STOPPED );
            this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
            
            return;
        }
        
        if ( manually ) {
            tunnel.setRestart( true );
            tunnel.setAutoState( Tunnel.STATE_AUTO_ON );
            this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        }
        
        tunnel.setSession( session );
        
        if ( tunnel.getPingChecker() == null ) {
            PingChecker pingChecker = new PingChecker( this, tunnel );
            pingChecker.start();
        }

        logger.info( "Started tunnel: " + tunnel.getAlias() );
        
        tunnel.setError( null );
        tunnel.setState( Tunnel.STATE_RUNNING );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
    }
    
    public void failTunnel( Tunnel tunnel, String error ) {
        
        tunnel.increaseFailures();
        tunnel.setError( error );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        if ( tunnel.getFailures() >= tunnel.getMaxFailures() ) {
            
            if ( tunnel.getAutostart() || tunnel.getRestart() ) {
                this.disableAutostart( tunnel );
            }
            
            this.stopTunnel( tunnel, false );
        }
    }
    
    public void enableAutostart( Tunnel tunnel ) {
        
        tunnel.setRestart( true );
        tunnel.setAutoState( Tunnel.STATE_AUTO_ON );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        if ( tunnel.getAutostartDaemon() == null ) {
            AutostartDaemon autostartDaemon = new AutostartDaemon( this, tunnel );
            autostartDaemon.start();
        }
    }
    
    public void disableAutostart( Tunnel tunnel ) {
        
        tunnel.setRestart( false );
        tunnel.setAutoState( Tunnel.STATE_AUTO_OFF );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
    }
    
    public void updatePing( Tunnel tunnel ) {
        
        if ( tunnel.getState() == Tunnel.STATE_RUNNING ) {
        
            if ( tunnel.getPingFailures() >= 3 ) {
                
                this.failTunnel( tunnel, "Ping failure" );
                
            } else {
                
                this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
            }
        }
    }
    
    public void updateAutostart( Tunnel tunnel ) {
        
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        if ( tunnel.getAutoState() == Tunnel.STATE_AUTO_AVAIL ) {
            
            this.startTunnel( tunnel, false );
        }
    }
}


