package org.khee.kheetun.server.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Forward;

/*
 * manage hosts file
 */
public class HostsManager {
    
    private static Logger logger = LogManager.getLogger( "kheetund" );

    private static HostsManager instance = new HostsManager();
    public static HostsManager getInstance() { 
        return instance; 
    }
    
    private static final String beginMarker = "#### kheetund managed start";
    private static final String endMarker   = "#### kheetund managed end";
    
    private File hosts;
    private ArrayList<String> content;
    private BufferedReader reader;
    private BufferedWriter writer;
    
    protected HostsManager() {
        
        // TODO: Windows... ugh..
        
        hosts = new File( "/etc/hosts" );
        content = new ArrayList<String>();
    }
    
    private void readHosts() {
        
        content.clear();
        
        try {
            
            reader = new BufferedReader( new FileReader( hosts ) );

            String line;
            while ( ( line = reader.readLine() ) != null ) {
                content.add( line );
            }
            
        } catch ( Exception e ) {
            
            logger.error( e.getMessage() );
        }
    }
    
    private void writeHosts() {
        
        try {
            
            hosts.createNewFile();
            writer = new BufferedWriter( new FileWriter( hosts ) );
            
            for( String line : content ) {
                
                writer.write( line + "\n") ;
            }
            
            writer.flush();
            writer.close();
            
        } catch ( Exception e ) {
            
            logger.error( e.getMessage() );
        }
    }
    
    
    public static void addForwards( ArrayList<Forward> forwards ) {
        
        instance.readHosts();
        for ( Forward forward : forwards ) {
            instance.addForward( forward );
        }
        instance.writeHosts();
    }
    
    public static void removeForwards( ArrayList<Forward> forwards ) {
        
        instance.readHosts();
        for ( Forward forward : forwards ) {
            instance.removeForward( forward );
        }
        instance.writeHosts();
    }
    
    public static void clear() {
        instance.readHosts();
        instance.cleanup();
        instance.writeHosts();
    }
    
    private void cleanup() {
        
        int start = content.indexOf( HostsManager.beginMarker );
        int end   = content.indexOf( HostsManager.endMarker );
        
        if ( start > -1 && end > -1 ) {
            
            ArrayList<String> top    = new ArrayList<String>( content.subList( 0, start -1 ) ); 
            ArrayList<String> bottom = new ArrayList<String>( content.subList( end + 1, content.size() ) ); 
            
            content = new ArrayList<String>( top );
            content.addAll( bottom );
        }
    }
    
    private void addForward( Forward forward ) {
        
        String entry = forward.getBindIp() + "    " + forward.getForwardedHost();
        
        if ( content.contains( entry ) ) {
            return;
        }
        
        if ( ! content.contains( HostsManager.beginMarker ) ) {
            content.add( HostsManager.beginMarker );
            content.add( entry );
            content.add( HostsManager.endMarker );
        } else {
            int index = content.indexOf( HostsManager.beginMarker );
            content.add( index + 1, entry );
        }
    }
    
    private void removeForward( Forward forward ) {
        
        String entry = forward.getBindIp() + "    " + forward.getForwardedHost();
        
        if ( ! content.contains( entry ) ) {
            return;
        }
        
        content.remove( entry );
        
        if ( content.indexOf( HostsManager.beginMarker ) == content.indexOf( HostsManager.endMarker ) - 1 ) {
            content.remove( HostsManager.beginMarker );
            content.remove( HostsManager.endMarker );
        }
    }
    
    
}
