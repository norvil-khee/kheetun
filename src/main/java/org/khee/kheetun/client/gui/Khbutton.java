package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Khbutton extends JPanel implements MouseListener {
    
    private JLabel  label;
    private Imx     icon;
    private String  text;
    private boolean hover = false;
    private boolean armed = false;
    private ArrayList<KhbuttonListener> listeners = new ArrayList<KhbuttonListener>();
    private String id;
    private Object object;
    
    private Color   colorEnabled    = new Color( 0x286090 );
    private Color   colorHover      = new Color( 0x3870A0 );
    private Color   colorDisabled   = new Color( 0xA0A0A0 );
    private Color   colorPressed    = new Color( 0x185080 );
    
    public Khbutton( String text, Imx icon ) {
        
        this.id     = text;
        this.text   = text;
        this.icon   = icon;
        
        this.setLayout( new GridBagLayout() );
        this.setOpaque( false );
        this.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
        
        this.label = new JLabel( this.text, this.icon.lighten( 32 ), JLabel.LEFT );
        this.label.setForeground( Color.WHITE );
        this.label.setIconTextGap( 8 );

        this.add( this.label );
        
        this.addMouseListener( this );
        
        this.setWidth( this.label.getPreferredSize().width );
    }
    
    public void setText( String text ) {
        
        this.text = text;
        this.label.setText( text );
    }
    
    public void setIcon( Imx icon ) {
        
        this.icon = icon;
        this.label.setIcon( icon );
    }
    
    public void setStyle( Color colorEnabled, Color colorHover, Color colorDisabled, Color colorPressed ) {
        
        this.colorEnabled   = colorEnabled;
        this.colorHover     = colorHover;
        this.colorDisabled  = colorDisabled;
        this.colorPressed   = colorPressed;
    }
    
    public void setWidth( int width ) {
        
        Dimension d = new Dimension( width + 8, this.label.getPreferredSize().height + 8 );

        this.setPreferredSize( d );
        this.setMinimumSize( d );
        this.setMaximumSize( d );
        this.revalidate();
    }
    
    public void setId( String id ) {
        
        this.id = id;
    }
    
    public void setObject( Object object ) {
        
        this.object = object;
    }
    
    public void addButtonListener( KhbuttonListener listener ) {
        
        this.listeners.add( listener );
    }
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
        
        if ( this.isEnabled() ) {
            
            if ( this.armed ) {
                
                g2d.setPaint( this.colorPressed );

            } else {
            
                g2d.setPaint( this.hover ? this.colorHover : this.colorEnabled );
            }
            
        } else {
            
            g2d.setPaint( this.colorDisabled );
        }
        
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g2d.fill( new Rectangle2D.Double( 0,  0, this.getWidth(), this.getHeight() ) );

        super.paint(g);
    }
    
    public void click() {
        
    };
    
    @Override
    public void setEnabled( boolean enabled ) {
        
        this.label.setForeground( enabled ? Color.WHITE : Color.LIGHT_GRAY );
        this.label.setIcon( enabled ? this.icon : this.icon.color( Color.LIGHT_GRAY ) );
        
        super.setEnabled( enabled );
        this.repaint();
    }
    
    @Override
    public void mouseClicked( MouseEvent e ) {
    }
    
    @Override
    public void mouseEntered( MouseEvent e ) {
        
        this.hover = true;
        
        if ( this.isEnabled() ) {
            this.label.setIcon( this.icon.lighten( 20 ) );
        }
        
        this.repaint();
    }
    
    @Override
    public void mouseExited( MouseEvent e ) {
        
        if ( this.isEnabled() ) {
            this.label.setIcon( this.icon );
        }
        
        this.hover = false;
        this.armed = false;
        this.repaint();
    }
    
    @Override
    public void mousePressed( MouseEvent e ) {
        
        if ( this.isEnabled() ) {
            this.armed = true;
            this.repaint();
        }
    }
    
    @Override
    public void mouseReleased( MouseEvent e ) {
        
        if ( ! this.isEnabled() ) {
            return;
        }
        
        if ( this.armed ) {
            
            this.click();
            
            for ( KhbuttonListener listener : this.listeners ) {
                
                listener.buttonClicked( this.id, this.object );
            }
        }
        
        this.armed = false;
        this.repaint();
    }
}
