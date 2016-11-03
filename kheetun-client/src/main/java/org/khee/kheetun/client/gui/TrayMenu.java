package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIDefaults;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.khee.kheetun.client.TunnelClient;
import org.khee.kheetun.client.TunnelManager;
import org.khee.kheetun.client.TunnelManagerListener;
import org.khee.kheetun.client.kheetun;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;

public class TrayMenu extends JWindow implements MouseListener, ConfigManagerListener, TunnelManagerListener {
    
    public static final long serialVersionUID = 42;
    
    private KTMenuItem  labelKheetun;
    private KTMenuItem  labelConnected;
    private KTMenuItem  labelConfig;
    private KTMenuItem  itemExit;
    private KTMenuItem  itemQuery;
    private KTMenuItem  itemStopAll;
    private KTMenuItem  itemAutostartAll;
    private JPanel      panel;
    
    @SuppressWarnings("serial")
    public TrayMenu() {
        
        setName( "kheetun" );
        setIconImage( Imx.CONFIG.getImage() );
        
        panel = new JPanel();
        panel.setDoubleBuffered( true );
        panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
        panel.setBorder( new LineBorder( Color.GRAY ) );
        
        this.getContentPane().setLayout( new BoxLayout( this.getContentPane(), BoxLayout.PAGE_AXIS ) );
        this.getContentPane().add( panel );

        labelKheetun    = new KTMenuItem( Imx.KHEETUN.icon, "" );
        labelKheetun.setStatus( kheetun.VERSION, Color.GRAY );
        
        labelConnected  = new KTMenuItem( Imx.NONE, "Daemon:" );
        labelConnected.setStatus( "disconnected", Color.RED );
        labelConnected.setActive( false );
        
        labelConfig     = new KTMenuItem( Imx.NONE, "Global config:" );
        labelConfig.setStatus( "none", Color.LIGHT_GRAY );
        
        itemExit = new KTMenuItem( Imx.EXIT, "Exit" ) {
            
            @Override
            public void leftClick(MouseEvent e) {

                TunnelManager.quit();
            }
        };
        
        itemStopAll = new KTMenuItem( Imx.STOP, "Stop all" ) {
        
            @Override
            public void leftClick(MouseEvent e) {

                TunnelManager.stopAllTunnels();
            }
        };
        
        itemAutostartAll = new KTMenuItem( Imx.START, "Autostart all" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                TunnelManager.resetAllFailures();
                TunnelManager.enableAutostartAll();
            }
        };
        
        itemQuery = new KTMenuItem( Imx.RELOAD, "Requery" ) {
            
            @Override
            public void leftClick(MouseEvent e) {

                TunnelClient.sendQueryTunnels();
            }
        };
        
        this.setAlwaysOnTop( true );
        this.setType( Type.POPUP );
        
        this.buildMenu( null );
        
        this.addMouseListener( this );
        panel.addMouseListener( this );
            
        this.setFocusableWindowState( true );
        this.setFocusable( true );
        
        TunnelManager.addTunnelManagerListener( this );
        ConfigManager.addConfigManagerListener( this );
    }
    
    public void buildMenu( Config config ) {
        
        TrayManager.clearMessages();
        
        panel.removeAll();
        
        panel.add( labelKheetun );
        panel.add( new KTSeperator() );
        panel.add( labelConnected );
        panel.add( labelConfig );
        panel.add( new KTSeperator() );
        panel.add( itemStopAll );
        panel.add( itemAutostartAll );
        panel.add( itemQuery );
        panel.add( new KTSeperator() );
        
        if ( config != null ) {
            for ( Profile profile : config.getProfiles() ) {
                
                KTMenuItem itemProfile = new KTMenuItem( Imx.PROFILE, profile.getName() );
                itemProfile.setStatus( "[" + profile.getConfigFile() + "]", new Color( 0, 100, 0 ) );
                
                if ( ! profile.getErrors().isEmpty() ) {
                    
                    String message = "<html><body>Configuration errors:";
                    
                    for ( String error : profile.getErrors() ) {
                        
                        message += "<br>    * " + error;
                    }
                    
                    message += "</body></html>";
                    
                    itemProfile.setMessage( message );
                    itemProfile.setStatus( "[" + profile.getConfigFile() + "]", Color.RED );
                    
                    TrayManager.blink();
                }
                
                panel.add( itemProfile );
                
                for ( Tunnel tunnel : profile.getTunnels() ) {
                    
                    TunnelMenuItem itemTunnel = new TunnelMenuItem( tunnel );
                    
                    if ( ! profile.getErrors().isEmpty() ) {
                        
                        itemTunnel.setActive( false );
                    }
                    
                    panel.add( itemTunnel );
                }
                panel.add( new KTSeperator() );
            }
        }
        
        panel.add( itemExit );
        
        for ( Component c : panel.getComponents() ) {
            c.addMouseListener( this );
        }
        
        this.revalidate();
        this.pack();
        this.repaint();
    }
    
    @Override
    public void configManagerConfigChanged( Config config, boolean valid ) {

        buildMenu( config );
        
        if ( valid ) {
            
            labelConfig.setMessage( null );
            labelConfig.setStatus( "valid", new Color( 0, 100, 0 ) );
            
        } else {
            
            String message = "<html><body>Configuration errors:";
            
            for ( String error : config.getErrors() ) {
                
                message += "<br>    * " + error;
            }
            
            message += "</body></html>";
            
            labelConfig.setMessage( message );
            labelConfig.setStatus( "error", Color.RED );
            
            TrayManager.blink();
        }
    }

    public void toggle( Point p ) {
        
        if ( isVisible() ) {
            
            setVisible( false );
            
        } else {
            
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            
            if ( p.x + this.getWidth() > d.width ) {
                p.x = d.width - this.getWidth();
            }
            
            if ( p.y + this.getHeight() > d.height ) {
                p.y = d.height - this.getHeight();
            }
            
            this.setLocation( p );
            this.setVisible( true );
            this.requestFocus();
        }
    }
    
    @Override
    public void tunnelManagerOnline() {
        
        labelConnected.setStatus( "connected", new Color( 0, 100, 0 ) );
    }
    
    @Override
    public void tunnelManagerOffline() {
        
        labelConnected.setStatus( "disconnected", Color.RED );
    }
    
    @Override
    public void tunnelManagerTunnelActivated( Tunnel tunnel ) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void tunnelManagerTunnelActivating( Tunnel tunnel ) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void tunnelManagerTunnelDeactivated( Tunnel tunnel ) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerTunnelDeactivating( Tunnel tunnel ) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerAutostartHostAvailable( Tunnel tunnel ) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerAutostartHostUnavailable( Tunnel tunnel ) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerTunnelError( Tunnel tunnel, String error ) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerTunnelPing( Tunnel tunnel, long ping ) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerAutostartDisabled(Tunnel tunnel) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerAutostartEnabled(Tunnel tunnel) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        
        if ( panel.isShowing() ) {
            Rectangle rect = new Rectangle( panel.getLocationOnScreen() );
            rect.setSize( panel.getSize() );
            
            if ( ! rect.contains( e.getLocationOnScreen() ) ) {
                setVisible( false );
            }
        }
    }
}
class KTSeperator extends JComponent {
    
    private static final long serialVersionUID = 1L;
    
    public KTSeperator() {
        
        this.setAlignmentX( Component.LEFT_ALIGNMENT );
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor( Color.LIGHT_GRAY );
        g.drawLine( 0, 0, this.getWidth(), 0 );
    }
}

class KTMenuItem extends JPanel implements MouseListener {

    private static final long serialVersionUID = 1L;

    protected JLabel        iconLeft            = new JLabel( Imx.NONE );
    protected JLabel        iconRight           = new JLabel( Imx.NONE );
    protected JLabel        text                = new JLabel( "" );
    protected JLabel        status              = new JLabel( "", JLabel.RIGHT );
    
    protected AnImx         processingLeft      = new AnImx( "loading.png", 50, 125, 16 );
    protected AnImx         processingRight     = new AnImx( "loading.png", 50, 125, 16 );
    
    protected Color         colorBackground;

    protected JLabel        message             = new JLabel( Imx.WARNING );
    protected JLabel        clipboardHint       = new JLabel( "[Click middle mouse button to copy this message]", Imx.NONE, JLabel.LEFT );
    protected JWindow       window              = new JWindow();
    
    protected boolean       active              = true;
    
    public KTMenuItem() {
    }
    
    public KTMenuItem( Icon iconLeft, Icon iconRight, String text, String status ) {
        
        this.initComponents();
        
        this.iconLeft.setIcon( iconLeft );
        this.iconRight.setIcon( iconRight );
        this.text.setText( text );
        this.status.setText( status );
    }
    
    public KTMenuItem( Icon iconLeft, String text ) {
        
        this.initComponents();
        
        this.iconLeft.setIcon( iconLeft );
        this.text.setText( text );
    }
    
    protected void initComponents() {
        
        UIDefaults defaults = javax.swing.UIManager.getDefaults();
        this.colorBackground = defaults.getColor( "List.selectionBackground" );
            
        this.setOpaque( true );
        this.setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
        this.setBorder( new EmptyBorder( 1, 8, 0, 8 ) );
        this.setAlignmentX( Container.LEFT_ALIGNMENT );
        
        this.iconLeft.setBorder( new EmptyBorder( new Insets( 0, 0, 0, 4 ) ) );
        
        this.status.setForeground( Color.LIGHT_GRAY );
        this.status.setBorder( new EmptyBorder( new Insets( 0, 8, 0, 4 ) ) );
        this.status.setAlignmentX( Component.RIGHT_ALIGNMENT );
        this.status.setPreferredSize( new Dimension( 120, this.status.getPreferredSize().height ) );
        
        this.text.setOpaque( true );
        this.text.setBorder( new EmptyBorder( new Insets( 4, 8, 4, 0 ) ) );
        
        this.processingLeft.setVisible( false );
        this.processingRight.setVisible( false );
        
        this.iconRight.addMouseListener( this );
        this.iconLeft.addMouseListener( this );

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
        
        this.window.setLocation( this.getLocation() );
        this.window.getContentPane().setLayout( new BoxLayout( this.window.getContentPane(), BoxLayout.PAGE_AXIS ) );
        this.window.getContentPane().add( panel );
        this.window.setAlwaysOnTop( true );
        this.window.setType( Type.POPUP );
    
        this.add( this.iconLeft );
        this.add( this.processingLeft );
        this.add( this.iconRight );
        this.add( this.processingRight );
        this.add( this.text );
        this.add( Box.createHorizontalGlue() );
        this.add( Box.createHorizontalStrut( 32 ) );
        this.add( this.status );
        
        this.addMouseListener( this );
    }
    
    public void setActive( boolean active ) {
        this.active = active;
        
        if ( ! active ) {
            this.setBackground( null );
            text.setBackground( null );            
        }
    }
    
    public void setStatus( String status, Color color ) {
        
        this.status.setText( status );
        this.status.setForeground( color );
    }
    
    public void setProcessingLeft( boolean state ) {
        
        processingLeft.setVisible( state );
        iconLeft.setVisible( ! state );
    }
    
    public void setProcessingRight( boolean state ) {
        
        processingRight.setVisible( state );
        iconRight.setVisible( ! state );
    }
    
    public void setMessage( String message ) {
        
        if ( message == null ) {
            this.message.setText( null );
            this.iconRight.setIcon( Imx.NONE );
            this.iconRight.setToolTipText( null );
            TrayManager.clearMessage( this.toString() );
        } else {
            this.message.setText( message );
            this.iconRight.setIcon( Imx.WARNING );
            this.setProcessingRight( false );
            TrayManager.setMessage( this.toString(), message );
        }
    }
    
    public void leftClick( MouseEvent e ) {
        
    }
    
    @Override
    public void mouseEntered( MouseEvent e ) {
        
        if ( ! this.active ) {
            return;
        }
        
        if ( iconRight.isVisible() && ( iconRight.getIcon() == Imx.WARNING || iconRight.getIcon() == Imx.WARNING_DISABLED ) ) {
            
            iconRight.setIcon( Imx.WARNING_DISABLED );
            
            
            Point p = this.getLocationOnScreen();
            
            if ( p.x - window.getWidth() < 0 ) {
                p.setLocation( p.x + this.getWidth() - 16, p.y );
            } else {
                p.setLocation( p.x - window.getWidth() + 16, p.y );
            }
            
            window.pack();
            window.setLocation( p );
            window.setVisible( true );
        }
        
        this.setBackground( colorBackground );
        text.setBackground( colorBackground );
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        
        if ( window.isVisible() && e.getComponent() != this.window ) {
            window.setVisible( false );
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
            
            this.leftClick( e );
        }
        
        if ( e.getButton() == MouseEvent.BUTTON2 ) {
            
            if ( iconRight.isVisible() && ( iconRight.getIcon() == Imx.WARNING || iconRight.getIcon() == Imx.WARNING_DISABLED ) ) {
                
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
            
            if ( iconRight.isVisible() && ( iconRight.getIcon() == Imx.WARNING || iconRight.getIcon() == Imx.WARNING_DISABLED ) ) {
                
                this.clipboardHint.setForeground( Color.BLACK );
                return;
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {

        if ( e.getButton() == MouseEvent.BUTTON2 ) {
            
            if ( iconRight.isVisible() && ( iconRight.getIcon() == Imx.WARNING || iconRight.getIcon() == Imx.WARNING_DISABLED ) ) {
                
                this.clipboardHint.setForeground( Color.GRAY );
                return;
            }
        }
    }
}


class TunnelMenuItem extends KTMenuItem implements TunnelManagerListener {
    
    private static final long serialVersionUID = 1L;
    
    protected Tunnel    tunnel;
    
    public TunnelMenuItem( Tunnel tunnel ) {
        
        this.add( Box.createHorizontalStrut( 20 ) );
        this.initComponents();
        
        this.tunnel = tunnel;
        
        this.iconLeft.setIcon( tunnel.getAutostart() ? Imx.AUTO : Imx.NONE );
        this.iconRight.setIcon( Imx.NONE );
        this.text.setText( tunnel.getAlias() );
        
        this.setStatus( "inactive", Color.LIGHT_GRAY );
        
        TunnelManager.addTunnelManagerListener( this );
    }
    
    @Override
    public void leftClick( MouseEvent e ) {
        
        if ( e.getComponent() == iconLeft && this.tunnel.getAutostart() ) {
            
            if ( iconLeft.getIcon() == Imx.AUTO ) {

                TunnelManager.disableAutostart( this.tunnel );
                
            } else {
                
                tunnel.setFailures( 0 );
                TunnelManager.enableAutostart( this.tunnel );
            }
            return;
        }
        
        if ( ! TunnelManager.isConnected() || TunnelManager.isBusy( tunnel ) ) {
            return;
        }
        
        if ( TunnelManager.isRunning( tunnel ) ) {
            
            TunnelManager.disableAutostart( tunnel );
            TunnelManager.stopTunnel( tunnel );
        } else {
            
            tunnel.setFailures( 0 );
            TunnelManager.enableAutostart( tunnel );
            TunnelManager.startTunnel( tunnel );
        }
    }
    
    @Override
    public void tunnelManagerOnline() {
        
        text.setForeground( Color.BLACK );
        
        if ( tunnel.getAutostart() && ! TunnelManager.isAutostartDisabled( this.tunnel ) ) {
            
            this.tunnelManagerAutostartHostUnavailable( tunnel );
        }
    }
    
    @Override
    public void tunnelManagerOffline() {
        
        text.setForeground( Color.LIGHT_GRAY );
        
        tunnelManagerTunnelDeactivated( tunnel );
    }
    
    @Override
    public void tunnelManagerTunnelActivated( Tunnel tunnel ) {

        if ( tunnel.equals( this.tunnel ) ) {
            
            iconRight.setIcon( Imx.ACTIVE );
            
            this.setProcessingRight( false );
            
            this.setStatus( "connected", Color.GRAY );
        }
    }
    
    @Override
    public void tunnelManagerTunnelDeactivated( Tunnel tunnel ) {

        if ( tunnel.equals( this.tunnel ) ) {
            
            if ( iconRight.getIcon() != Imx.WARNING ) {
                iconRight.setIcon( Imx.INACTIVE );
            }
            
            this.setProcessingRight( false );
            
            this.setStatus( "inactive", Color.LIGHT_GRAY );
        }
    }
    
    
    @Override
    public void tunnelManagerTunnelPing( Tunnel tunnel, long ping) {

        if ( tunnel.equals( this.tunnel ) ) {

            iconRight.setIcon( Imx.ACTIVE );
            iconRight.setVisible( true );

            if ( ping < 0 ) {
                
                this.setStatus( "failing (" + String.valueOf( -ping ) + "/3)", Color.RED );

            } else {
                
                this.setStatus( String.valueOf( ping ) + " ms", Color.GRAY );
            }
        }
    }
    
    @Override
    public void tunnelManagerTunnelError( Tunnel tunnel, String error ) {
        
        if ( tunnel.equals( this.tunnel ) ) {
            
            tunnelManagerTunnelDeactivated( tunnel );
            
            TrayManager.blink();
            
            iconRight.setIcon( Imx.WARNING );
            
            message.setText( error );
        }
    }
    
    @Override
    public void tunnelManagerAutostartHostAvailable( Tunnel tunnel ) {

        if ( tunnel.equals( this.tunnel ) ) {
            
            this.setStatus( "autostart " + (tunnel.getFailures() + 1) + "/" + tunnel.getMaxFailures() , Color.GRAY );
        }
    }
    
    @Override
    public void tunnelManagerAutostartHostUnavailable( Tunnel tunnel ) {
        
        if ( tunnel.equals( this.tunnel ) ) {
            
            this.setStatus( "waiting",  Color.GRAY );
        }
    }
    
    @Override
    public void tunnelManagerTunnelActivating( Tunnel tunnel ) {
        
        if ( tunnel.equals( this.tunnel ) ) {
            
            this.setProcessingRight( true );
        }
    }
    
    @Override
    public void tunnelManagerTunnelDeactivating( Tunnel tunnel ) {

        if ( tunnel.equals( this.tunnel ) ) {
            
            this.setProcessingRight( true );
        }
    }
    
    @Override
    public void tunnelManagerAutostartDisabled( Tunnel tunnel ) {
        
        if ( tunnel.equals( this.tunnel ) ) {
            
            iconLeft.setIcon( Imx.AUTO_DISABLED );
            
            if ( ! TunnelManager.isRunning( this.tunnel ) ) {
                tunnelManagerTunnelDeactivated( this.tunnel );
            }
        }
    }
    
    @Override
    public void tunnelManagerAutostartEnabled(Tunnel tunnel) {

        if ( tunnel.equals( this.tunnel ) ) {
            
            iconLeft.setIcon( Imx.AUTO );
        }
    }
    
    
}





















