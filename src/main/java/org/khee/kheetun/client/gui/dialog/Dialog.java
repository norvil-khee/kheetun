package org.khee.kheetun.client.gui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.config.Base;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.ConfigManager;
import org.khee.kheetun.client.config.ConfigManagerListener;
import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.GlobalConfig;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.client.gui.ErrorMatrix;
import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.KhbuttonListener;
import org.khee.kheetun.client.gui.Kholor;
import org.khee.kheetun.client.gui.KhoolButton;
import org.khee.kheetun.client.gui.TextStyle;

@SuppressWarnings("serial")
public class Dialog extends JFrame implements ConfigManagerListener, KhbuttonListener, GUIElementListener, SelectionListener {
    
    protected static Logger logger = LogManager.getLogger( "kheetun" );

    public static Dialog            instance;
    
    private Config                  config;
    private Config                  configOriginal;
    
    private ConfigPanel<Profile>    profilesPanel;
    private ConfigPanel<Tunnel>     tunnelPanel;
    private ConfigPanel<Forward>    forwardsPanel;
    
    private KhoolButton             buttonSave          = new KhoolButton( "SAVE",         Imx.SAVE );
    private KhoolButton             buttonRevert        = new KhoolButton( "REVERT",       Imx.REVERT );
    private KhoolButton             buttonUndo          = new KhoolButton( "UNDO",         Imx.UNDO );
    private KhoolButton             buttonErrors        = new KhoolButton( "NO ERRORS",    Imx.WARNING.color( Kholor.OK ) );
    private KhoolButton             buttonNew           = new KhoolButton( "NEW",          Imx.NEW );

    private GUIElement              editingElement      = null;
    
    private ErrorMatrix             errorMatrix         = new ErrorMatrix();
    private JScrollPane             scrollErrorMatrix;
    private JWindow                 windowTooltip       = new JWindow();
    private JLabel                  labelTooltip        = new JLabel();
    
    public Dialog() {}
    
    private void initComponents() {
        
        this.setTitle( "Kheetun Configuration" );
        this.setIconImage( Imx.TRAY.getImage() );
        
        this.setType( Type.UTILITY );
        
        this.setLayout( new GridBagLayout() );
        
        this.setMinimumSize( new Dimension( 1350, 800 ) );
        this.setMaximumSize( new Dimension( 1350, Integer.MAX_VALUE ) );
        
        this.profilesPanel  = new ConfigPanel<Profile>( this, "PROFILES", Imx.NONE, TextStyle.CONFIG_HEADER );
        this.tunnelPanel    = new ConfigPanel<Tunnel>( this,  "TUNNEL",   Imx.NONE, TextStyle.CONFIG_HEADER );
        this.forwardsPanel  = new ConfigPanel<Forward>( this, "FORWARDS", Imx.NONE, TextStyle.CONFIG_HEADER );
        
        
        this.labelTooltip.setBorder( BorderFactory.createCompoundBorder( new LineBorder(Color.DARK_GRAY ), new EmptyBorder( 4, 4, 4, 4 ) ) );
        this.windowTooltip.setType( Type.POPUP );
        this.windowTooltip.getContentPane().setLayout( new BoxLayout( this.windowTooltip.getContentPane(), BoxLayout.X_AXIS ) );
        this.windowTooltip.add( this.labelTooltip );
        
        this.buttonSave.addButtonListener( this );
        this.buttonRevert.addButtonListener( this );
        this.buttonErrors.addButtonListener( this );
        this.buttonNew.addButtonListener( this );
        
        JPanel panelButtons = new JPanel(){
            
            @Override
            protected void paintComponent(Graphics g) {
                
                Graphics2D g2d = (Graphics2D)g;
                
                g2d.setColor( Color.LIGHT_GRAY );
                g2d.drawLine( 0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2 );
                
                super.paintComponent(g);
            }
        };
        panelButtons.setOpaque( false );
        panelButtons.setLayout( new BoxLayout( panelButtons, BoxLayout.X_AXIS ) );

        JPanel panelLeft = new JPanel(){
            
            @Override
            protected void paintComponent(Graphics g) {
                
                Graphics2D g2d = (Graphics2D)g;
                
                g2d.setColor( Color.LIGHT_GRAY );
                g2d.drawLine( 0, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2 );
                
                super.paintComponent(g);
            }
        };
        panelLeft.setOpaque( false );
        panelLeft.setLayout( new BoxLayout( panelLeft, BoxLayout.X_AXIS ) );
        
        JLabel labelConfiguration = new JLabel( "CONFIGURATION" );
        labelConfiguration.setOpaque( true );
        labelConfiguration.setFont( new Font( Font.DIALOG, Font.BOLD, 12 ) );
        labelConfiguration.setBackground( Kholor.BLUE );
        labelConfiguration.setForeground( Color.WHITE );
        labelConfiguration.setPreferredSize( new Dimension( labelConfiguration.getPreferredSize().width + 16, 24 ) );
        labelConfiguration.setMinimumSize( new Dimension( labelConfiguration.getPreferredSize().width + 16, 24 ) );
        labelConfiguration.setMaximumSize( new Dimension( labelConfiguration.getPreferredSize().width + 16, 24 ) );
        labelConfiguration.setHorizontalAlignment( SwingConstants.CENTER );
        
        KhoolButton easterEggedCakeButton = new KhoolButton( "Hi!", Imx.CONFIGURATION ) {
            
            private String[] cake = {
                "Hi!",
                "Hello!",
                "Good Day!",
                "So polite...",
                "How are you?",
                "Doesn't this button look like a cake?",
                "Surely we know that cakes are lies.",
                "Not all cakes though...",
                "There actually was a cake!",
                "Though it was hidden quite well.",
                "Well, this button is certainly no lie.",
                "Try clicking it!",
                "Oh... you already have?",
                "Sorry.",
                "Quite disappointing, right?",
                "So this cake seems to be a lie after all.",
                "I dont't know what to say :-(",
                "Lets just change the icon.",
                "Perhaps this makes us feel better...",
                "Here we go.",
                "Better?",
                "Aw, come on, its cute!",
                "But it's a heart!",
                "<3!!",
                "<3 <3 <3 <3",
                "Okay, enough.",
                "Too much love will kill us.",
                "Too less, too.",
                "I order the cake back.",
                "Hi, cake.",
                "Ok, it's over.",
                "Stop hovering this button.",
                "I command thee!",
                "Really, nothing new here.",
                "Hover away!",
                "Are you serious?",
                "You seriously believe, someone would put endless mindless quotes into his code just to satisfy your hovering needs?",
                "Okay, i'm gonna bore you away.",
                "Just couting now...",
                "One",
                "Two",
                "Three",
                "Four",
                "Five",
                "Seven",
                "Ha!",
                "Just wanted to see if you are still here.",
                "You are.",
                "Stubborn are we...",
                "Ok, you win.",
                "You hovered me down.",
                "You won the epic battle of hovering.",
                "It's hover now!",
                "Game Hover!",
                "Insert coin to continue.",
                "Game restarts in...",
                "Three",
                "Two",
                "One"
            };
            
            private int piece = 0;
            
            @Override
            public void mouseEntered(MouseEvent e) {
                
                if ( piece >= this.cake.length ) {
                    this.piece = 0;
                }
                
                this.text = cake[ piece ];
                
                if ( this.text.equals( "Here we go." ) ) {
                    
                    this.setEnabled( false );
                    this.setIcon( Imx.AUTO.color( Kholor.ERROR ) );
                }
                
                if ( this.text.equals( "Hi, cake." ) ) {

                    this.setIcon( Imx.CONFIGURATION );
                    this.setEnabled( true );
                }
                
                super.mouseEntered(e);
                
                this.piece++;
            }
        };
        
        panelLeft.add( easterEggedCakeButton );
        panelLeft.add( Box.createHorizontalStrut( 8 ) );
        panelLeft.add( labelConfiguration );
//        panelLeft.add( Box.createHorizontalGlue() );
//        panelLeft.add( new JLabel( Imx.TRAY_NEUTRAL ) );

        int scrollBarSize = ((Integer)UIManager.get("ScrollBar.width")).intValue();
        panelLeft.add( Box.createHorizontalStrut( scrollBarSize ) );
        

        this.buttonErrors.setId( "ERRORS" );
        panelButtons.add( buttonNew );
        panelButtons.add( Box.createHorizontalStrut( 32 ) );
        panelButtons.add( buttonSave );
        panelButtons.add( Box.createHorizontalStrut( 32 ) );
        panelButtons.add( buttonRevert );
//        panelButtons.add( Box.createHorizontalStrut( 32 ) );
//        panelButtons.add( buttonUndo );
        panelButtons.add( Box.createHorizontalGlue() );
        panelButtons.add( buttonErrors );
        panelButtons.add( Box.createHorizontalStrut( scrollBarSize ) );
        
        
        
        profilesPanel.setMinimumSize( new Dimension( 280, 1 ) );
        profilesPanel.setPreferredSize( new Dimension( 280, 1 ) );
        profilesPanel.setMaximumSize( new Dimension( 280, Integer.MAX_VALUE ) );
        
        tunnelPanel.setMinimumSize( new Dimension( 550, 1 ) );
        tunnelPanel.setPreferredSize( new Dimension( 550, 1 ) );
        tunnelPanel.setMaximumSize( new Dimension( 550, Integer.MAX_VALUE ) );

        this.scrollErrorMatrix = new JScrollPane( this.errorMatrix );
        this.scrollErrorMatrix.setMinimumSize( new Dimension( 622, 100 ) );
        this.scrollErrorMatrix.setPreferredSize( new Dimension( 622, 100 ) );
        this.scrollErrorMatrix.setMaximumSize( new Dimension( 622, 100 ) );
        this.scrollErrorMatrix.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        
        this.add( panelLeft,            new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 32, 0 ), 0, 0 ) );
        this.add( panelButtons,         new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 32, 0 ), 0, 0 ) );
        this.add( scrollErrorMatrix,    new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 32, 0 ), 0, 0 ) );
        this.add( this.profilesPanel,   new GridBagConstraints( 0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.VERTICAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        this.add( this.tunnelPanel,     new GridBagConstraints( 1, 2, 1, 1, 0.0, 1.0, GridBagConstraints.PAGE_END, GridBagConstraints.VERTICAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        this.add( this.forwardsPanel,   new GridBagConstraints( 2, 2, 1, 1, 1.0, 1.0, GridBagConstraints.LAST_LINE_END, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        
        ConfigManager.addConfigManagerListener( this );
        Selection.getInstance().addSelectionListener( this );
        
        JComponent glass = new JComponent() {
            
            @Override
            public void paint(Graphics g) {
                Shape upper = new Rectangle2D.Double( 0, 0, 400, 34 );
                Shape lower = new Rectangle2D.Double( 0, 66, 400, 64 );
                
                Area  clip  = new Area();
                clip.add( new Area( upper ) );
                clip.add( new Area( lower ) );
                
                g.setClip( clip );
                
                super.paint( g );
                
                Graphics2D g2d = (Graphics2D)g;
                
                Color background = UIManager.getColor( "Panel.background" );
                
                Color start = new Color( background.getRed(), background.getGreen(), background.getBlue(), 160 );
                Color end = new Color( background.getRed(), background.getGreen(), background.getBlue(), 255 );
                
                g2d.setPaint( new GradientPaint( 0.0f, 0.0f, start, 100.0f, 0.0f, end ) );
                g2d.fillRect( 0, 0, 300, 200 );
            }

        };
        
        glass.setLayout( new GridBagLayout() );
        glass.add( new JLabel( Imx.TRAY_NEUTRAL.size( 100 ) ), new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        
        this.getRootPane().setGlassPane( glass );
        this.getRootPane().getGlassPane().setVisible( true );
    }
    
    public static void init() {
        
        Dialog.instance = new Dialog();
        Dialog.instance.initComponents();
    }
    
    public static Dialog getInstance() {
        
        return Dialog.instance;
    }
    
    public Config getConfig() {
        
        return this.config;
    }
    
    public void showTooltip( Point p, Icon icon, String hint ) {
        
        this.labelTooltip.setIcon( icon );
        this.labelTooltip.setText( hint );
        this.windowTooltip.setLocation( p.x, p.y - this.windowTooltip.getHeight() - 4 );
        this.windowTooltip.pack();
        this.windowTooltip.setVisible( true );
    }
    
    public void hideTooltip() {
        
        this.windowTooltip.setVisible( false );
    }
    
    
    public void toggle() {
        
        this.setVisible( ! this.isVisible() );
    }
    
    private void validateConfig() {

        ArrayList<String> errors = this.config.getAllErrors();
        
        if ( errors.size() == 0 ) {

            this.buttonErrors.setText( "NO ERRORS" );
            this.buttonErrors.setIcon( Imx.WARNING.color( Kholor.OK ) );
        
        } else {
            
            this.buttonErrors.setText( errors.size() + " ERRORS" );
            this.buttonErrors.setIcon( Imx.WARNING.color( Kholor.ERROR ) );
        }
        
        this.errorMatrix.showErrors( this.config );        
        
        if ( this.config.equals( this.configOriginal ) && this.config.equalsMeta( this.configOriginal ) ) {
            
            this.buttonRevert.setEnabled( false );
            this.buttonSave.setEnabled( false );
            
            return;
        }

        if ( ! this.config.equals( this.configOriginal ) ) {

            this.buttonRevert.setEnabled( true );
            this.buttonSave.setEnabled( errors.size() == 0 );
        }

        if ( ! this.config.equalsMeta( this.configOriginal ) ) {

            this.buttonRevert.setEnabled( true );
            this.buttonSave.setEnabled( errors.size() == 0 );
        }
    }
    
    public void endEdit() {
        
        if ( this.editingElement != null ) {
            this.editingElement.endEdit( true );
        }
    }
    
    @Override
    public void selectionChanged( Class<? extends Base> c, Base object ) {
        
        if ( c == Profile.class ) {
            
            if ( object != null ) {

                this.tunnelPanel.setObjects( ((Profile)object).getTunnels() );
                if ( ((Profile)object).getTunnels().size() > 0 ) {
                    
                    Selection.getInstance().setSelected( Tunnel.class, this.tunnelPanel.getRowByObject( ((Profile)object).getTunnels().get( 0 ) ) );
                }
            
            } else {
                
                this.tunnelPanel.setObjects( null );
            }
            
            this.forwardsPanel.setObjects( null );
        
        } else if ( c == Tunnel.class ) {
            
            if ( object != null ) {
                
                this.forwardsPanel.setObjects( ((Tunnel)object).getForwards() );
                if ( ((Tunnel)object).getForwards().size() > 0 ) {
                    Selection.getInstance().setSelected( Forward.class, this.forwardsPanel.getRowByObject( ((Tunnel)object).getForwards().get( 0 ) ) );
                }
            
            } else {
                
                this.forwardsPanel.setObjects( null );
            }

        } else if ( c == Forward.class ) {
            
        }
    }
    
    @Override
    public void buttonClicked( String id, Object object ) {
        
        if ( id.equals( "REVERT" ) ) {
            
            this.config = new Config( this.configOriginal );
            
            this.cacheConfigurationRows();
            
            this.profilesPanel.setObjects( this.config.getProfiles() );
            
            if ( this.config.getProfiles().size() > 0 ) {
                
                Selection.getInstance().setSelected( Profile.class, this.profilesPanel.getRowByObject( this.config.getProfiles().get( 0 ) ) );
            }
            
            this.validateConfig();
            
        } else if ( id.equals( "PROFILES:ADD" ) ) {
            
            Profile profile = new Profile();
            
            profile.setName( this.config.getUniqueProfileName() );
            
            this.config.addProfile( profile );
            this.profilesPanel.refreshBody();
            Selection.getInstance().setSelected( Profile.class, this.profilesPanel.getRowByObject( profile ) );

            this.validateConfig();
        } else if ( id.equals( "TUNNEL:ADD" ) && Selection.getInstance().getSelectedProfile() != null ) {
            
            Tunnel tunnel = new Tunnel();
            
            tunnel.setAlias( this.config.getUniqueTunnelName( Selection.getInstance().getSelectedProfile() ) );
            tunnel.setUser( System.getenv( "USER" ) );
            tunnel.setHostname( "localhost" );
            
            Selection.getInstance().getSelectedProfile().addTunnel( tunnel );
            this.tunnelPanel.refreshBody();
            Selection.getInstance().setSelected( Tunnel.class, this.tunnelPanel.getRowByObject( tunnel ) );

            this.validateConfig();

        } else if ( id.equals( "FORWARDS:ADD" ) && Selection.getInstance().getSelectedTunnel() != null ) {
            
            Forward forward = new Forward();
            
            Selection.getInstance().getSelectedTunnel().addForward( forward );
            this.forwardsPanel.refreshBody();
            Selection.getInstance().setSelected( Forward.class, this.forwardsPanel.getRowByObject( forward ) );
            
            this.validateConfig();

        } else if ( id.equals( "PROFILES:COPY" ) && Selection.getInstance().getSelectedProfile() != null ) {
            
            Profile profile = new Profile( Selection.getInstance().getSelectedProfile() );
            
            this.config.addProfile( profile );
            this.profilesPanel.refreshBody();
            Selection.getInstance().setSelected( Profile.class, this.profilesPanel.getRowByObject( profile ) );
            
            this.validateConfig();
            
        } else if ( id.equals( "TUNNEL:COPY" ) && Selection.getInstance().getSelectedTunnel() != null ) {

            Tunnel tunnel = new Tunnel( Selection.getInstance().getSelectedTunnel() );
            
            Selection.getInstance().getSelectedProfile().addTunnel( tunnel );
            this.tunnelPanel.refreshBody();
            Selection.getInstance().setSelected( Tunnel.class, this.tunnelPanel.getRowByObject( tunnel ) );
            
            this.validateConfig();
            
        } else if ( id.equals( "FORWARDS:COPY" ) && Selection.getInstance().getSelectedForward() != null ) {
            
            Forward forward = new Forward( Selection.getInstance().getSelectedForward() );
            
            Selection.getInstance().getSelectedTunnel().addForward( forward );
            this.forwardsPanel.refreshBody();
            Selection.getInstance().setSelected( Forward.class, this.forwardsPanel.getRowByObject( forward ) );
            
            this.validateConfig();
            
        } else if ( id.equals( "DELETE") ) {
            
            if ( object instanceof Profile ) {
                
                this.config.removeProfileById( ((Profile)object).getId() );
                Selection.getInstance().setSelected( Profile.class, null );
                
            } else if ( object instanceof Tunnel ) {
                
                Selection.getInstance().getSelectedProfile().removeTunnelById( ((Tunnel)object).getId() );
                Selection.getInstance().setSelected(  Tunnel.class, null );
                
            } else if ( object instanceof Forward ) {
                
                Selection.getInstance().getSelectedTunnel().removeForwardById( ((Forward)object).getId() );
                Selection.getInstance().setSelected( Forward.class, null );
            }
            
            this.profilesPanel.refreshBody();
            this.tunnelPanel.refreshBody();
            this.forwardsPanel.refreshBody();
            
            this.validateConfig();
            
        } else if ( id.equals( "NEW" ) ) {
            
            this.config = new Config();
            this.profilesPanel.setObjects( this.config.getProfiles() );
            this.tunnelPanel.setObjects( null);
            this.forwardsPanel.setObjects( null );
            Selection.getInstance().clearSelection();
            this.validateConfig();
        
        } else if ( id.equals( "SAVE" ) ) {
            
            this.config.save();
            this.configOriginal = new Config( this.config );
            this.validateConfig();
        }
    }
    
    private void cacheConfigurationRows() {
        
        logger.debug( "Caching rows in dialog" );
        this.profilesPanel.getRowsCache().clear();
        this.profilesPanel.cacheRowsByObjects( this.config.getAllProfiles() );
        
        this.tunnelPanel.getRowsCache().clear();
        this.tunnelPanel.cacheRowsByObjects( this.config.getAllTunnels() );
        
        this.forwardsPanel.getRowsCache().clear();
        this.forwardsPanel.cacheRowsByObjects( this.config.getAllForwards() );
        logger.debug( "Finished caching rows in dialog" );
    }
    
    @Override
    public void configManagerConfigChanged( Config oldConfig, Config newConfig, boolean valid ) {
        
        if ( this.config != null && this.config.equals( newConfig ) && this.config.equalsMeta( newConfig ) ) {
            return;
        }
        
        this.config         = new Config( newConfig );
        this.configOriginal = new Config( this.config );
        
        this.cacheConfigurationRows();
        
        this.validateConfig();
        
        this.profilesPanel.setObjects( this.config.getProfiles() );
        
        if ( this.config.getProfiles().size() > 0 ) {
            
            Selection.getInstance().setSelected( Profile.class, this.profilesPanel.getRowByObject( this.config.getProfiles().get( 0 ) ) );
        }
    }
    
    @Override
    public void configManagerGlobalConfigChanged( GlobalConfig oldConfig, GlobalConfig newConfig, boolean valid ) {
    }
    
    @Override
    public void guiElementBeginEdit( GUIElement element ) {
        
        if ( this.editingElement != null && this.editingElement != element ) {
            
            this.editingElement.endEdit( true );
        }
        
        this.editingElement = element;
    }
    
    @Override
    public void guiElementEndEdit( GUIElement element, boolean confirm ) {
        
        this.editingElement = null;
        
        this.validateConfig();
    }
}







