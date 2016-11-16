package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;

public class TextStyle {
    
    public static final TextStyle       DEFAULT;
    public static final TextStyle       PROFILE;
    public static final TextStyle       TUNNEL;
    
    static {
        {
            Font    fontActive    = new Font( Font.DIALOG, Font.PLAIN, 12 );
            Color   colorActive   = Color.BLACK;
            
            Font    fontInactive  = new Font( Font.DIALOG, Font.PLAIN, 12 );
            Color   colorInactive = Color.LIGHT_GRAY;
            
            DEFAULT = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
        }
        {
            HashMap<TextAttribute, Object> attributesActive = new HashMap<TextAttribute, Object>();
            attributesActive.put( TextAttribute.TRACKING, 0.1 );
            Font    fontActive    = new Font( Font.DIALOG, Font.BOLD, 13 );
            Color   colorActive   = Color.DARK_GRAY;

            HashMap<TextAttribute, Object> attributesInactive = new HashMap<TextAttribute, Object>();
            attributesInactive.put( TextAttribute.TRACKING, 0.1 );
            Font    fontInactive    = new Font( Font.DIALOG, Font.BOLD, 13 );
            Color   colorInactive   = Color.LIGHT_GRAY;
            
            PROFILE = new TextStyle( fontActive.deriveFont( attributesActive ), colorActive, fontInactive.deriveFont( attributesInactive ), colorInactive );
        }
        {
            Font    fontActive    = new Font( Font.DIALOG, Font.PLAIN, 12 );
            Color   colorActive   = Color.BLUE;
            
            Font    fontInactive  = new Font( Font.DIALOG, Font.PLAIN, 12 );
            Color   colorInactive = Color.LIGHT_GRAY;
            
            TUNNEL = new TextStyle( fontActive, colorActive, fontInactive, colorInactive );
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
