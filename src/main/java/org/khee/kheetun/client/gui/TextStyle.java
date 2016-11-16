package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;

public class TextStyle {
    
    public static final TextStyle       DEFAULT_ACTIVE;
    public static final TextStyle       DEFAULT_INACTIVE;
    public static final TextStyle       PROFILE_ACTIVE;
    public static final TextStyle       PROFILE_INACTIVE;
    
    static {
        {
            Font    font    = new Font( "Arial", Font.PLAIN, 12 );
            Color   color   = Color.BLACK;
            
            DEFAULT_ACTIVE = new TextStyle( font, color );
        }
        {
            Font    font    = new Font( "Arial", Font.PLAIN, 12 );
            Color   color   = Color.LIGHT_GRAY;
            
            DEFAULT_INACTIVE = new TextStyle( font, color );
        }
        {
            HashMap<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
            attributes.put( TextAttribute.TRACKING, 0.3 );
            
            Font    font    = new Font( "Arial", Font.BOLD, 13 );
            Color   color   = Color.BLACK;
            
            PROFILE_ACTIVE = new TextStyle( font.deriveFont( attributes ), color );
        }
        {
            HashMap<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
            attributes.put( TextAttribute.TRACKING, 0.3 );
            
            Font    font    = new Font( "Arial", Font.BOLD, 13 );
            Color   color   = Color.LIGHT_GRAY;
            
            PROFILE_INACTIVE = new TextStyle( font.deriveFont( attributes ), color );
        }
    }
    
    private Font    font;
    private Color   color;
    
    public TextStyle( Font font, Color color ) {
        
        this.font   = font;
        this.color  = color;
    }
    
    public Font getFont() {
        return font;
    }
    public void setFont(Font font) {
        this.font = font;
    }
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }

}
