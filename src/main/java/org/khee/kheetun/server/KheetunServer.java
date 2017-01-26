package org.khee.kheetun.server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.Kheetun;
import org.khee.kheetun.server.manager.HostsManager;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class KheetunServer {
    
    static {
        System.setProperty( "log4j.configurationFile", "log4j2.server.xml" );
    }
    private static Logger logger = LogManager.getLogger( KheetunServer.class );

    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        
        Options options = new Options();
        
        options.addOption( OptionBuilder.withLongOpt( "port" ).withDescription( "Port to listen on for kheetun clients" ).withArgName( "num" ).hasArg().isRequired().create( "p" ) );

        CommandLineParser parser =  new GnuParser();
        
        try {
        
            CommandLine cmd = parser.parse( options, args );

            logger.info( "Started kheetun daemon " + Kheetun.VERSION );

            Signal.handle( new Signal( "INT" ), new SignalHandler() {
                
                public void handle(Signal arg0) {
                    
                    logger.info( "Caught SIGINT, will clean up and sail away" );
                    HostsManager.clear();
                    System.exit( 0 );
                }
            } );
            
            new TunnelServer( new Integer( cmd.getOptionValue( "port" ) ) );
            
            HostsManager.clear();
            
        } catch ( ParseException e ) {
            System.err.println( e.getLocalizedMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "kheetun-server.jar --port <num>", options );
            System.exit( 1 );
        }            
            
        
    }
    

}
