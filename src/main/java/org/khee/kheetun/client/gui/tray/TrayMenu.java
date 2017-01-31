package org.khee.kheetun.client.gui.tray;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
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
import org.khee.kheetun.client.gui.KhmenuItem;
import org.khee.kheetun.client.gui.Kholor;
import org.khee.kheetun.client.gui.TextStyle;

public class TrayMenu extends JWindow implements MouseListener, ConfigManagerListener, TunnelClientListener {
    
    public static final long serialVersionUID = 42;
    
    private KhmenuItem      labelKheetun;
    private KhmenuItem      labelConnected;
    private KhmenuItem      itemExit;
    private KhmenuItem      itemStopAll;
    private KhmenuItem      itemAutostartAll;
    private KhmenuItem      itemConfiguration;
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

        labelKheetun    = new KhmenuItem( Imx.KHEETUN.size( 0.6f ) );
        labelKheetun.setStatus( Kheetun.VERSION, Color.GRAY );
        
        labelConnected  = new KhmenuItem( null, "Daemon:" );
        labelConnected.setStatus( "disconnected", Color.RED );
        
        itemExit = new KhmenuItem( Imx.EXIT, "Exit" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                System.exit( 0 );
            }
        };
        
        itemConfiguration = new KhmenuItem( Imx.CONFIGURATION, "Configuration" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                TrayManager.toggleDialog();
            }
        };

        itemStopAll = new KhmenuItem( Imx.STOP, "Stop all" ) {
        
            @Override
            public void leftClick(MouseEvent e) {

                TunnelClient.sendStopAll();
            }
        };
        
        itemAutostartAll = new KhmenuItem( Imx.START, "Autostart all" ) {
            
            @Override
            public void leftClick(MouseEvent e) {
                
                TunnelClient.sendAutoAll();
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
            labelConnected.setError( null );
        } else {
            
            labelConnected.setStatus( "disconnected", Color.RED );
            labelConnected.setError( connectionString + ": " + error);
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
        
//        if ( panelMain.isShowing() ) {
//            Rectangle rect = new Rectangle( panelMain.getLocationOnScreen() );
//            rect.setSize( panelMain.getSize() );
//            
//            if ( ! rect.contains( e.getLocationOnScreen() ) ) {
//                setVisible( false );
//            }
//        }
    }
}

@SuppressWarnings("serial")
class KTProfilesPanel extends JPanel implements ContainerListener {
    
    private JPanel                  panelLoading;
    private JPanel                  panelProfiles;
    private KhmenuItem              noProfiles;
    private KhmenuItem              itemSort;
    private HashMap<Profile,JPanel> profilePanels   = new HashMap<Profile,JPanel>();
    private ArrayList<Profile>      profiles        = new ArrayList<Profile>();
    
    public KTProfilesPanel() {
        
        this.setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
        
        this.panelLoading = new JPanel();
        this.panelLoading.setLayout( new GridBagLayout() );
        this.panelLoading.setAlignmentX( Component.LEFT_ALIGNMENT );
        
        AnImx loading = new AnImx( "loading.png", 50, 125, 50 );
        
        this.panelLoading.add( loading );
        this.panelLoading.setVisible( false );
        
        this.noProfiles = new KhmenuItem( Imx.NONE, "No Profiles" );
        this.noProfiles.setTextStyle( TextStyle.NO_PROFILES );
        this.noProfiles.setActive( false );
        
        this.itemSort    = new KhmenuItem( Imx.PROFILE_SORT, "Sort Profiles" ) {
            
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
        
        this.profiles = profiles;
        
        panelLoading.setVisible( true );
        panelProfiles.setVisible( false );       
        
        ((JWindow)this.getTopLevelAncestor()).pack();
        
        for ( Profile profile : profilePanels.keySet() ) {
            
            for ( Component c : profilePanels.get( profile ).getComponents() ) {
                
                if ( c instanceof KhmenuItem ) {
                    
                    ((KhmenuItem)c).setError( null );
                    
                    if ( c instanceof TunnelMenuItem ) {
                        
                        TunnelClient.removeTunnelClientListener( (TunnelMenuItem)c );
                    }
                }
            }
        }
        
        profilePanels.clear();
        
        for ( Profile profile : profiles ) {
            
            JPanel profilePanel = new JPanel();
            profilePanel.setLayout( new BoxLayout( profilePanel, BoxLayout.PAGE_AXIS ) );
            
            KhmenuItem itemProfile = new KhmenuItem( Imx.PROFILE, profile.getName() );

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
    
    @Override
    public void componentAdded(ContainerEvent e) {
    }
    
    @Override
    public void componentRemoved(ContainerEvent e) {
        
        if ( e.getChild() instanceof KhmenuItem ) {
            
            KhmenuItem item = (KhmenuItem)e.getChild();
            item.setError( null );
            System.out.println( "Message reset for KhmenuItem" );
            
            if ( e.getChild() instanceof TunnelMenuItem ) {
                
                TunnelMenuItem tunnelItem = (TunnelMenuItem)e.getChild();
                TunnelClient.removeTunnelClientListener( tunnelItem );
                System.out.println( "TunnelCLient listener removed for tunnel item" );
            }
        }
    }
}


@SuppressWarnings("serial")
class KTSeperator extends JComponent {
    
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


@SuppressWarnings("serial")
class TunnelMenuItem extends KhmenuItem implements TunnelClientListener {
    
    protected Tunnel    tunnel;
    
    public TunnelMenuItem( Tunnel tunnel ) {
        
        this.add( Box.createHorizontalStrut( 28 ) );
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
            this.setError( null );
            this.setStatus( "unknown", Color.LIGHT_GRAY );
            this.setActive( false );
            this.setInfo( null );
        }
    }
    
    @Override
    public void TunnelClientTunnelStatus(Tunnel tunnel) {
        
        if ( tunnel.equals( this.tunnel ) ) {
            
            if ( tunnel.getError() != null ) {
                
                this.setError( tunnel.getError() + ", failures: " + tunnel.getFailures() );
            } else { 
                this.setError( null );
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
                    this.setStatus( "Ping fail: " + tunnel.getPingFailures() + "/" + tunnel.getMaxPingFailures(), Color.RED );
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
                    iconLeft.setIcon( Imx.AUTO.color( Kholor.READ ) );
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





















