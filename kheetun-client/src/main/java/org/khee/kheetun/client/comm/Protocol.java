package org.khee.kheetun.client.comm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.khee.kheetun.client.config.Tunnel;


public class Protocol implements Serializable {
    
    public static final long serialVersionUID = 42;
    
    public static final int UNKNOWN         =  -1;
    public static final int ERROR           =  50;
    public static final int CONNECT         = 100;
    public static final int ACCEPT          = 200;
    public static final int DISCONNECT      = 300;
    public static final int QUIT            = 350;
    public static final int STARTTUNNEL     = 400;
    public static final int TUNNELSTARTED   = 450;
    public static final int STOPTUNNEL      = 500;
    public static final int TUNNELSTOPPED   = 550;
    public static final int QUERYTUNNELS    = 600;
    public static final int ACTIVETUNNELS   = 650;
    public static final int ECHO            = 900;
    
    private static final HashMap<Integer, String> commandToString;
    static {
        commandToString = new HashMap<Integer, String>();
        commandToString.put( UNKNOWN,       "UNKNOWN" );
        commandToString.put( ERROR,         "ERROR" );
        commandToString.put( CONNECT,       "CONNECT" );
        commandToString.put( ACCEPT,        "ACCEPT" );
        commandToString.put( DISCONNECT,    "DISCONNECT" );
        commandToString.put( QUIT,          "QUIT" );
        commandToString.put( STARTTUNNEL,   "STARTTUNNEL" );
        commandToString.put( TUNNELSTARTED, "TUNNELSTARTED" );
        commandToString.put( STOPTUNNEL,    "STOPTUNNEL" );
        commandToString.put( TUNNELSTOPPED, "TUNNELSTOPPED" );
        commandToString.put( QUERYTUNNELS,  "QUERYTUNNELS" );
        commandToString.put( ACTIVETUNNELS, "ACTIVETUNNELS" );
        commandToString.put( ECHO,          "ECHO" );
    }
    
    private String user = System.getProperty( "user.name" );
    private int command;
    private Tunnel tunnel;
    private String string;
    private String socket;
    private ArrayList<String> signatures = new ArrayList<String>();
    
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

    public Protocol( int command, Tunnel tunnel, String socket ) {
        this.command = command;
        this.tunnel  = tunnel;
        this.socket  = socket;
    }
    
    public Protocol( int command, ArrayList<String> signatures ) {
        this.command = command;
        this.signatures = signatures;
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

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
    
    public ArrayList<String> getSignatures() {
        return signatures;
    }

    public void setSignatures(ArrayList<String> signatures) {
        this.signatures = signatures;
    }

    public void clear() {
        
        command = UNKNOWN;
        tunnel  = null;
        string  = "";
        socket  = "";
    }
    
    @Override
    public String toString() {
        
        
        return "Protocol[ " + commandToString.get( getCommand() ) + ", String = " + getString() + ", Tunnel = " + ( getTunnel() != null ? getTunnel().getSignature() : "null" ) + ", Socket = " + getSocket() + " ]";
                 
    }

}
