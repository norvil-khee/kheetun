package org.khee.kheetun.client.gui.tray;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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

import org.khee.kheetun.Kheetun;
import org.khee.kheetun.client.TunnelClient;
import org.khee.kheetun.client.TunnelClientListener;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.GlobalConfig;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.client.gui.AnImx;
import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.Kholor;
import org.khee.kheetun.client.gui.TextStyle;

public class TrayMenu extends JWindow implements MouseListener, ConfigManagerListener, TunnelClientListener {
    
    public static final long serialVersionUID = 42;
    
    private KTMenuItem      labelKheetun;
    private KTMenuItem      labelConnected;
    private KTMenuItem      itemExit;
    private KTMenuItem      itemStopAll;
    private KTMenuItem      itemAutostartAll;
    private KTMenuItem      itemConfiguration;
    private KTProfilesPanel panelProfiles       = new KTProfilesPanel();
    private JPanel          panelMain;
    
    @SuppressWarnings("serial")
    public TrayMenu() {
        
        this.setName( "kheetun" );
        this.setIconImage( Imx.KHEETUN.getImage() );
        
        panelMain = new JPanel();
        panelMain.setDoubleBuffered( true );
        panelMain.setLayout( new BoxLayout( panelMain, BoxLayout.PAGE_AXIS ) );
        panelMain.setBorder( new LineBorder( Color.GRAY ) );
        
        this.getContentPane().setLayout( new BoxLayout( this.getContentPane(), BoxLayout.PAGE_AXIS ) );
        this.getContentPane().add( panelMain );

        labelKheetun    = new KTMenuItem( Imx.KHEETUN, "" );
        labelKheetun.setStatus( Kheetun.VERSION, Color.GRAY );
        
        labelConnected  = new KTMenuItem( Imx.NONE, "Daemon:" );
        labelConnected.setStatus( "disconnected", Color.RED );
        
        itemExit = new KTMenuItem( Imx.EXIT, "Exit" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                System.exit( 0 );
            }
        };
        
        itemStopAll = new KTMenuItem( Imx.STOP, "Stop all" ) {
        
            @Override
            public void leftClick(MouseEvent e) {

                TunnelClient.sendStopAll();
            }
        };
        
        itemAutostartAll = new KTMenuItem( Imx.START, "Autostart all" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                TunnelClient.sendAutoAll();
            }
        };
        
        itemConfiguration = new KTMenuItem( Imx.CONFIGURATION, "Configuration" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                TrayManager.toggleDialog();
            }
        };
        
        
        this.setAlwaysOnTop( true );
        this.setType( Type.POPUP );
        
        this.addMouseListener( this );
        panelMain.addMouseListener( this );
            
        this.setFocusableWindowState( true );
        this.setFocusable( true );
        
        TunnelClient.addTunnelClientListener( this );
        ConfigManager.addConfigManagerListener( this );
        
        panelMain.add( labelKheetun );
        panelMain.add( new KTSeperator() );
        panelMain.add( labelConnected );
        panelMain.add( new KTSeperator() );
        panelMain.add( itemConfiguration );
        panelMain.add( itemStopAll );
        panelMain.add( itemAutostartAll );
        panelMain.add( panelProfiles );
        panelMain.add( itemExit );
        
        this.pack();
    }
    
    @Override
    public void configManagerGlobalConfigChanged( GlobalConfig oldConfig, GlobalConfig newConfig, boolean valid ) {
        
        if ( valid && ! oldConfig.getSortOrder().equals( newConfig.getSortOrder() ) ) {
            
            panelProfiles.sort();
        }
    }
    
    @Override
    public void configManagerConfigChanged( Config oldConfig, Config newConfig, boolean valid ) {
        
        panelProfiles.setProfiles( newConfig.getProfiles() );
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
    public void TunnelClientConnection( boolean connected, String error ) {
        
        String connectionString = ConfigManager.getGlobalConfig().getHost() + ":" + ConfigManager.getGlobalConfig().getPort();
        
        if ( connected ) {
            
            labelConnected.setStatus( connectionString, new Color( 0, 100, 0 ) );
            labelConnected.setMessage( "connection", null );
        } else {
            
            labelConnected.setStatus( "disconnected", Color.RED );
            labelConnected.setMessage( "connection", connectionString + ": " + error);
        }
    }
    
    @Override
    public void TunnelClientTunnelStatus( Tunnel tunnel ) {
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
        
        if ( panelMain.isShowing() ) {
            Rectangle rect = new Rectangle( panelMain.getLocationOnScreen() );
            rect.setSize( panelMain.getSize() );
            
            if ( ! rect.contains( e.getLocationOnScreen() ) ) {
                setVisible( false );
            }
        }
    }
}

@SuppressWarnings("serial")
class KTProfilesPanel extends JPanel {
    
    private static final long serialVersionUID = 2L;
    
    private JPanel                  panelLoading;
    private JPanel                  panelProfiles;
    private KTMenuItem              noProfiles;
    private KTMenuItem              itemSort;
    private HashMap<Profile,JPanel> profilePanels   = new HashMap<Profile,JPanel>();
    private ArrayList<Profile>      profiles        = new ArrayList<Profile>();
    
    public KTProfilesPanel() {
        
        this.setDoubleBuffered( true );
        
        this.setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
        
        this.panelLoading = new JPanel();
        this.panelLoading.setLayout( new GridBagLayout() );
        this.panelLoading.setAlignmentX( Component.LEFT_ALIGNMENT );
        
        AnImx loading = new AnImx( "loading.png", 50, 125, 50 );
        
        this.panelLoading.add( loading );
        this.panelLoading.setVisible( false );
        
        this.noProfiles = new KTMenuItem( Imx.NONE, "No Profiles" );
        this.noProfiles.setTextStyle( TextStyle.NO_PROFILES );
        this.noProfiles.setActive( false );
        
        this.itemSort    = new KTMenuItem( Imx.PROFILE_SORT, "Sort Profiles" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                if ( ConfigManager.getGlobalConfig() != null ) {
                    
                    switch( ConfigManager.getGlobalConfig().getSortOrder() ) {
                        
                    case GlobalConfig.SORT_ALPHABETICAL_DESC:
                        ConfigManager.getGlobalConfig().setSortOrder( GlobalConfig.SORT_MODIFIED_ASC );
                        break;

                    case GlobalConfig.SORT_MODIFIED_ASC:
                        ConfigManager.getGlobalConfig().setSortOrder( GlobalConfig.SORT_MODIFIED_DESC );
                        break;

                    case GlobalConfig.SORT_MODIFIED_DESC:
                        ConfigManager.getGlobalConfig().setSortOrder( GlobalConfig.SORT_ALPHABETICAL_ASC );
                        break;

                    default:
                        ConfigManager.getGlobalConfig().setSortOrder( GlobalConfig.SORT_ALPHABETICAL_DESC );
                        break;
                    }
                    
                    KTProfilesPanel.this.sort();
                    ConfigManager.getGlobalConfig().save();
                }
            };
        };
        
        this.itemSort.setStatus( "A...Z", Color.DARK_GRAY );
        
        this.panelProfiles = new JPanel();
        this.panelProfiles.setLayout( new BoxLayout( this.panelProfiles, BoxLayout.PAGE_AXIS ) );
        
        this.add( itemSort );
        this.add( new KTSeperator() );
        this.add( this.panelLoading );
        this.add( this.panelProfiles );
    }
    
    public void setProfiles( ArrayList<Profile> profiles ) {
        
        TrayManager.clearErrors( "profile" );
        
        this.profiles = profiles;
        
        panelLoading.setVisible( true );
        panelProfiles.setVisible( false );       
        
        ((JWindow)this.getTopLevelAncestor()).pack();
        
        profilePanels.clear();
        
        for ( Profile profile : profiles ) {
            
            JPanel profilePanel = new JPanel();
            profilePanel.setLayout( new BoxLayout( profilePanel, BoxLayout.PAGE_AXIS ) );
            
            KTMenuItem itemProfile = new KTMenuItem( Imx.PROFILE, profile.getName() );

            itemProfile.setTextStyle( TextStyle.PROFILE );
            
            itemProfile.setStatus( "[" + profile.getConfigFile().getName() + "]", new Color( 0, 100, 0 ) );
            
            if ( ! profile.isActive() ) {
                itemProfile.setActive( false );
                itemProfile.setInfo( "This profile is deactivated." );
            }
            
//            if ( ! profile.getErrors().isEmpty() ) {
//                
//                String message = "<html><body>Configuration errors:";
//                
//                for ( String error : profile.getErrors() ) {
//                    
//                    message += "<br>    * " + error;
//                }
//                
//                message += "</body></html>";
//                
//                itemProfile.setMessage( "profile", message );
//                itemProfile.setStatus( "[" + profile.getConfigFile().getName() + "]", Color.RED );
//            }
            
            profilePanel.add( itemProfile );
            
            if ( profile.isActive() ) {
            
                for ( Tunnel tunnel : profile.getTunnels() ) {
                    
                    TunnelMenuItem itemTunnel = new TunnelMenuItem( tunnel );
                    itemTunnel.setTextStyle( TextStyle.TUNNEL );
                    
                    if ( ! profile.getErrors().isEmpty() ) {
                        
                        itemTunnel.setActive( false );
                    } 
                    
                    profilePanel.add( itemTunnel );
                }
            }
            
            profilePanel.add( new KTSeperator() );
            
            this.profilePanels.put( profile, profilePanel );
        }
        
        panelLoading.setVisible( false );
        panelProfiles.setVisible( true );
        
        this.sort();
    }
    
    public void sort() {
        
        this.panelProfiles.removeAll();
        
        if ( this.profiles.isEmpty() ) {
            
            this.panelProfiles.add( this.noProfiles );
            
        } else {
        
            switch( ConfigManager.getGlobalConfig().getSortOrder() ) {
              
            case GlobalConfig.SORT_ALPHABETICAL_DESC:
                itemSort.setStatus( "Z...A", Color.DARK_GRAY );
                Collections.sort( profiles, new Comparator<Profile>() {
                    
                  public int compare( Profile p1, Profile p2 ) {
                      return p2.getName().compareTo( p1.getName() );
                  };
                } );
                break;
                  
            case GlobalConfig.SORT_MODIFIED_ASC:
                itemSort.setStatus( "DATE ASC", Color.DARK_GRAY );
                Collections.sort( profiles, new Comparator<Profile>() {
                    
                    public int compare( Profile p1, Profile p2 ) {
                        return p1.getConfigFile().lastModified() > p2.getConfigFile().lastModified() ? 1 : -1;
                    };
                } );
                break;
                
            case GlobalConfig.SORT_MODIFIED_DESC:
                itemSort.setStatus( "DATE DESC", Color.DARK_GRAY );
                Collections.sort( profiles, new Comparator<Profile>() {
                    
                    public int compare( Profile p1, Profile p2 ) {
                        return p1.getConfigFile().lastModified() < p2.getConfigFile().lastModified() ? 1 : -1;
                    };
                } );
                break;
                  
            default:
                itemSort.setStatus( "A...Z", Color.DARK_GRAY );
                Collections.sort( profiles, new Comparator<Profile>() {
                    
                    public int compare( Profile p1, Profile p2 ) {
                        return p1.getName().compareTo( p2.getName() );
                    };
                } );
                break;
            };
            
            for ( Profile profile : this.profiles ) {
                this.panelProfiles.add( profilePanels.get( profile ) );
            }
        }

        ((JWindow)this.getTopLevelAncestor()).pack();
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
    private static int      count              = 0;

    protected JLabel        iconLeft            = new JLabel( Imx.NONE );
    protected JLabel        iconCenter          = new JLabel( Imx.NONE );
    protected JLabel        iconRight           = new JLabel( Imx.NONE );
    protected JLabel        iconInfo            = new JLabel( Imx.NONE );
    protected JLabel        text                = new JLabel( "" );
    protected JLabel        status              = new JLabel( "", JLabel.RIGHT );
    
    private TextStyle       textStyle           = TextStyle.DEFAULT;
    
    protected AnImx         processing          = new AnImx( "loading.png", 50, 125, 16 );
    
    protected Color         colorBackground;

    protected JLabel        message             = new JLabel( Imx.WARNING );
    protected JLabel        info                = new JLabel( Imx.INFO );
    protected JLabel        clipboardHint       = new JLabel( "[Click middle mouse button to copy this message]", Imx.NONE, JLabel.LEFT );
    protected JWindow       windowMessage       = new JWindow();
    protected JWindow       windowInfo          = new JWindow();
    
    protected boolean       active              = true;
    
    private int id                              = KTMenuItem.count++;
    
    public KTMenuItem() {
    }
    
    public KTMenuItem( Icon iconLeft, Icon iconCenter, Icon iconRight, String text, String status ) {
        
        this.initComponents();
        
        this.iconLeft.setIcon( iconLeft );
        this.iconCenter.setIcon( iconCenter );
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
    
    public void setMessage( String scope, String message ) {
        
        if ( message == null ) {
            this.message.setText( null );
            this.iconCenter.setIcon( Imx.NONE );
            TrayManager.clearError( scope, id );
            this.windowInfo.setVisible( false );
        } else {
            
            if ( ! message.equals( this.message.getText() ) ) {
                this.message.setText( message );
                this.iconCenter.setIcon( Imx.WARNING );
                TrayManager.setError( scope, id, message );
            }
        }
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
        
        if ( iconCenter.getIcon() == Imx.WARNING || iconCenter.getIcon() == Imx.WARNING_DISABLED ) {
            
            iconCenter.setIcon( Imx.WARNING_DISABLED );
            
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
        
        if ( this.iconInfo.getIcon() == Imx.INFO ) {
            
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
            
            if ( iconCenter.isVisible() && ( iconCenter.getIcon() == Imx.WARNING || iconCenter.getIcon() == Imx.WARNING_DISABLED ) ) {
                
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
            
            if ( iconCenter.isVisible() && ( iconCenter.getIcon() == Imx.WARNING || iconCenter.getIcon() == Imx.WARNING_DISABLED ) ) {
                
                this.clipboardHint.setForeground( Color.BLACK );
                return;
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {

        if ( e.getButton() == MouseEvent.BUTTON2 ) {
            
            if ( iconCenter.isVisible() && ( iconCenter.getIcon() == Imx.WARNING || iconCenter.getIcon() == Imx.WARNING_DISABLED ) ) {
                
                this.clipboardHint.setForeground( Color.GRAY );
                return;
            }
        }
    }
}


class TunnelMenuItem extends KTMenuItem implements TunnelClientListener {
    
    private static final long serialVersionUID = 1L;
    
    protected Tunnel    tunnel;
    
    public TunnelMenuItem( Tunnel tunnel ) {
        
        this.add( Box.createHorizontalStrut( 20 ) );
        this.initComponents();
        
        this.tunnel = tunnel;
        
        this.iconLeft.setIcon( Imx.NONE );
        this.iconRight.setIcon( Imx.NONE );
        this.text.setText( tunnel.getAlias() );
        
        this.setStatus( "unknown", Color.LIGHT_GRAY );
        
        TunnelClient.addTunnelClientListener( this );
        TunnelClient.sendQueryTunnel( this.tunnel );
    }
    
    @Override
    public void leftClick( MouseEvent e ) {
        
        if ( ! this.isProcessing() ) {
            this.setProcessing( true );
            TunnelClient.sendToggle( this.tunnel );
        }
    }
    
    @Override
    public void TunnelClientConnection( boolean connected, String error ) {
        
        if ( connected ) {

            text.setForeground( Color.BLACK );
            this.setActive( true );
            TunnelClient.sendQueryTunnel( this.tunnel );
            
        } else {
            
            text.setForeground( Color.LIGHT_GRAY );
            this.iconRight.setIcon( Imx.NONE );
            this.setProcessing( false );
            this.setMessage( "profile", null );
            this.setStatus( "unknown", Color.LIGHT_GRAY );
            this.setActive( false );
            this.setInfo( null );
        }
    }
    
    @Override
    public void TunnelClientTunnelStatus(Tunnel tunnel) {
        
        if ( tunnel.equals( this.tunnel ) ) {
            
            if ( tunnel.getError() != null ) {
                
                this.setMessage( "profile", tunnel.getError() + ", failures: " + tunnel.getFailures() );
            } else { 
                this.setMessage( "profile", null );
            }
            
            switch ( tunnel.getState() ) {
            
            case Tunnel.STATE_STARTING:

                this.setProcessing( true );
                this.setStatus( "starting[" + ( tunnel.getFailures() + 1 ) + "]", Color.GRAY );
                this.setTextStyle( TextStyle.TUNNEL );
                break;
            
            case Tunnel.STATE_RUNNING:
                
                iconRight.setIcon( Imx.RUNNING );
                this.setProcessing( false );
                this.setTextStyle( TextStyle.TUNNEL_RUNNING );

                if ( tunnel.getPingFailures() > 0 ) {
                    this.setStatus( "failing (" + tunnel.getPingFailures() + "/3)", Color.RED );
                } else {
                    this.setStatus( String.valueOf( tunnel.getPing() ) + " ms", Color.GRAY );
                }
                break;
                
            case Tunnel.STATE_STOPPING:

                this.setProcessing( true );
                this.setStatus( "stopping", Color.GRAY );
                break;
                
            case Tunnel.STATE_STOPPED:
                
                iconRight.setIcon( Imx.STOPPED );
                this.setProcessing( false );
                this.setStatus( "inactive", Color.LIGHT_GRAY );
                this.setTextStyle( TextStyle.TUNNEL );
                break;
            }
            
            switch ( tunnel.getAutoState() ) {
            
            case Tunnel.STATE_AUTO_WAIT:
                
                if ( this.tunnel.getAutostart() ) {
                    this.iconLeft.setIcon( Imx.AUTO );
                }
                
                this.setStatus( "waiting",  Color.GRAY );
                this.setInfo( tunnel.getInfo() );
                break;
                
            case Tunnel.STATE_AUTO_ON:
                
                if ( this.tunnel.getAutostart() ) {
                    iconLeft.setIcon( Imx.AUTO );
                    this.setInfo( null );
                }
                break;

            case Tunnel.STATE_AUTO_OFF:

                if ( this.tunnel.getAutostart() ) {
                    iconLeft.setIcon( Imx.AUTO.color( Kholor.DISABLED ) );
                    this.setInfo( null );
                }
                break;
            
            case Tunnel.STATE_AUTO_AVAIL:

                if ( this.tunnel.getAutostart() ) {
                    iconLeft.setIcon( Imx.AUTO );
                    this.setInfo( null );
                }
                break;
            }
        }
    }
}




















