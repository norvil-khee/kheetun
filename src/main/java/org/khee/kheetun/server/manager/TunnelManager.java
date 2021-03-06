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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
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
    
    private static HashMap<String, TunnelManager> instances = new HashMap<String, TunnelManager>();
    
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
    
    public synchronized void setConfig( Config config ) {
        
        if ( config == null ) {
            logger.debug( "Got null config - ignoring" );
            return;
        }
        
        if ( this.config.equals( config ) && this.config.equalsMeta( config ) ) {
            
            logger.info( "Config is unchanged for " + this.id );
            return;
        }
        
        logger.info( "Updating configuration for " + this.id );
        
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
        
        this.knownTunnels.clear();
        
        config.loopTunnels( true, config.new TunnelLoop() {
            
            @Override
            public void execute(Tunnel tunnel) {
                
                TunnelManager.this.knownTunnels.add( tunnel );
                
                if ( oldTunnels.contains( tunnel ) ) {
                    
                    Tunnel oldTunnel = oldTunnels.get( oldTunnels.indexOf( tunnel ) );
                    
                    tunnel.setSession( oldTunnel.getSession() );
                    tunnel.setRestart( oldTunnel.getRestart() );
                    tunnel.setPingFailures( oldTunnel.getPingFailures() );
                    tunnel.setFailures( oldTunnel.getFailures() );
                    tunnel.setState( oldTunnel.getState() );
                    tunnel.setAutoState( oldTunnel.getAutoState() );
                    tunnel.setShellChannel( oldTunnel.getShellChannel() );
                    
                    if ( oldTunnel.getPingChecker() != null ) {
                        oldTunnel.getPingChecker().stop();
                    }
                    if ( tunnel.getState() == Tunnel.STATE_RUNNING ) {
                        logger.info( "Setting new ping checker for old tunnel " + tunnel.getAlias() );
                        PingChecker pingChecker = new PingChecker( TunnelManager.this, tunnel );
                        pingChecker.start();
                    }
                    
                    if ( oldTunnel.getAutostartDaemon() != null ) {
                        oldTunnel.getAutostartDaemon().stop();
                    }
                    
                    if ( tunnel.getState() == Tunnel.STATE_STOPPED && ( tunnel.getAutostart() || tunnel.getRestart() ) ) {
                        logger.info( "Setting new autostart daemon for old tunnel " + tunnel.getAlias() );
                        TunnelManager.this.enableAutostart( tunnel );
                    }
                    
                    oldTunnels.remove( tunnel );
                }
            }
        } );
        
        // stop remaining stale tunnels
        // also remove stale ping- and autostart daemons
        //
        for ( Tunnel staleTunnel : oldTunnels ) {
            
            logger.info( "Stopping stale tunnel " + staleTunnel.getAlias() );
            this.stopTunnel( staleTunnel, true );
            
            if ( staleTunnel.getPingChecker() != null ) {
                staleTunnel.getPingChecker().stop();
            }
            if ( staleTunnel.getAutostartDaemon() != null ) {
                staleTunnel.getAutostartDaemon().stop();
            }
        }
        
        this.config = config;
        
        // setup autostarting, send initial status
        //
        this.config.loopTunnels( true, this.config.new TunnelLoop() {
            
            @Override
            public void execute(Tunnel tunnel) {
                
                if ( tunnel.getAutostart() ) { 
                    
                    if ( tunnel.getAutostartDaemon() == null ) {
                        logger.info( "Setup autostart checker for tunnel " + tunnel.getAlias() );
                        TunnelManager.this.enableAutostart( tunnel );
                    }
                    
                } else {
                    
                    if ( tunnel.getAutostartDaemon() != null ) {
                        
                        logger.info( "Removing autostart checker for tunnel " + tunnel.getAlias() );
                        TunnelManager.this.disableAutostart( tunnel );
                        
                    } else {
                        
                        server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
                    }
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
    
    private synchronized JSch setupPublickeyAuth( Tunnel tunnel ) {
        
        JSch jsch;
        
        try {
            
            jsch = new JSch();
            
            if ( tunnel.getPassPhrase() != null ) {
                
                jsch.addIdentity( tunnel.getSshKeyString(), tunnel.getPassPhrase().getBytes() );
                tunnel.setPassPhrase( null );
                
            } else {
                jsch.addIdentity( tunnel.getSshKeyString() );
            }
        } catch ( JSchException e ) {
            
            this.failTunnel( tunnel, "Failed to setup session using public key auth: " + e.getMessage() );
            return null;
        }
        
        return jsch;
    }

    private synchronized JSch setupSshAgentAuth( Tunnel tunnel ) {
        
        JSch jsch;
        
        try {
            
            jsch = new JSch();
            
            JNAUSocketFactory udsf = new JNAUSocketFactory();
            Connector con = new SSHSocketAgentConnector( udsf, tunnel.getSshAgentSocket() );

            IdentityRepository identities = new RemoteIdentityRepository( con );

            jsch.setIdentityRepository( identities );
            
        } catch ( AgentProxyException eAgent ) {
            
            this.failTunnel( tunnel, "Failed to setup session using ssh agent auth: " + eAgent.getMessage() );
            return null;
        }
        
        return jsch;
    }
    
    public void autostartAllTunnels() {
        
        this.config.loopTunnels( true, this.config.new TunnelLoop() {
        
            @Override
            public void execute(Tunnel tunnel) {
                
                if ( tunnel.getAutostart() ) {
                    TunnelManager.this.enableAutostart( tunnel );
                }
            }
        });
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
        
        logger.info( "Toggling tunnel " + tunnel.getAlias() + ", current state=" + tunnel.getState() );
        
        if ( tunnel.getState() == Tunnel.STATE_STOPPED ) {
            
            this.startTunnel( tunnel, true );
        
        } else if ( tunnel.getState() == Tunnel.STATE_RUNNING ) {
            
            this.stopTunnel( tunnel, true );
        
        } else {
            
            logger.info( "Tunnel " + tunnel.getAlias() + " is in transition, will not toggle" );
        }
    }
    
    public void stopTunnel( Tunnel tunnel, boolean manually ) {
        
        tunnel.lock();
        
        if ( tunnel.getState() != Tunnel.STATE_RUNNING ) {
            logger.info( "Tunnel " + tunnel.getAlias() + " is not started (state=" + tunnel.getState() + "), will not stop" );
            tunnel.unlock();
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
            
            try {
            
                for ( Forward forward : tunnel.getForwards() ) {
                    
                    logger.info( "Remove Port forwarding: " + forward.getSignature() );
                    
                    if ( forward.getType().equals( Forward.REMOTE ) ) {
                        
                        session.delPortForwardingR( forward.getBindIp(), forward.getBindPort() );
                    } else {
                        
                        session.delPortForwardingL( forward.getBindIp(), forward.getBindPort() );
                    }
                }
                
            } catch ( JSchException eJsch ) {
                
                logger.error( "Failed to remove port forwarding while stopping tunnel " + tunnel.getAlias() + ": " + eJsch.getMessage() );
            }
        
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
        tunnel.setState( Tunnel.STATE_STOPPED );
        
        if ( ! manually && ( tunnel.getAutostart() || tunnel.getRestart() ) ) {
            
            AutostartDaemon autostartDaemon = new AutostartDaemon( this, tunnel );
            autostartDaemon.start();
        }
        
        logger.info( "Stopped tunnel: " + tunnel.getAlias() );
        
        server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        tunnel.unlock();
    }
    
    public void startTunnel( Tunnel tunnel, boolean manually ) {
        
        tunnel.lock();

        if ( tunnel.getState() != Tunnel.STATE_STOPPED ) {
            logger.info( "Tunnel " + tunnel.getAlias() + " is not stopped (state=" + tunnel.getState() + "), will not start" );
            tunnel.unlock();
            return;
        }
        
        logger.info( "Starting tunnel: " + tunnel.getAlias() );
        
        tunnel.setError( null );
        tunnel.setState( Tunnel.STATE_STARTING );
        server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        tunnel.setSession( null );
        
        Session session = null;
        
        try {
            
            JSch jsch;
            
            if ( tunnel.getSshKeyString().length() > 0 ) {
                
                jsch = setupPublickeyAuth( tunnel );
            } else {
                jsch = setupSshAgentAuth( tunnel );
            }
            
            if ( jsch == null ) {
                
                tunnel.setState( Tunnel.STATE_STOPPED );
                server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
                tunnel.unlock();
                return;
            }
            
            session = jsch.getSession( tunnel.getUser(), tunnel.getHostname(), tunnel.getPort() );
            session.setConfig( "PreferredAuthentications", "publickey" );
            session.setConfig( "StrictHostKeyChecking", "no" );
            
            session.setTimeout( 10000 );
            session.connect();
            
            Channel channel = session.openChannel( "shell" );
            ChannelShell channelShell = (ChannelShell)channel;
            
            channelShell.setPty( false );
            channelShell.connect();
            
            tunnel.setShellChannel( channelShell );
            
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

            tunnel.unlock();
            this.failTunnel( tunnel, "SSH Session connection failed: " + eJSch.getMessage() );

            tunnel.setState( Tunnel.STATE_STOPPED );
            this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
            
            return;
        }
        
        tunnel.setFailures( 0 );
        tunnel.setSession( session );
        tunnel.setState( Tunnel.STATE_RUNNING );

        if ( manually ) {
            tunnel.setRestart( true );
            tunnel.setAutoState( Tunnel.STATE_AUTO_ON );
            this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        }
        
        PingChecker pingChecker = new PingChecker( this, tunnel );
        pingChecker.start();

        logger.info( "Started tunnel: " + tunnel.getAlias() );
        
        tunnel.setError( null );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        tunnel.unlock();
    }
    
    public void failTunnel( Tunnel tunnel, String error ) {
        
        tunnel.increaseFailures();
        tunnel.setError( error );
        logger.error( "Tunnel " + tunnel.getAlias() + " failed [" + tunnel.getFailures() + "/" + tunnel.getMaxFailures() +"]: " + error );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        if ( tunnel.getFailures() >= tunnel.getMaxFailures() ) {
            
            logger.error( "Stopping tunnel " + tunnel.getAlias() + ": max fail count reached" );
            if ( tunnel.getAutostart() || tunnel.getRestart() ) {
                this.disableAutostart( tunnel );
            }
            
            this.stopTunnel( tunnel, false );
        }
    }
    
    public void enableAutostart( Tunnel tunnel ) {
        
        tunnel.setRestart( true );
        tunnel.setFailures( 0 );
        tunnel.setError( null );
        tunnel.setAutoState( Tunnel.STATE_AUTO_ON );
        this.server.send( new Protocol( Protocol.TUNNEL, tunnel ) );
        
        if ( tunnel.getAutostartDaemon() == null && tunnel.getState() != Tunnel.STATE_RUNNING ) {
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
        
        if ( tunnel != null && tunnel.getState() == Tunnel.STATE_RUNNING ) {
        
            if ( tunnel.getPingFailures() >= tunnel.getMaxPingFailures() ) {
                
                this.failTunnel( tunnel, "Ping failure (" + tunnel.getMaxPingFailures() + " times above " + tunnel.getPingTimeout() + "ms)" );
                this.stopTunnel( tunnel, false );
                
            } else if ( ! tunnel.getSession().isConnected() ) {
                
                this.failTunnel( tunnel, "Ping failure (SSH session closed)" );
                this.stopTunnel( tunnel, false );
                
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


