package org.khee.kheetun.server.daemon;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.Kheetun;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.server.manager.TunnelManager;

public class PingChecker implements Runnable {
    
    private static Logger       logger = LogManager.getLogger( "kheetund" );
    
    private Tunnel              tunnel;
    private TunnelManager       tunnelManager;
    private String              id;
    private boolean             running         = true;
    private BufferedReader      shellIn;
    private DataOutputStream    shellOut;
    private ExecutorService     executorService = Executors.newCachedThreadPool();
    private Callable<Integer>   callableGetPing;
    
    public PingChecker( TunnelManager tunnelManager, Tunnel tunnel ) {
        
        if ( tunnel.getPingChecker() != null ) {
            logger.info( "Restarting ping checker for tunnel "  +tunnel.getAlias() );
            tunnel.getPingChecker().stop();
        }
        
        this.tunnelManager  = tunnelManager;
        this.tunnel         = tunnel;
        this.id             = tunnel.getAlias();
        
        this.tunnel.setPingChecker( this );
        this.tunnel.setPingFailures( 0 );
        
        try {
            
            this.shellOut = new DataOutputStream( this.tunnel.getShellChannel().getOutputStream() );
            this.shellIn  = new BufferedReader( new InputStreamReader( this.tunnel.getShellChannel().getInputStream() ) );
            
        } catch ( IOException eIO ) {
            
            logger.error( "Could not aquire IN/OUT from shell for pinging purposes for tunnel " + tunnel.getAlias() + ": " + eIO.getMessage() );
        }
        
        this.callableGetPing = new Callable<Integer>() {
            
            @Override
            public Integer call() throws Exception {
                
                return PingChecker.this.getPing();
            }
        };
    }
    
    public void stop() {
        
        this.running = false;
    }
    
    public void start() {
        
        Thread daemon = new Thread( this, "kheetund-ping-checker-" + this.tunnel.getAlias().toLowerCase() );
        daemon.start();
    }
    
    private Integer getPing() throws IOException {
        
        logger.debug( "Measuring ping of connected tunnel " + tunnel.getAlias() );
        
        long pingStart = System.currentTimeMillis();
        
        this.shellOut.writeBytes( "echo kheetun." + Kheetun.VERSION + ".ping." + pingStart + "\r\n" );
        this.shellOut.flush();
        
        String output = this.shellIn.readLine();
        
        while ( output != null && ! output.startsWith( "kheetun." + Kheetun.VERSION + ".ping." + pingStart ) ) {
            
            output = this.shellIn.readLine();
        }
        
        long pingStop = System.currentTimeMillis();
        
        return (int)(pingStop - pingStart);
    }
    
    
    @Override
    public void run() {
        
        logger.info( "Started ping daemon[" + this + "] for tunnel " + this.id );
        
        while( this.running && this.tunnel.getState() == Tunnel.STATE_RUNNING && this.tunnel.getSession() != null && this.tunnel.getSession().isConnected() && this.tunnel.getPingFailures() < this.tunnel.getMaxPingFailures() ) {
            
            Future<Integer> futureGetPing = this.executorService.submit( this.callableGetPing );
            
            try {
                
                Integer ping = futureGetPing.get( this.tunnel.getPingTimeout(), TimeUnit.MILLISECONDS );
                
                this.tunnel.setPingFailures( 0 );
                this.tunnel.setPing( ping );
                
            } catch ( TimeoutException eTimeout ) {

                this.tunnel.increasePingFailures();
                logger.warn( "Ping failure " + this.tunnel.getPingFailures() + "/" + this.tunnel.getMaxPingFailures() + ": Timeout (> " + this.tunnel.getPingTimeout() + "ms)" );
                
            } catch ( InterruptedException eInterrupted ) {
                
                this.tunnel.increasePingFailures();
                logger.error( "Ping failure " + this.tunnel.getPingFailures() + "/" + this.tunnel.getMaxPingFailures() + ": Interrupted" );
                
            } catch ( ExecutionException eExecution ) {
                
                this.tunnel.increasePingFailures();
                logger.error( "Ping failure " + this.tunnel.getPingFailures() + "/" + this.tunnel.getMaxPingFailures() + ": " + eExecution.getMessage() );
                
            } finally {
                
               futureGetPing.cancel( true );
            }
                
            this.tunnelManager.updatePing( this.tunnel );
            
            logger.trace( "PingDaemon sleeping for 2 seconds" );
            
            try {
                Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
                logger.warn( "Sleep interrupted" );
            }
        }
        
        this.tunnelManager.updatePing( this.tunnel );
        
        if ( this.tunnel.getPingChecker() != null && this.tunnel.getPingChecker() == this ) {
            this.tunnel.setPingChecker( null );
        }
        
        logger.info( "Stopped ping daemon[ " + this + "] for tunnel " + this.id );
    }
}
