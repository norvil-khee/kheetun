package org.khee.kheetun.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.server.TunnelServer;
import org.khee.kheetun.server.manager.HostsManager;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class kheetund {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );
    private static final String VERSION = "0.1";

    public static void main(String[] args) {
        
        logger.info( "Started kheetun daemon " + VERSION );

        Signal.handle( new Signal( "INT" ), new SignalHandler() {
            
            public void handle(Signal arg0) {
                
                logger.info( "Caught SIGINT, will clean up and sail away" );
                HostsManager.clear();
                System.exit( 0 );
            }
        } );
        
        new TunnelServer();
        
        HostsManager.clear();
    }
    

}
