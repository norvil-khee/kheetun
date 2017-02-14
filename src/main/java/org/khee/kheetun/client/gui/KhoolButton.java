package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.khee.kheetun.client.gui.dialog.Dialog;

@SuppressWarnings("serial")
public class KhoolButton extends JPanel implements MouseListener {
    
    private JLabel      label;
    private Imx         icon;
    protected String    text;
    private boolean     armed = false;
    private ArrayList<KhbuttonListener> listeners   = new ArrayList<KhbuttonListener>();
    private String      id;
    private Object      object;
    
    public KhoolButton( String text, Imx icon ) {
        
        this.id     = text;
        this.text   = text;
        this.icon   = icon;
        
        this.setLayout( null );
        this.setOpaque( false );
        this.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
        
        this.setMinimumSize( new Dimension( 24, 24 ) );
        this.setPreferredSize( new Dimension( 24, 24 ) );
        this.setMaximumSize( new Dimension( 24, 24 ) );
        
        this.label = new JLabel( this.icon.color( Color.WHITE ) );
        this.label.setOpaque( true );
        this.label.setBackground( Kholor.BLUE );
        
        this.label.setBounds( 0, 0, 24, 24 );
        
        this.add( this.label );
        
        this.addMouseListener( this );
        
    }
    
    public void setText( String text ) {
        
        this.text = text;
    }
    
    public void setIcon( Imx icon ) {
        
        this.icon = icon;
        this.label.setIcon( icon );
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
    
    public void click() {
        
    };
    
    @Override
    public void setEnabled( boolean enabled ) {
        
        this.label.setForeground( enabled ? Color.WHITE : Color.LIGHT_GRAY );
        this.label.setIcon( enabled ? this.icon.color( Color.WHITE ) : this.icon.color( Color.LIGHT_GRAY ) );
        
        super.setEnabled( enabled );
        this.repaint();
    }
    
    @Override
    public void mouseClicked( MouseEvent e ) {
        
    }
    
    @Override
    public void mouseEntered( MouseEvent e ) {
        
        if ( this.isEnabled() ) {
            this.label.setIcon( this.icon.color( Kholor.LIGHT_BLUE ) );
        }
        
        Dialog.getInstance().showTooltip( this.getLocationOnScreen(), this.icon, this.text );
    }
    
    @Override
    public void mouseExited( MouseEvent e ) {
        
        if ( this.isEnabled() ) {
            this.label.setIcon( this.icon.color( Color.WHITE ) );
        }
        
        Dialog.getInstance().hideTooltip();
        
        this.armed = false;
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
