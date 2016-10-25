package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
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
    
    private JLabel                          labelKheetun;
    private JLabel                          labelConnected;
    private JLabel                          labelStatus;
    private JLabel                          labelMessage;
    private JWindow                         windowMessage;
    private JMenuItem                       itemExit;
    private JMenuItem                       itemQuery;
    private JMenuItem                       itemStopAll;
    private HashMap<TunnelMenuItem, Tunnel> tunnelByItem        = new HashMap<TunnelMenuItem, Tunnel>();
    private JPopupMenuEx                    menu;
    private JPanel                          panel;
    private boolean                         connected           = false;
    
    public TrayMenu() {
        
        setName( "kheetun" );
        setIconImage( Imx.CONFIG.getImage() );
        
        panel = new JPanel();
        panel.setDoubleBuffered( true );
        
        menu = new JPopupMenuEx();
        menu.setVisible( true );
        menu.setEnabled( true );
        menu.setLayout( new BoxLayout( menu, BoxLayout.PAGE_AXIS ) );
        
        menu.setBorder( new EmptyBorder( new Insets( 8, 8, 8, 8 ) ) );

        panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
        
        panel.setBorder( new LineBorder( Color.GRAY ) );
        panel.add( menu );
        
        this.getContentPane().setLayout( new BoxLayout( this.getContentPane(), BoxLayout.PAGE_AXIS ) );
        this.getContentPane().add( panel );

        labelKheetun = new JLabel( "kheetun v" + kheetun.VERSION, Imx.KHEETUN, JLabel.LEADING );
        labelKheetun.setIconTextGap( 8 );
        labelKheetun.setBorder( new EmptyBorder( new Insets( 0, 10, 0, 0 ) ) );
        labelKheetun.setAlignmentX( Component.LEFT_ALIGNMENT );
        labelKheetun.setFont( new Font( labelKheetun.getFont().getName(), Font.BOLD, labelKheetun.getFont().getSize() + 1 ) );
        
        labelConnected = new JLabel( "disconnected", Imx.NONE, JLabel.LEADING );
        labelConnected.setIconTextGap( 8 );
        labelConnected.setForeground( Color.RED );
        labelConnected.setAlignmentX( Component.LEFT_ALIGNMENT );
        labelConnected.setBorder( new EmptyBorder( new Insets( 4, 10, 0, 0 ) ) );
        labelConnected.addMouseListener( this );
        
        labelStatus = new JLabel( "", Imx.NONE, JLabel.LEADING );
        labelStatus.setIconTextGap( 8 );
        labelStatus.setForeground( Color.RED );
        labelStatus.setAlignmentX( Component.LEFT_ALIGNMENT );
        labelStatus.setBorder( new EmptyBorder( new Insets( 4, 10, 8, 0 ) ) );
        labelStatus.addMouseListener( this );

        labelMessage = new JLabel( Imx.WARNING );
        labelMessage.setIconTextGap( 8 );
        labelMessage.setBorder( new CompoundBorder( new LineBorder( Color.RED ), new EmptyBorder( new Insets( 4, 4, 4, 4 ) ) ) );
        labelMessage.setForeground( Color.BLACK );
        
        windowMessage = new JWindow();
        windowMessage.setLocation( this.getLocation() );
        windowMessage.getContentPane().setLayout( new BoxLayout( windowMessage.getContentPane(), BoxLayout.PAGE_AXIS ) );
        windowMessage.getContentPane().add( labelMessage );
        windowMessage.setAlwaysOnTop( true );
        windowMessage.setType( Type.POPUP );
        windowMessage.setVisible( false );
        
        itemExit = new JMenuItem( "Exit", Imx.EXIT );
        itemExit.setIconTextGap( 8 );
        itemExit.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                TunnelManager.quit();
            }
        });
        itemExit.addMouseListener( this );
        
        itemStopAll = new JMenuItem( "Stop all", Imx.STOP );
        itemStopAll.setIconTextGap( 8 );
        itemStopAll.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                for( Tunnel tunnel : tunnelByItem.values() ) {
                    
                    TunnelManager.stopTunnel( tunnel );
                }
            }
        });
        itemStopAll.addMouseListener( this );
        
        itemQuery = new JMenuItem( "Requery", Imx.RELOAD );
        itemQuery.setIconTextGap( 8 );
        itemQuery.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                TunnelClient.sendQueryTunnels();
            }
        });
        itemQuery.addMouseListener( this );

        setAlwaysOnTop( true );
        setType( Type.POPUP );
        
        buildMenu( null );
        
        this.addMouseListener( this );
        panel.addMouseListener( this );
        menu.addMouseListener( this );
            
        setFocusableWindowState( true );
        setFocusable( true );
        
        TunnelManager.addTunnelManagerListener( this );
        ConfigManager.addConfigManagerListener( this );
    }
    
    public void buildMenu( Config config ) {
        
        menu.removeAll();
        
        tunnelByItem.clear();

        menu.add( labelKheetun );
        menu.add( labelConnected );
        menu.add( labelStatus );
        menu.addSeparator();
        menu.add( itemStopAll );
        menu.add( itemQuery );
        
        if ( config != null ) {
            for ( Profile profile : config.getProfiles() ) {
                
                menu.addSeparator();
                menu.add( Box.createVerticalStrut( 4 ) );
                JLabel labelProfile = new JLabel( profile.getName(), Imx.PROFILE, JLabel.TRAILING );
                labelProfile.setIconTextGap( 8 );
                labelProfile.setAlignmentX( Component.LEFT_ALIGNMENT );
                labelProfile.setFont( new Font( labelProfile.getFont().getName(), Font.BOLD, labelProfile.getFont().getSize() ) );
                labelProfile.setBorder( new EmptyBorder( new Insets( 0, 10, 8, 0 ) ) );
                labelProfile.addMouseListener( this );
                menu.add( labelProfile );
                
                for ( Tunnel tunnel : profile.getTunnels() ) {
                    
                    menu.add( Box.createVerticalStrut( 2 ) );
                    
                    TunnelMenuItem itemTunnel = new TunnelMenuItem( tunnel );
                    itemTunnel.setBorder( new EmptyBorder( new Insets( 0, 20, 0, 0 ) ) );
                    itemTunnel.setEnabled( connected );
                    itemTunnel.addMouseListener( this );
                    itemTunnel.setAlignmentX( Component.LEFT_ALIGNMENT );
                    
                    menu.add( itemTunnel );
                    
                    tunnelByItem.put( itemTunnel, tunnel );
                }
            }
        }
        
        menu.add( Box.createVerticalStrut( 4 ) );
        menu.addSeparator();
        menu.add( itemExit );
        menu.add( Box.createVerticalStrut( 4 ) );
        
        this.revalidate();
        this.pack();
        this.repaint();
        
        if ( connected ) {
            TunnelClient.sendQueryTunnels();
        }
    }
    
    @Override
    public void configManagerConfigChanged(Config config) {

        buildMenu( config );
    }
    
    @Override
    public void configManagerConfigInvalid(Config config, ArrayList<String> errorStack) {
        
        String message = "<html><body>Configuration errors:";
        
        for ( String error : errorStack ) {
            
            message += "<br>    * " + error;
        }
        
        message += "</body></html>";
        
        labelMessage.setText( message );
        labelStatus.setIcon( Imx.WARNING );
        labelStatus.setText( "config error" );
        labelStatus.setForeground( Color.RED );
        
        TrayManager.blink();
    }
    
    @Override
    public void configManagerConfigValid(Config config) {
        
        labelStatus.setIcon( Imx.NONE );
        labelStatus.setText( "config valid" );
        labelStatus.setForeground( new Color( 0, 100, 0 ) );
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
            
            setLocation( p );
            setVisible( true );
            requestFocus();
            repaint();
        }
    }
    
    @Override
    public void tunnelManagerOnline() {
        labelConnected.setText( "connected to daemon" );
        labelConnected.setForeground( new Color( 0, 100, 0 ) );
    }
    
    @Override
    public void tunnelManagerOffline() {
        labelConnected.setText( "disconnected" );
        labelConnected.setForeground( Color.RED );
    }
    
    @Override
    public void tunnelManagerTunnelActivated(String signature) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void tunnelManagerTunnelActivating(String signature) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void tunnelManagerTunnelDeactivated(String signature) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerTunnelDeactivating(String signature) {
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
    public void tunnelManagerTunnelError(String signature, String error) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelManagerTunnelPing(String signature, long ping) {
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
        
        if ( e.getComponent().equals( labelStatus ) && ( labelStatus.getIcon() == Imx.WARNING || labelStatus.getIcon() == Imx.WARNING_DISABLED ) ) {
            
            labelStatus.setIcon( Imx.WARNING_DISABLED );
            
            windowMessage.pack();
            
            Point p = labelStatus.getLocationOnScreen();
            
            if ( p.x - windowMessage.getWidth() < 0 ) {
                p.setLocation( p.x + this.getWidth(), p.y );
            } else {
                p.setLocation( p.x - windowMessage.getWidth(), p.y );
            }
            
            windowMessage.setLocation( p );
            windowMessage.setVisible( true );
        }
    }

    public void mouseExited(MouseEvent e) {
        
        if ( e.getComponent().equals( labelStatus ) && windowMessage.isVisible() ) {
            
            windowMessage.setVisible( false );
        }

        Rectangle rect = new Rectangle( panel.getLocationOnScreen() );
        rect.setSize( panel.getSize() );
        
        if ( ! rect.contains( e.getLocationOnScreen() ) ) {
            setVisible( false );
        }
    }
}


class TunnelMenuItem extends JPanel implements MouseListener, TunnelManagerListener {
    
    public static final long serialVersionUID = 42;
    
    private JLabel    labelAuto;
    private JLabel    labelTunnel;
    private JLabel    labelIcon;
    private JLabel    labelStatus;
    private JLabel    labelMessage;
    private JWindow   windowMessage;
    private AnImx     anImxProcessing = new AnImx( "loading.png", 50, 125, 16 );
    private Color     colorBackground;
    private Tunnel    tunnel;
    
    public TunnelMenuItem( Tunnel tunnel ) {
        
        this.tunnel = tunnel;
        
        UIDefaults defaults = javax.swing.UIManager.getDefaults();
        colorBackground = defaults.getColor("List.selectionBackground");
            
        setOpaque( true );
        setBorder( new EmptyBorder( new Insets( 0, 0, 0, 0 ) ) );
        this.setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
        
        labelAuto   = new JLabel( tunnel.getAutostart() ? Imx.AUTO : Imx.NONE );
        labelTunnel = new JLabel( tunnel.getAlias() );
        labelIcon   = new JLabel( Imx.INACTIVE );
        labelStatus = new JLabel( "inactive", Label.RIGHT );
        
        labelAuto.setToolTipText( "Autostart" );
        labelAuto.setBorder( new EmptyBorder( new Insets( 0, 0, 0, 4 ) ) );
        labelAuto.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
        
        labelStatus.setForeground( Color.LIGHT_GRAY );
        labelStatus.setBorder( new EmptyBorder( new Insets( 0, 8, 0, 0 ) ) );
        labelStatus.setAlignmentX( Component.RIGHT_ALIGNMENT );
        
        labelTunnel.setOpaque( true );
        labelTunnel.setBorder( new EmptyBorder( new Insets( 2, 8, 2, 0 ) ) );
        
        anImxProcessing.setVisible( false );
        labelIcon.setVisible( true );
        
        labelIcon.addMouseListener( this );
        labelAuto.addMouseListener( this );
        
        labelMessage = new JLabel( Imx.WARNING );
        labelMessage.setIconTextGap( 8 );
        labelMessage.setBorder( new CompoundBorder( new LineBorder( Color.RED ), new EmptyBorder( new Insets( 4, 4, 4, 4 ) ) ) );
        labelMessage.setForeground( Color.BLACK );
        
        windowMessage = new JWindow();
        windowMessage.setLocation( this.getLocation() );
        windowMessage.getContentPane().setLayout( new BoxLayout( windowMessage.getContentPane(), BoxLayout.PAGE_AXIS ) );
        windowMessage.getContentPane().add( labelMessage );
        windowMessage.setAlwaysOnTop( true );
        windowMessage.setType( Type.POPUP );

        add( labelAuto );
        add( anImxProcessing );
        add( labelIcon );
        add( labelTunnel );
        add( Box.createHorizontalGlue() );
        add( labelStatus );
        
        addMouseListener( this );
        TunnelManager.addTunnelManagerListener( this );
    }
    
    @Override
    public void tunnelManagerOnline() {
        
        labelTunnel.setForeground( Color.BLACK );
        
        if ( tunnel.getAutostart() && ! TunnelManager.isAutostartDisabled( this.tunnel ) ) {
            
            this.tunnelManagerAutostartHostUnavailable( tunnel );
        }
    }
    
    @Override
    public void tunnelManagerOffline() {
        
        labelTunnel.setForeground( Color.LIGHT_GRAY );
        
        tunnelManagerTunnelDeactivated( tunnel.getSignature() );
    }
    
    @Override
    public void tunnelManagerTunnelActivated( String signature ) {

        if ( signature.equals( tunnel.getSignature() ) ) {
            
            labelIcon.setIcon( Imx.ACTIVE );
            labelIcon.setVisible( true );
            anImxProcessing.setVisible( false );

            labelStatus.setText( "connected" );
            labelStatus.setForeground( Color.GRAY );
        }
    }
    
    @Override
    public void tunnelManagerTunnelDeactivated( String signature ) {

        if ( signature.equals( tunnel.getSignature() ) ) {
            
            labelIcon.setIcon( Imx.INACTIVE );
            labelIcon.setVisible( true );
            anImxProcessing.setVisible( false );
            labelStatus.setText( "inactive" );
            labelStatus.setForeground( Color.LIGHT_GRAY );
        }
    }
    
    
    @Override
    public void tunnelManagerTunnelPing(String signature, long ping) {

        if ( signature.equals( tunnel.getSignature() ) ) {

            labelIcon.setIcon( Imx.ACTIVE );
            labelIcon.setVisible( true );

            if ( ping < 0 ) {

                labelStatus.setText( "failing (" + String.valueOf( -ping ) + "/3)" );
                labelStatus.setForeground( Color.RED );

            } else {
                
                labelStatus.setText( String.valueOf( ping ) + " ms" );
                labelStatus.setForeground( Color.GRAY );
            }
        }
    }
    
    @Override
    public void tunnelManagerTunnelError(String signature, String error) {
        
        if ( this.tunnel.getSignature().equals( signature ) ) {
            
            tunnelManagerTunnelDeactivated( tunnel.getSignature() );
            
            TrayManager.blink();
            
            labelIcon.setIcon( Imx.WARNING );
            
            labelMessage.setText( error );
        }
    }
    
    @Override
    public void tunnelManagerAutostartHostAvailable( Tunnel tunnel ) {

        if ( tunnel.getSignature().equals( this.tunnel.getSignature() ) ) {
            labelStatus.setText( "autostarting" );
            labelStatus.setForeground( Color.GRAY );
        }
    }
    
    @Override
    public void tunnelManagerAutostartHostUnavailable( Tunnel tunnel ) {
        
        if ( tunnel.getSignature().equals( this.tunnel.getSignature() ) ) {
            labelStatus.setText( "waiting" );
            labelStatus.setForeground( Color.GRAY );
        }
    }
    
    @Override
    public void tunnelManagerTunnelActivating(String signature) {
        
        if ( tunnel.getSignature().equals( signature ) ) {
            labelIcon.setVisible( false );
            anImxProcessing.setVisible( true );
        }
    }
    
    @Override
    public void tunnelManagerTunnelDeactivating(String signature) {

        if ( tunnel.getSignature().equals( signature ) ) {
            labelIcon.setVisible( false );
            anImxProcessing.setVisible( true );
        }
    }
    
    @Override
    public void tunnelManagerAutostartDisabled(Tunnel tunnel) {
        
        if ( tunnel.getSignature().equals( this.tunnel.getSignature() ) ) {
            
            labelAuto.setIcon( Imx.AUTO_DISABLED );
            
            if ( ! TunnelManager.isRunning( this.tunnel.getSignature() ) ) {
                tunnelManagerTunnelDeactivated( this.tunnel.getSignature() );
            }
        }
    }
    
    @Override
    public void tunnelManagerAutostartEnabled(Tunnel tunnel) {

        if ( tunnel.getSignature().equals( this.tunnel.getSignature() ) ) {
            
            labelAuto.setIcon( Imx.AUTO );
        }
    }
    
    @Override
    public void mouseEntered( MouseEvent e ) {
        
        if ( labelIcon.isVisible() && ( labelIcon.getIcon() == Imx.WARNING || labelIcon.getIcon() == Imx.WARNING_DISABLED ) ) {
            
            labelIcon.setIcon( Imx.WARNING_DISABLED );
            
            windowMessage.pack();
            
            Point p = this.getLocationOnScreen();
            
            if ( p.x - windowMessage.getWidth() < 0 ) {
                p.setLocation( p.x + this.getWidth(), p.y );
            } else {
                p.setLocation( p.x - windowMessage.getWidth(), p.y );
            }
            
            windowMessage.setLocation( p );
            windowMessage.setVisible( true );
        }
        
        setBackground( colorBackground );
        labelTunnel.setBackground( colorBackground );
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        
        if ( windowMessage.isVisible() ) {

            windowMessage.setVisible( false );
        }
        
        setBackground( null );
        labelTunnel.setBackground( null );
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        
        if ( e.getComponent() == labelAuto && this.tunnel.getAutostart() ) {
            
            if ( labelAuto.getIcon() == Imx.AUTO ) {

                TunnelManager.disableAutostart( this.tunnel );
                
            } else {
                
                TunnelManager.enableAutostart( this.tunnel );
            }
            return;
        }
        
        if ( ! TunnelManager.isConnected() || TunnelManager.isBusy( tunnel ) ) {
            return;
        }
        
        labelIcon.setVisible( false );
        anImxProcessing.setVisible( true );
        
        if ( TunnelManager.isRunning( tunnel.getSignature() ) ) {
            
            TunnelManager.stopTunnel( tunnel );
        } else {
            
            TunnelManager.startTunnel( tunnel );
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
}




class JPopupMenuEx extends JPopupMenu implements MouseListener {

    private static final long serialVersionUID = 42;

    @Override
    public void addSeparator() {
        add(new JSeparatorEx());
    }

    @Override
    public JMenuItem add(JMenuItem menuItem) {
        menuItem.addMouseListener(this);
        return super.add(menuItem);
    }

    public void mouseEntered(MouseEvent e) {
        ((JMenuItem)e.getSource()).setArmed(true);
    }

    public void mouseExited(MouseEvent e) {
        ((JMenuItem)e.getSource()).setArmed(false);
    }

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public class JSeparatorEx extends JSeparator {

        /**
         * 
         */
        private static final long serialVersionUID = 3477309905456341629L;

        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();

            if (d.height==0)
                d.height = 4;

            return d;
        }
    }
}

















