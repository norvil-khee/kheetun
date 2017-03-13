package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIDefaults;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.khee.kheetun.client.gui.tray.TrayManager;

@SuppressWarnings("serial")
public class KhmenuItem extends JPanel implements MouseListener {

    private static int      count               = 0;
    
    protected Imx           icon;
    protected JLabel        iconLeft            = new JLabel( Imx.NONE );
    protected JLabel        iconCenter          = new JLabel( Imx.NONE );
    protected JLabel        iconRight           = new JLabel( Imx.NONE );
    protected JLabel        iconInfo            = new JLabel( Imx.NONE );
    protected JLabel        text                = new JLabel( "" );
    protected JLabel        status              = new JLabel( "", JLabel.RIGHT );
    
    private TextStyle       textStyle           = TextStyle.DEFAULT;
    
    protected AnImx         processing          = new AnImx( "loading.png", 50, 125, 16 );
    
    protected Color         colorBackground;

    protected JLabel        message             = new JLabel( Imx.WARNING.color( Kholor.ERROR ) );
    protected JLabel        info                = new JLabel( Imx.INFO );
    protected JLabel        clipboardHint       = new JLabel( "[Click middle mouse button to copy this message]", Imx.NONE, JLabel.LEFT );
    protected JWindow       windowMessage       = new JWindow();
    protected JWindow       windowInfo          = new JWindow();
    
    protected boolean       active              = true;
    
    private int id                              = KhmenuItem.count++;
    
    public KhmenuItem( Imx icon ) {
        
        this.icon = icon;
        
        this.initComponents();

        if ( icon != null ) {
            
            if ( icon.getIconWidth() == icon.getIconHeight() ) {
                
                this.iconLeft.setIcon( icon.color( Color.WHITE ) );
                this.iconLeft.setOpaque( true );
                this.iconLeft.setBackground( Kholor.BUTTON_HOVER );
                this.iconLeft.setBorder( new EmptyBorder( new Insets( 4, 4, 5, 4 ) ) );
                
            } else {

                this.iconLeft.setIcon( icon );
            }
        }
    }
    
    public KhmenuItem( Imx icon, String text ) {
        
        this.icon = icon;
        
        this.initComponents();
        
        this.text.setText( text );

        if ( icon != null ) {

            this.iconLeft.setIcon( icon.color( Color.WHITE ) );
            this.iconLeft.setOpaque( true );
            this.iconLeft.setBackground( Kholor.BUTTON_HOVER );
            this.iconLeft.setBorder( new EmptyBorder( new Insets( 4, 4, 5, 4 ) ) );
        }
    }
    
    public KhmenuItem() {
    }
    
    protected void initComponents() {
        
        UIDefaults defaults = javax.swing.UIManager.getDefaults();
        this.colorBackground = defaults.getColor( "List.selectionBackground" );
            
        this.setOpaque( true );
        this.setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
        this.setAlignmentX( Container.LEFT_ALIGNMENT );
        
        this.iconCenter.setBorder( new EmptyBorder( new Insets( 0, 0, 0, 4 ) ) );

        this.status.setForeground( Color.LIGHT_GRAY );
        this.status.setBorder( new EmptyBorder( new Insets( 0, 8, 0, 4 ) ) );
        this.status.setAlignmentX( Component.RIGHT_ALIGNMENT );
        this.status.setPreferredSize( new Dimension( 120, this.status.getPreferredSize().height ) );
        
        this.text.setOpaque( true );
        this.text.setBorder( new EmptyBorder( new Insets( 4, 8, 4, 0 ) ) );
        this.setTextStyle( this.textStyle );
        
        this.processing.setVisible( false );
        
        this.iconRight.addMouseListener( this );
        this.iconCenter.addMouseListener( this );
        this.iconLeft.addMouseListener( this );
        this.iconInfo.addMouseListener( this );
        
        this.iconInfo.setToolTipText( "WTF" );

        this.info.setIconTextGap( 8 );
        this.info.setForeground( Color.BLACK );
        this.info.setAlignmentX( Component.LEFT_ALIGNMENT );
        this.info.setHorizontalAlignment( JLabel.LEFT );
        this.info.setBorder( new CompoundBorder( new LineBorder( Color.BLUE ), new EmptyBorder( new Insets( 4, 4, 4, 4 ) ) ) );
        
        this.message.setIconTextGap( 8 );
        this.message.setForeground( Color.BLACK );
        this.message.setAlignmentX( Component.LEFT_ALIGNMENT );
        this.message.setHorizontalAlignment( JLabel.LEFT );
        
        this.clipboardHint.setIconTextGap( 8 );
        this.clipboardHint.setForeground( Color.GRAY );
        this.clipboardHint.setAlignmentX( Component.LEFT_ALIGNMENT );
        this.clipboardHint.setHorizontalAlignment( JLabel.LEFT );
        
        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
        panel.setBorder( new CompoundBorder( new LineBorder( Color.RED ), new EmptyBorder( new Insets( 4, 4, 4, 4 ) ) ) );
        panel.add( this.message );
        panel.add( this.clipboardHint );
        
        this.windowMessage.setLocation( this.getLocation() );
        this.windowMessage.getContentPane().setLayout( new BoxLayout( this.windowMessage.getContentPane(), BoxLayout.PAGE_AXIS ) );
        this.windowMessage.getContentPane().add( panel );
        this.windowMessage.setAlwaysOnTop( true );
        this.windowMessage.setType( Type.POPUP );

        this.windowInfo.setLocation( this.getLocation() );
        this.windowInfo.getContentPane().setLayout( new BoxLayout( this.windowInfo.getContentPane(), BoxLayout.PAGE_AXIS ) );
        this.windowInfo.getContentPane().add( this.info );
        this.windowInfo.setAlwaysOnTop( true );
        this.windowInfo.setType( Type.POPUP );
     
        this.add( this.iconLeft );
        this.add( this.iconCenter );
        this.add( this.processing );
        this.add( this.iconRight );
        this.add( this.text );
        this.add( Box.createHorizontalGlue() );
        this.add( Box.createHorizontalStrut( 32 ) );
        this.add( this.status );
        this.add( Box.createHorizontalStrut( 4 ) );
        this.add( this.iconInfo );
        
        this.addMouseListener( this );
    }
    
    public void setTextStyle(TextStyle textStyle) {
        
        this.textStyle = textStyle;
        this.setActive( this.active );
    }

    public JLabel getTextLabel() {
        return this.text;
    }
    
    public JLabel getInfoIcon() {
        return this.iconInfo;
    }
    
    public void setActive( boolean active ) {
        
        this.active = active;
        
        if ( ! active ) {
            this.setBackground( null );
            text.setBackground( null );
        }
        
        text.setForeground( active ? this.textStyle.getColorActive() : this.textStyle.getColorInactive() );
        text.setFont( active ? this.textStyle.getFontActive() : this.textStyle.getFontInactive() );
    }
    
    public void setStatus( String status, Color color ) {
        
        this.status.setText( status );
        this.status.setForeground( color );
    }
    
    public void setProcessing( boolean state ) {
        
        processing.setVisible( state );
        iconRight.setVisible( ! state );
    }
    
    public boolean isProcessing() {
        
        return processing.isVisible();
    }
    
    public void setError( String error ) {
        
        if ( error == null ) {
            this.message.setText( null );
            this.iconCenter.setIcon( Imx.NONE );
            TrayManager.clearError( id );
            this.windowInfo.setVisible( false );
        } else {
            
            if ( ! error.equals( this.message.getText() ) ) {
                
                this.message.setText( error );
                this.iconCenter.setIcon( Imx.WARNING.color( Kholor.ERROR ) );
                TrayManager.setError( id, error );
            }
        }
    }
    
    public void setErrorRead() {
        
        this.iconCenter.setIcon( Imx.WARNING.color( Kholor.READ ) );
        TrayManager.clearError( id );
    }
    
    public void setInfo( String info ) {
        
        if ( info == null ) {
            this.info.setText( null );
            this.iconInfo.setIcon( Imx.NONE );
            this.windowInfo.setVisible( false );
        } else {
            this.info.setText( info );
            this.iconInfo.setIcon( Imx.INFO );
        }
    }
    
    public void leftClick( MouseEvent e ) {
        
    }
    
    @Override
    public void mouseEntered( MouseEvent e ) {
        
        if ( this.message.getText() != null ) {
            
            this.setErrorRead();
            
            Point p = this.getLocationOnScreen();
            
            if ( p.x - windowMessage.getWidth() < 0 ) {
                p.setLocation( p.x + this.getWidth() - 16, p.y );
            } else {
                p.setLocation( p.x - windowMessage.getWidth() + 16, p.y );
            }
            
            windowMessage.pack();
            windowMessage.setLocation( p );
            windowMessage.setVisible( true );
        }
        
        if ( this.info.getText() != null ) {
            
            Point p = this.getLocationOnScreen();
            
            if ( p.x - windowInfo.getWidth() < 0 ) {
                p.setLocation( p.x + this.getWidth() - 16, p.y );
            } else {
                p.setLocation( p.x - windowInfo.getWidth() + 16, p.y );
            }
            
            if ( this.windowMessage.isVisible() ) {
                p.setLocation( p.x, p.y + this.windowMessage.getHeight() + 2 );
            }
            
            windowInfo.pack();
            windowInfo.setLocation( p );
            windowInfo.setVisible( true );
        }
        
        if ( this.icon != null && this.icon.getIconWidth() == this.icon.getIconHeight() ) {
            
            this.iconLeft.setIcon( this.icon.color( Kholor.LIGHT_BLUE ) );
        }
        
        this.setBackground( colorBackground );
        text.setBackground( colorBackground );
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        
        if ( windowMessage.isVisible() ) {
            windowMessage.setVisible( false );
        }
        
        if ( windowInfo.isVisible() ) {
            windowInfo.setVisible( false );
        }
        
        if ( this.icon != null && this.icon.getIconWidth() == this.icon.getIconHeight() ) {
            
            this.iconLeft.setIcon( this.icon.color( Color.WHITE ) );
        }
        
        this.setBackground( null );
        text.setBackground( null );
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        
        if ( ! this.active ) {
            return;
        }
        
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
            
            this.windowMessage.setVisible( false );
            this.windowInfo.setVisible( false );
            this.leftClick( e );
        }
        
        if ( e.getButton() == MouseEvent.BUTTON2 ) {
            
            if ( iconCenter.isVisible() && ( iconCenter.getIcon() == Imx.WARNING.color( Kholor.ERROR ) || iconCenter.getIcon() == Imx.WARNING.color( Kholor.READ ) ) ) {
                
                StringSelection selection = new StringSelection( this.message.getText() );
                
                Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipBoard.setContents( selection, null );
                
                return;
            }
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        
        if ( e.getButton() == MouseEvent.BUTTON2 ) {
            
            if ( iconCenter.isVisible() && ( iconCenter.getIcon() == Imx.WARNING.color( Kholor.ERROR ) || iconCenter.getIcon() == Imx.WARNING.color( Kholor.READ ) ) ) {
                
                this.clipboardHint.setForeground( Color.BLACK );
                return;
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {

        if ( e.getButton() == MouseEvent.BUTTON2 ) {
            
            if ( iconCenter.isVisible() && ( iconCenter.getIcon() == Imx.WARNING.color( Kholor.ERROR ) || iconCenter.getIcon() == Imx.WARNING.color( Kholor.READ ) ) ) {
                
                this.clipboardHint.setForeground( Color.GRAY );
                return;
            }
        }
    }
}
