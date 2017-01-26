package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Font;

public class TextStyle {
    
    public static final TextStyle       DEFAULT;
    public static final TextStyle       PROFILE;
    public static final TextStyle       TUNNEL;
    public static final TextStyle       TUNNEL_RUNNING;
    public static final TextStyle       NO_PROFILES;
    
    public static final TextStyle       CONFIG_HEADER;
    
    static {
        {
            Font    fontActive    = new Font( Font.DIALOG, Font.PLAIN, 13 );
            Color   colorActive   = Color.BLACK;
            
            Font    fontInactive  = new Font( Font.DIALOG, Font.PLAIN, 13 );
            Color   colorInactive = Color.LIGHT_GRAY;
            
            DEFAULT = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
        }
        {
            Font    fontActive    = new Font( Font.DIALOG, Font.BOLD, 13 );
            Color   colorActive   = Color.BLUE;

            Font    fontInactive    = new Font( Font.DIALOG, Font.BOLD, 13 );
            Color   colorInactive   = Color.LIGHT_GRAY;
            
            PROFILE = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
        }
        {
            Font    fontActive    = new Font( Font.DIALOG, Font.PLAIN, 13 );
            Color   colorActive   = Color.DARK_GRAY;
            
            Font    fontInactive  = new Font( Font.DIALOG, Font.PLAIN, 13 );
            Color   colorInactive = Color.LIGHT_GRAY;
            
            TUNNEL = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
        }
        {
            Font    fontActive    = new Font( Font.DIALOG, Font.BOLD, 13 );
            Color   colorActive   = Color.DARK_GRAY;
            
            Font    fontInactive  = new Font( Font.DIALOG, Font.BOLD, 13 );
            Color   colorInactive = Color.LIGHT_GRAY;
            
            TUNNEL_RUNNING = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
        }
        {
            Font    fontActive    = new Font( Font.DIALOG, Font.BOLD, 15 );
            Color   colorActive   = Color.LIGHT_GRAY;
            
            Font    fontInactive  = new Font( Font.DIALOG, Font.BOLD, 15 );
            Color   colorInactive = Color.LIGHT_GRAY;
            
            NO_PROFILES = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
        }
        
        {
            Font    fontActive    = new Font( Font.MONOSPACED, Font.BOLD, 18 );
            Color   colorActive   = Color.DARK_GRAY;

            Font    fontInactive    = new Font( Font.MONOSPACED, Font.BOLD, 18 );
            Color   colorInactive   = Color.DARK_GRAY;
            
            CONFIG_HEADER = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
        }
        
    }
    
    private Font    fontActive;
    private Color   colorActive;
    private Font    fontInactive;
    private Color   colorInactive;
    
    public TextStyle( Font fontActive, Color colorActive, Font fontInactive, Color colorInactive ) {
        
        this.fontActive     = fontActive;
        this.colorActive    = colorActive;
        this.fontInactive   = fontInactive;
        this.colorInactive  = colorInactive;
    }

    public Font getFontActive() {
        return fontActive;
    }

    public void setFontActive(Font fontActive) {
        this.fontActive = fontActive;
    }

    public Color getColorActive() {
        return colorActive;
    }

    public void setColorActive(Color colorActive) {
        this.colorActive = colorActive;
    }

    public Font getFontInactive() {
        return fontInactive;
    }

    public void setFontInactive(Font fontInactive) {
        this.fontInactive = fontInactive;
    }

    public Color getColorInactive() {
        return colorInactive;
    }

    public void setColorInactive(Color colorInactive) {
        this.colorInactive = colorInactive;
    }
}
