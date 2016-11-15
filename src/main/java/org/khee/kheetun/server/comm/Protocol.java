package org.khee.kheetun.server.comm;

import java.io.Serializable;
import java.util.HashMap;

import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Tunnel;


public class Protocol implements Serializable {
    
    public static final long serialVersionUID = 42;
    
    public static final int UNKNOWN         =  -1;
    public static final int TUNNEL          =   1;
    public static final int CONFIG          =  10;
    public static final int ERROR           =  50;
    public static final int CONNECT         = 100;
    public static final int ACCEPT          = 200;
    public static final int DISCONNECT      = 300;
    public static final int QUIT            = 350;
    public static final int START           = 400;
    public static final int STOP            = 500;
    public static final int TOGGLE          = 600;
    public static final int STOPALL         = 675;
    public static final int AUTOALL         = 680;
    public static final int ECHO            = 900;
    
    private static final HashMap<Integer, String> commandToString;
    static {
        commandToString = new HashMap<Integer, String>();
        commandToString.put( UNKNOWN,        "UNKNOWN" );
        commandToString.put( TUNNEL,         "TUNNEL" );
        commandToString.put( CONFIG,         "CONFIG" );
        commandToString.put( ERROR,          "ERROR" );
        commandToString.put( CONNECT,        "CONNECT" );
        commandToString.put( ACCEPT,         "ACCEPT" );
        commandToString.put( DISCONNECT,     "DISCONNECT" );
        commandToString.put( QUIT,           "QUIT" );
        commandToString.put( START,          "STARTTUNNEL" );
        commandToString.put( TOGGLE,         "TOGGLE" );
        commandToString.put( STOP,           "STOPTUNNEL" );
        commandToString.put( STOPALL,        "STOPALLTUNNELS" );
        commandToString.put( AUTOALL,        "AUTOALL" );
        commandToString.put( ECHO,           "ECHO" );
    }
    
    private String              user        = System.getProperty( "user.name" );
    private int                 command     = -1;
    private Tunnel              tunnel      = null;
    private String              string      = null;
    private Config              config      = null;
    
    public Protocol( int command ) {
        this.command = command;
    }
    
    public Protocol( int command, String string ) {
        this.command = command;
        this.string  = string;
    }
    
    public Protocol( int command, Tunnel tunnel ) {
        this.command = command;
        this.tunnel  = tunnel;
    }
    
    public Protocol( int command, Tunnel tunnel, String string ) {
        this.command = command;
        this.tunnel  = tunnel;
        this.string  = string;
    }
    
    public Protocol( int command, Config config ) {
        this.command = command;
        this.config  = config;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public Tunnel getTunnel() {
        return tunnel;
    }

    public void setTunnel(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
    
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void clear() {
        
        command = UNKNOWN;
        tunnel  = null;
        string  = null;
        config  = null;
    }
    
    @Override
    public String toString() {
        
        String s = "Protocol[" + Protocol.commandToString.get( this.getCommand() ) + " ";
        
        s += ( this.string != null ) ? "String=" + this.string + " " : "";
        s += ( this.tunnel != null ) ? "Tunnel=" + this.tunnel + " " : "";
        s += "]";
        
        return s;
    }

}
