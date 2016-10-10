package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.HostPingDaemon;
import org.khee.kheetun.client.HostPingDaemonListener;
import org.khee.kheetun.client.TunnelClient;
import org.khee.kheetun.client.TunnelClientListener;
import org.khee.kheetun.client.kheetun;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;

public class TrayMenu extends JWindow implements MouseListener, ConfigFrameListener, TunnelClientListener {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    public static final long serialVersionUID = 42;
    
    private JLabel                          labelKheetun;
    private JLabel                          labelConnected;
    private JMenuItem                       itemExit;
    private JMenuItem                       itemConfig;
    private JMenuItem                       itemQuery;
    private JMenuItem                       itemStopAll;
    private ConfigFrame                     configFrame;
    private HashMap<TunnelMenuItem, Tunnel> tunnelByItem        = new HashMap<TunnelMenuItem, Tunnel>();
    private JPopupMenuEx                    menu;
    private JPanel                          panel;
    private boolean                         connected           = false;
    
    public TrayMenu( ConfigFrame frame ) {
        
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

        this.configFrame = frame;
        
        labelKheetun = new JLabel( "kheetun v" + kheetun.VERSION, Imx.KHEETUN, JLabel.LEADING );
        labelKheetun.setIconTextGap( 8 );
        labelKheetun.setBorder( new EmptyBorder( new Insets( 0, 10, 0, 0 ) ) );
        labelKheetun.setAlignmentX( Component.LEFT_ALIGNMENT );
        labelKheetun.setFont( new Font( labelKheetun.getFont().getName(), Font.BOLD, labelKheetun.getFont().getSize() + 1 ) );
        
        labelConnected = new JLabel( "disconnected", Imx.NONE, JLabel.LEADING );
        labelConnected.setIconTextGap( 8 );
        labelConnected.setForeground( Color.RED );
        labelConnected.setAlignmentX( Component.LEFT_ALIGNMENT );
        labelConnected.setBorder( new EmptyBorder( new Insets( 0, 10, 8, 0 ) ) );
        
        itemExit = new JMenuItem( "Exit", Imx.EXIT );
        itemExit.setIconTextGap( 8 );
        itemExit.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                logger.info( "Exiting" );
                TunnelClient.disconnect();
                System.exit( 0 );
            }
        });
        itemExit.addMouseListener( this );
        
        itemConfig = new JMenuItem( "Configure", Imx.CONFIG );
        itemConfig.setIconTextGap( 8 );
        itemConfig.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                configFrame.setVisible( ! configFrame.isVisible() );
                
                if ( configFrame.isVisible() ) {
                    configFrame.revalidate();
                    configFrame.repaint();
                }
            }
        });
        itemConfig.addMouseListener( this );

        itemStopAll = new JMenuItem( "Stop all", Imx.STOP );
        itemStopAll.setIconTextGap( 8 );
        itemStopAll.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                for( Tunnel tunnel : tunnelByItem.values() ) {
                    
                    TunnelClient.sendStopTunnel( tunnel );
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
        
        configFrame.addConfigChangedListener( this );
        
        TunnelClient.addClientListener( this );
    }
    
    public void buildMenu( Config config ) {
        
        menu.removeAll();
        
        tunnelByItem.clear();

        menu.add( labelKheetun );
        menu.add( labelConnected );
        menu.addSeparator();
        menu.add( itemConfig );
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
//                    itemTunnel.setMinimumSize( new Dimension( panel.getWidth(), itemTunnel.getMinimumSize().height ) );
//                    itemTunnel.setMaximumSize( new Dimension( panel.getWidth(), itemTunnel.getMaximumSize().height ) );
                    
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

    public void configChanged(Config config) {
        
        buildMenu( config );
    }
    
    public void toggle( Point p ) {
        
        if ( isVisible() ) {
            
            setVisible( false );
            
        } else {
            
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width  = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();            
            
            if ( p.x + this.getWidth() > width ) {
                p.x = width - this.getWidth();
            }
            
            if ( p.y + this.getHeight() > height ) {
                p.y = height - this.getHeight();
            }
            
            setLocation( p );
            setVisible( true );
            requestFocus();
            repaint();
        }
    }
    
    @Override
    public void connected() {
        
        labelConnected.setText( "connected" );
        labelConnected.setForeground( new Color( 0, 100, 0 ) );
    }
    
    @Override
    public void disconnected() {
        labelConnected.setText( "disconnected" );
        labelConnected.setForeground( Color.RED );
    }
    
    @Override
    public void activeTunnels(ArrayList<String> signatures) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void error(Tunnel tunnel, String error) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelStarted(String signature) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelStopped(String signature) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void tunnelPing(String signature, long ping) {
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

        Rectangle rect = new Rectangle( panel.getLocationOnScreen() );
        rect.setSize( panel.getSize() );
        
        if ( ! rect.contains( e.getLocationOnScreen() ) ) {
            setVisible( false );
        }
    }
}


class TunnelMenuItem extends JPanel implements MouseListener, TunnelClientListener, HostPingDaemonListener {
    
    public static final long serialVersionUID = 42;
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    private JLabel    labelAuto;
    private JLabel    labelTunnel;
    private JLabel    labelIcon;
    private JLabel    labelPing;
    private JLabel    labelMs;
    private AnImx     anImxProcessing = new AnImx( "loading.png", 50, 125, 16 );
    private Color     colorBackground;
    private Tunnel    tunnel;
    private boolean   isActive;
    private boolean   activating        = false;
    private boolean   connected         = false;
    private boolean   stoppedManually   = false;
    
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
        labelPing   = new JLabel( "", Label.RIGHT );
        labelMs     = new JLabel( "inactive" );

        labelAuto.setToolTipText( "Autostart" );
        labelAuto.setBorder( new EmptyBorder( new Insets( 0, 0, 0, 4 ) ) );
        
        labelPing.setForeground( Color.GRAY );
        labelPing.setAlignmentX( Component.RIGHT_ALIGNMENT );
        
        labelMs.setForeground( Color.LIGHT_GRAY );
        labelMs.setBorder( new EmptyBorder( new Insets( 0, 8, 0, 0 ) ) );
        
        labelTunnel.setOpaque( true );
        labelTunnel.setBorder( new EmptyBorder( new Insets( 2, 8, 2, 0 ) ) );
        
        anImxProcessing.setVisible( false );
        labelIcon.setVisible( true );

        add( labelAuto );
        add( anImxProcessing );
        add( labelIcon );
        add( labelTunnel );
        add( Box.createHorizontalGlue() );
        add( labelPing );
        add( labelMs );
        
        addMouseListener( this );
        TunnelClient.addClientListener( this );
        HostPingDaemon.addHostPingDaemonListener( this );
    }
    
    @Override
    public void activeTunnels(ArrayList<String> signatures) {
        
        for ( String signature : signatures ) {
            
            if ( signature.equals( tunnel.getSignature() ) ) {
                
                tunnelStarted( signature );
            }
        }
    }
    
    @Override
    public void connected() {
        this.connected = true;
        labelTunnel.setForeground( Color.BLACK );
    }
    
    @Override
    public void disconnected() {
        this.connected = false;
        labelTunnel.setForeground( Color.LIGHT_GRAY );
        tunnelStopped( tunnel.getSignature() );
    }
    
    @Override
    public void tunnelStarted(String signature) {
        
        if ( signature.equals( tunnel.getSignature() ) ) {
            
            this.activating = false;
            this.isActive = true;
            labelIcon.setIcon( Imx.ACTIVE );
            labelIcon.setVisible( true );
            anImxProcessing.setVisible( false );
            labelMs.setText( "ms" );
            labelMs.setForeground( Color.GRAY );
            labelPing.setText( "..." );
        }
    }
    
    @Override
    public void tunnelStopped(String signature) {

        if ( signature.equals( tunnel.getSignature() ) ) {
            
            this.activating = false;
            this.isActive = false;
            labelIcon.setIcon( Imx.INACTIVE );
            labelIcon.setVisible( true );
            anImxProcessing.setVisible( false );
            labelMs.setText( "inactive" );
            labelMs.setForeground( Color.LIGHT_GRAY );
            labelPing.setText( "" );
        }
    }
    
    @Override
    public void tunnelPing(String signature, long ping) {
        
        if ( signature.equals( tunnel.getSignature() ) ) {

            if ( ping < 0 ) {

                labelPing.setText( "" );
                labelMs.setText( "failing" );
                labelMs.setForeground( Color.RED );
                
                if ( ping < -1 ) {
                    TrayManager.setState( Tray.STATE_WARNING );
                }
            
            } else {
                
                labelPing.setText( String.valueOf( ping ) );
                labelMs.setText( "ms" );
                labelMs.setForeground( Color.GRAY );
            }
        }
    }
    
    @Override
    public void error( Tunnel tunnel, String error ) {
        
        if ( tunnel != null && tunnel.getSignature().equals( this.tunnel.getSignature() ) ) {
            tunnelStopped( tunnel.getSignature() );
        }
    }
    
    
    @Override
    public void mouseEntered( MouseEvent e ) {
        
        setBackground( colorBackground );
        labelTunnel.setBackground( colorBackground );
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        
        setBackground( null );
        labelTunnel.setBackground( null );
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        
        if ( ! this.connected || anImxProcessing.isVisible() ) {
            return;
        }
        
        labelIcon.setVisible( false );
        anImxProcessing.setVisible( true );
        
        if ( isActive ) {
            
            this.stoppedManually = true;
            logger.info( "Requesting to stop tunnel " + tunnel.getSignature() );
            TunnelClient.sendStopTunnel( tunnel );
        } else {
            
            this.activating = true;
            logger.info( "Requesting to start tunnel " + tunnel.getSignature() );
            TunnelClient.sendStartTunnel( this, tunnel );
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
    
    @Override
    public void hostReachable( Tunnel tunnel ) {
        
        if ( this.tunnel.getAutostart() && ! this.stoppedManually && ! this.isActive && ! this.activating && tunnel.getSignature().equals( this.tunnel.getSignature() ) ) {
            
            logger.info( "Host " + tunnel.getHostname() + " is now reachable, autostarting tunnel" );
            mouseClicked( null );
        }
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

















