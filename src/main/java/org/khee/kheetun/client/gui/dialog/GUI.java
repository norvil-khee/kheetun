package org.khee.kheetun.client.gui.dialog;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.client.gui.Imx;

public class GUI {
    
    public Class<? extends GUIElement>  component       = null;
    public Object                       param1          = null;
    public Object                       param2          = null;
    public Object                       param3          = null;
    public Object                       param4          = null;
    public String                       hint            = null;
    public Imx                          icon            = Imx.NONE;
    public Font                         font            = new Font( Font.DIALOG, Font.PLAIN, 12 );
    public int                          width           = 140;
    public int                          gridx           = 0;
    public int                          gridy           = 0;
    public int                          gridwidth       = 1;
    public int                          gridheight      = 1;
    public int                          anchor          = GridBagConstraints.LINE_START;
    public int                          fill            = GridBagConstraints.VERTICAL;
    
    public static final HashMap<Field, GUI> FIELD;
    
    static {
        
        FIELD = new HashMap<Field, GUI>();
        
        try {
        
            // Profile:name
            {
                GUI gui         = new GUI( GUIElementTextField.class );
                gui.hint        = "Name";
                gui.icon        = Imx.EDIT;
                gui.font        = new Font( Font.DIALOG, Font.BOLD, 12 );
                gui.width       = 160;
                
                FIELD.put( Profile.class.getDeclaredField( "name" ), gui );
            }
            
            // Profile:active
            {
                GUI gui     = new GUI( GUIElementBoolean.class );
                gui.hint    = "Active";
                gui.param1  = "ON";
                gui.param3  = "OFF";
                gui.icon    = Imx.POWER;
                gui.gridx   = 2;
                gui.width   = 60;
                
                FIELD.put( Profile.class.getDeclaredField( "active" ), gui );
            }
            
            
            // Tunnel:alias
            {
                GUI gui         = new GUI( GUIElementTextField.class );
                gui.hint        = "Name";
                gui.width       = 340;
                gui.icon        = Imx.EDIT;
                gui.gridwidth   = 2;
                gui.font        = new Font( Font.DIALOG, Font.BOLD, 12 );
                
                FIELD.put( Tunnel.class.getDeclaredField( "alias" ), gui );
            }
            
            // Tunnel:sshkey
            {
                GUI gui     = new GUI( GUIElementFileSelector.class );
                gui.hint    = "SSH Key";
                gui.width   = 140;
                gui.gridx   = 2;
                gui.icon    = Imx.KEY;
                
                FIELD.put( Tunnel.class.getDeclaredField( "sshKey" ), gui );
            }
            
            // Tunnel:autostart
            {
                GUI gui         = new GUI( GUIElementBoolean.class );
                gui.hint        = "Auto";
                gui.param1      = "ON";
                gui.param3      = "OFF";
                gui.icon        = Imx.AUTO;
                gui.gridheight  = 1;
                gui.gridx       = 3;
                gui.width       = 60;
                
                FIELD.put( Tunnel.class.getDeclaredField( "autostart" ), gui );
            }
            
            // Tunnel:user
            {
                GUI gui     = new GUI( GUIElementTextField.class );
                gui.hint    = "User";
                gui.gridy   = 1;
                gui.width   = 150;
                gui.icon    = Imx.USER;
                
                FIELD.put( Tunnel.class.getDeclaredField( "user" ), gui );
            }

            // Tunnel:host
            {
                GUI gui     = new GUI( GUIElementTextField.class );
                gui.hint    = "Host";
                gui.gridx   = 1;
                gui.gridy   = 1;
                gui.width   = 190;
                gui.icon    = Imx.HOST;
                
                FIELD.put( Tunnel.class.getDeclaredField( "hostname" ), gui );
            }
            
            // Tunnel:port
            {
                GUI gui     = new GUI( GUIElementNumberField.class );
                gui.hint    = "Port";
                gui.gridx   = 2;
                gui.gridy   = 1;
                gui.width   = 140;
                gui.icon    = Imx.PORT;
                
                FIELD.put( Tunnel.class.getDeclaredField( "port" ), gui );
            }
            

            
            // Forward:type
            {
                GUI gui         = new GUI( GUIElementLocalRemote.class );
                gui.hint        = "Type";
                gui.gridheight  = 1;
                gui.width       = 100;
                gui.icon        = Imx.DIRECTION;
                
                FIELD.put( Forward.class.getDeclaredField( "type" ), gui );
            }
            
            // Forward:bindIp
            {
                GUI gui     = new GUI( GUIElementTextField.class );
                gui.hint    = "Bind IP";
                gui.gridx   = 1;
                gui.width   = 250;
                gui.icon    = Imx.HOST;
                
                FIELD.put( Forward.class.getDeclaredField( "bindIp" ), gui );
            }
            
            // Forward: bindPort
            {
                GUI gui     = new GUI( GUIElementNumberField.class );
                gui.hint    = "Bind Port";
                gui.gridx   = 2;
                gui.width   = 100;
                gui.icon    = Imx.PORT;
                
                FIELD.put( Forward.class.getDeclaredField( "bindPort" ), gui );
            }
            
            // Forward:forwardedHost
            {
                GUI gui     = new GUI( GUIElementTextField.class );
                gui.hint    = "Forwarded Host";
                gui.gridx   = 1;
                gui.gridy   = 1;
                gui.width   = 250;
                gui.icon    = Imx.HOST;
                
                FIELD.put( Forward.class.getDeclaredField( "forwardedHost" ), gui );
            }
            
            // Forward: forwardedPort
            {
                GUI gui     = new GUI( GUIElementNumberField.class );
                gui.hint    = "Forwarded Port";
                gui.gridx   = 2;
                gui.gridy   = 1;
                gui.width   = 100;
                gui.icon    = Imx.PORT;
                
                FIELD.put( Forward.class.getDeclaredField( "forwardedPort" ), gui );
            }
            
        } catch ( NoSuchFieldException eNoSuchField ) {
            
            eNoSuchField.printStackTrace();
        }
    }
    
    protected GUI( Class<? extends GUIElement> component ) {
        
        this.component = component;
    }
}
