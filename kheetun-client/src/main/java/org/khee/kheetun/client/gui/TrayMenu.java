package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.TunnelClient;
import org.khee.kheetun.client.TunnelClientListener;
import org.khee.kheetun.client.kheetun;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;

public class TrayMenu extends JWindow implements MouseListener, ConfigFrameListener, TunnelClientListener {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    public static final long serialVersionUID = 42;
    
    private JLabel                      labelKheetun;
    private JMenuItem                   itemExit;
    private JMenuItem                   itemConfig;
    private JMenuItem                   itemQuery;
    private JMenuItem                   itemStopAll;
    private ConfigFrame                 configFrame;
    private HashMap<String, JMenuItem>  itemsBySignature    = new HashMap<String, JMenuItem>();
    private HashMap<JMenuItem, Tunnel>  tunnelByItem        = new HashMap<JMenuItem, Tunnel>();
    private HashSet<String>             activeTunnels       = new HashSet<String>();
    private JPopupMenuEx                menu;
    private JPanel                      panel;
    private boolean                     connected           = false;
    private TrayMenu                    tray                = this;
    private TrayIcon                    icon;
    
    public TrayMenu( TrayIcon tray, ConfigFrame frame ) {
        
        setName( "kheetun" );
        setIconImage( Imx.CONFIG.getImage() );
        
        panel = new JPanel();
        
        icon = tray;
        
        menu = new JPopupMenuEx();
        menu.setVisible( true );
        menu.setEnabled( true );
        menu.setBorder( new EmptyBorder( new Insets( 8, 8, 8, 8 ) ) );

        panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
        
        panel.setBorder( new LineBorder( Color.GRAY ) );
        panel.add( menu );
        
        this.getContentPane().setLayout( new BoxLayout( this.getContentPane(), BoxLayout.PAGE_AXIS ) );
        this.getContentPane().add( panel );

        this.configFrame = frame;
        
        labelKheetun = new JLabel( "kheetun v" + kheetun.VERSION, Imx.KHEETUN, JLabel.LEADING );
        labelKheetun.setBorder( new EmptyBorder( new Insets( 4, 0, 8, 0 ) ) );
        labelKheetun.setFont( new Font( labelKheetun.getFont().getName(), Font.BOLD, labelKheetun.getFont().getSize() + 1 ) );
        
        itemExit = new JMenuItem( "Exit", Imx.EXIT );
        itemExit.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                logger.info( "Exiting" );
                TunnelClient.disconnect();
                System.exit( 0 );
            }
        });
        itemExit.addMouseListener( this );
        
        itemConfig = new JMenuItem( "Configure", Imx.CONFIG );
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
        itemStopAll.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                for( Tunnel tunnel : tunnelByItem.values() ) {
                    
                    TunnelClient.sendStopTunnel( tunnel );
                }
            }
        });
        itemStopAll.addMouseListener( this );
        
        

        itemQuery = new JMenuItem( "Requery", Imx.FORWARDS );
        itemQuery.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                TunnelClient.sendQueryTunnels();
            }
        });
        itemQuery.addMouseListener( this );

        setAlwaysOnTop( true );
        setType( Type.POPUP );
        
        buildMenu( null );
        
        tray.addMouseListener( this );
        
        this.addMouseListener( this );
        panel.addMouseListener( this );
        menu.addMouseListener( this );
            
        setFocusableWindowState( true );
        setFocusable( true );
        
        TunnelClient.addClientListener( this );
        
        configFrame.addConfigChangedListener( this );
    }
    
    public void buildMenu( Config config ) {
        
        menu.removeAll();
        
        itemsBySignature.clear();
        tunnelByItem.clear();

        menu.add( labelKheetun );
        menu.addSeparator();
        menu.add( itemConfig );
        menu.addSeparator();
        menu.add( itemStopAll );
        menu.add( itemQuery );
        
        if ( config != null ) {
            for ( Profile profile : config.getProfiles() ) {
                
                menu.addSeparator();
                menu.add( Box.createVerticalStrut( 4 ) );
                JLabel labelProfile = new JLabel( profile.getName(), Imx.PROFILE, JLabel.LEADING );
                labelProfile.setFont( new Font( labelProfile.getFont().getName(), Font.BOLD, labelProfile.getFont().getSize() ) );
                labelProfile.addMouseListener( this );
                menu.add( labelProfile );
                
                for ( Tunnel tunnel : profile.getTunnels() ) {
                    
                    menu.add( Box.createVerticalStrut( 2 ) );
                    
                    JMenuItem itemTunnel = new JMenuItem( tunnel.getAlias(), Imx.INACTIVE );
                    itemTunnel.setBorder( new EmptyBorder( new Insets( 0, 16, 0, 0 ) ) );
                    itemTunnel.setEnabled( connected );
                    itemTunnel.addMouseListener( this );
                    itemTunnel.addActionListener( new ActionListener() {
                     
                        
                        public void actionPerformed(ActionEvent e) {

                            JMenuItem sender = (JMenuItem)e.getSource();
                            if ( tunnelByItem.containsKey( sender ) ) {
                                
                                if ( sender.getIcon().equals( Imx.INACTIVE ) )  {
                                    
                                    logger.info( "Requesting to start tunnel " + tunnelByItem.get( sender ).getSignature() );
                                    TunnelClient.sendStartTunnel( tray, tunnelByItem.get( sender ) );
                                } else {

                                    logger.info( "Requesting to stop tunnel " + tunnelByItem.get( sender ).getSignature() );
                                    TunnelClient.sendStopTunnel( tunnelByItem.get( sender ) );
                                }
                            }
                            
                            
                        }
                    });
                    
                    menu.add( itemTunnel );
                    
                    itemsBySignature.put( tunnel.getSignature(), itemTunnel );
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
    
    public void error(String error) {
    }

    public void connected() {
        
        for ( JMenuItem item : itemsBySignature.values() ) {
            item.setEnabled( true );
        }
        
        TunnelClient.sendQueryTunnels();
        connected = true;
    }

    public void disconnected() {

        for ( JMenuItem item : itemsBySignature.values() ) {
            item.setEnabled( false );
        }
        
        connected = false;
    }

    public void tunnelStarted(String signature) {
        
        if ( itemsBySignature.containsKey( signature ) ) {
            
            activeTunnels.add( signature );
            
            JMenuItem item = itemsBySignature.get( signature );
            item.setIcon( Imx.ACTIVE );

            if ( icon.getImage() != Imx.KHEETUN_ON.s24.getImage() ) {
                icon.setImage( Imx.KHEETUN_ON.s24.getImage() );
                icon.getImage().getGraphics();
            }
        }
    }

    public void tunnelStopped(String signature) {

        if ( itemsBySignature.containsKey( signature ) ) {
            
            activeTunnels.remove( signature );
            
            JMenuItem item = itemsBySignature.get( signature );
            item.setIcon( Imx.INACTIVE );
            
            if ( activeTunnels.size() == 0 && icon.getImage() == Imx.KHEETUN_ON.s24.getImage() ) {
                icon.setImage( Imx.KHEETUN_OFF.s24.getImage() );
            }
        }
    }

    public void activeTunnels(ArrayList<String> signatures) {
        
        int countActive = 0;
        
        for ( String signature : itemsBySignature.keySet() ) {
            
            if ( signatures.contains( signature ) ) {
                tunnelStarted( signature );
                countActive++;
            } else {
                tunnelStopped( signature );
            }
        }
        
        if ( countActive > 0 ) {
            if ( icon.getImage() != Imx.KHEETUN_ON.s24.getImage() ) {
                icon.setImage( Imx.KHEETUN_ON.s24.getImage() );
            }
        } else {
            if ( icon.getImage() != Imx.KHEETUN_OFF.s24.getImage() ) {
                icon.setImage( Imx.KHEETUN_OFF.s24.getImage() );
            }
        }
    }

    public void configChanged(Config config) {
        
        buildMenu( config );
    }
    
    public void mouseClicked(MouseEvent e) {
        
        if ( ! ( e.getSource() instanceof TrayIcon ) ) {
            return;
        }

        if ( isVisible() ) {
            setVisible( false );
        
        } else {

            /*
             * taskbar detection is not really working (here: ubuntu mint 17 / cinnamon),
             * also popup window is acting strange and would be placed below taskbar,
             * so a really fuzzy detection method for placing our popup is used. Sorry!
             */
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

            Rectangle safeBounds = new Rectangle(bounds);
            safeBounds.x += insets.left;
            safeBounds.y += insets.top;
            safeBounds.width -= (insets.left + insets.right);
            safeBounds.height -= (insets.top + insets.bottom);
            
            Point position = new Point();
                
            // right
            if ( e.getLocationOnScreen().x > safeBounds.x + safeBounds.width - icon.getSize().width  ) {
                position.setLocation( safeBounds.x + safeBounds.width - panel.getWidth() - icon.getSize().width - 4, e.getLocationOnScreen().y - panel.getHeight() / 3 );
            }
            // left
            else if ( e.getLocationOnScreen().x < safeBounds.x + icon.getSize().width ) { 
                position.setLocation( safeBounds.x + icon.getSize().width + 4, e.getLocationOnScreen().y - panel.getHeight() / 3 );
            }
            // top
            else if ( e.getLocationOnScreen().y < safeBounds.y + icon.getSize().height ) { 
                position.setLocation( e.getLocationOnScreen().x - panel.getWidth() / 3, safeBounds.y + icon.getSize().height + 4 );
            }
            // bottom
            else if ( e.getLocationOnScreen().y > safeBounds.y + safeBounds.height - icon.getSize().height ) { 
                position.setLocation( e.getLocationOnScreen().x - panel.getWidth() / 3, safeBounds.y + safeBounds.height - panel.getHeight() - icon.getSize().height - 4 );
            }
            
            // finally adjust potential clipping
            int diffX = ( position.x + panel.getWidth() )  - ( safeBounds.x + safeBounds.width );
            int diffY = ( position.y + panel.getHeight() ) - ( safeBounds.y + safeBounds.height );
            
            position.x -= diffX > 0 ? diffX + 4 : 0;
            position.y -= diffY > 0 ? diffY + 4 : 0;
            
            setLocation( position );
            setVisible( true );
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {

        try {
        
            Rectangle rect = new Rectangle( panel.getLocationOnScreen() );
            rect.setSize( panel.getSize() );
            
            if ( ! rect.contains( e.getLocationOnScreen() ) ) {
                setVisible( false );
            }
        } catch ( Exception ex ) {
            
        }
    }
}


class ForwardLabel extends JPanel {
    
    public static final long serialVersionUID = 42;
    
    private JLabel labelComment;
    private JLabel labelType;
    private JLabel labelBind;
    private JLabel labelForward;
    
    public ForwardLabel( Forward forward ) {
        
        this.setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
        this.setAlignmentX( 0.0f );
        this.setOpaque( false );
        
        labelComment = new JLabel( forward.getComment() );
        labelType    = new JLabel( forward.getType().substring( 0, 1 ).toUpperCase() );
        labelBind    = new JLabel( forward.getBindIp() + ":" + forward.getBindPort() );
        labelForward = new JLabel( forward.getForwardedHost() + ":" + forward.getForwardedPort() );
        
        labelComment.setHorizontalAlignment( JLabel.LEFT );
        labelType.setHorizontalAlignment( JLabel.LEFT );
        labelBind.setHorizontalAlignment( JLabel.LEFT );
        labelForward.setHorizontalAlignment( JLabel.LEFT );
        
        Font font = new Font( labelComment.getFont().getName(), labelComment.getFont().getStyle(), 10 );
        labelComment.setFont( font );
        labelType.setFont( font );
        labelBind.setFont( font );
        labelForward.setFont( font );
        
        labelComment.setForeground( Color.DARK_GRAY );
        labelType.setForeground( forward.getType().equals( Forward.LOCAL ) ? Color.GREEN : Color.RED );
        labelBind.setForeground( Color.GRAY );
        labelForward.setForeground( Color.GRAY );
        
        Dimension dimType    = new Dimension( 16, labelType.getPreferredSize().height );
        Dimension dimBind    = new Dimension( 90, labelBind.getPreferredSize().height );
        Dimension dimComment = new Dimension( 130, labelComment.getPreferredSize().height );
        Dimension dimForward = new Dimension( 220, labelForward.getPreferredSize().height );
        labelType.setPreferredSize( dimType );
        labelBind.setPreferredSize( dimBind );
        labelComment.setPreferredSize( dimComment );
        labelForward.setPreferredSize( dimForward );
        
        this.add( Box.createHorizontalStrut( 40 ) );
        this.add( labelType );
        this.add( Box.createHorizontalStrut( 4 ) );
        this.add( labelBind );
        this.add( Box.createHorizontalStrut( 4 ) );
        this.add( labelForward );
        this.add( Box.createHorizontalStrut( 4 ) );
        this.add( labelComment );
        this.add( Box.createHorizontalGlue() );
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

















