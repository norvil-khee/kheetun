package org.khee.kheetun.client.gui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
import org.khee.kheetun.client.gui.Khbutton;
import org.khee.kheetun.client.gui.KhbuttonListener;
import org.khee.kheetun.client.gui.Kholor;
import org.khee.kheetun.client.gui.TextStyle;

@SuppressWarnings("serial")
public class Dialog extends JFrame implements ConfigManagerListener, KhbuttonListener, GUIElementListener, SelectionListener {
    
    public static final Dialog      instance            = new Dialog();
    
    private Config                  config;
    private Config                  configOriginal;
    
    private ConfigPanel<Profile>    profilesPanel       = new ConfigPanel<Profile>( this, "PROFILES", Imx.NONE, TextStyle.CONFIG_HEADER );
    private ConfigPanel<Tunnel>     tunnelPanel         = new ConfigPanel<Tunnel>( this,  "TUNNEL",   Imx.NONE, TextStyle.CONFIG_HEADER );
    private ConfigPanel<Forward>    forwardsPanel       = new ConfigPanel<Forward>( this, "FORWARDS", Imx.NONE, TextStyle.CONFIG_HEADER );
    
    private Khbutton                buttonSave          = new Khbutton( "SAVE",         Imx.SAVE );
    private Khbutton                buttonRevert        = new Khbutton( "REVERT",       Imx.REVERT );
    private Khbutton                buttonUndo          = new Khbutton( "UNDO",         Imx.UNDO );
    private Khbutton                buttonErrors        = new Khbutton( "NO ERRORS",    Imx.WARNING.color( Kholor.OK ) );
    private Khbutton                buttonNew           = new Khbutton( "NEW",          Imx.NEW );

    private GUIElement              editingElement      = null;
    
    private ErrorMatrix             errorMatrix         = new ErrorMatrix();
    private JScrollPane             scrollErrorMatrix;
    private JWindow                 windowVerify        = new JWindow();
    private JLabel                  labelVerify         = new JLabel();

    public Dialog() {
        
        this.setTitle( "Kheetun Configuration" );
        this.setIconImage( Imx.TRAY.getImage() );
        this.setBackground( Color.WHITE );
        
        this.setType( Type.UTILITY );
        
        this.getContentPane().setLayout( new GridBagLayout() );
        
        this.setMinimumSize( new Dimension( 1400, 800 ) );
        this.setMaximumSize( new Dimension( 1400, Integer.MAX_VALUE ) );
        
        this.labelVerify.setBorder( BorderFactory.createCompoundBorder( new LineBorder(Color.DARK_GRAY ), new EmptyBorder( 4, 4, 4, 4 ) ) );
        this.windowVerify.setType( Type.POPUP );
        this.windowVerify.getContentPane().setLayout( new BoxLayout( this.windowVerify.getContentPane(), BoxLayout.X_AXIS ) );
        this.windowVerify.add( this.labelVerify );
        
        this.buttonSave.addButtonListener( this );
        this.buttonRevert.addButtonListener( this );
        this.buttonErrors.addButtonListener( this );
        this.buttonNew.addButtonListener( this );
        
        JPanel title = new JPanel();
        title.setLayout( new GridBagLayout() );

        JLabel labelHeetun  = new JLabel( Imx.KHEETUN );
        labelHeetun.setVerticalAlignment( SwingConstants.BOTTOM );
        labelHeetun.setFont( new Font( Font.DIALOG, Font.PLAIN, 32 ) );
        labelHeetun.setForeground( Color.WHITE );
        
        JLabel labelConfiguratio = new JLabel( "C O N F I G U R A T I O " );
        labelConfiguratio.setVerticalAlignment( SwingConstants.BOTTOM );
        labelConfiguratio.setFont( new Font( Font.DIALOG, Font.PLAIN, 32 ) );
        labelConfiguratio.setForeground( Color.WHITE );
        
        JLabel labelN       = new JLabel( "N", SwingConstants.LEFT );
        labelN.setVerticalAlignment( SwingConstants.BOTTOM );
        labelN.setFont( new Font( Font.DIALOG, Font.BOLD, 32 ) );
        labelN.setForeground( new Color( 0x359eff ) );
        
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
        
        this.buttonSave.setWidth( 80 );
        this.buttonRevert.setWidth( 80 );
        this.buttonUndo.setWidth( 80 );
        this.buttonNew.setWidth( 80 );
        this.buttonErrors.setWidth( 120 );
        this.buttonErrors.setId( "ERRORS" );
        
        panelButtons.add( buttonSave );
        panelButtons.add( Box.createHorizontalStrut( 32 ) );
        panelButtons.add( buttonRevert );
//        panelButtons.add( Box.createHorizontalStrut( 32 ) );
//        panelButtons.add( buttonUndo );
        panelButtons.add( Box.createHorizontalStrut( 32 ) );
        panelButtons.add( buttonNew );
        panelButtons.add( Box.createHorizontalGlue() );
        panelButtons.add( buttonErrors );
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.weightx = 0.0f;
        c.anchor = GridBagConstraints.LINE_START;
        
        title.add( labelHeetun, c );
        
        c.weightx = 1.0f;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        title.add( Box.createHorizontalGlue(), c );
        
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0f;
        
        title.add( labelConfiguratio, c );
        title.add( labelN, c );
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0f;
        c.weighty = 0.0f;
        c.gridy = 1;
        c.gridx = 0;
        c.insets = new Insets( 8, 0, 0, 0 );
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        title.add( panelButtons, c );
        
        profilesPanel.setMinimumSize( new Dimension( 280, 1 ) );
        profilesPanel.setPreferredSize( new Dimension( 280, 1 ) );
        profilesPanel.setMaximumSize( new Dimension( 280, Integer.MAX_VALUE ) );
        
        tunnelPanel.setMinimumSize( new Dimension( 600, 1 ) );
        tunnelPanel.setPreferredSize( new Dimension( 600, 1 ) );
        tunnelPanel.setMaximumSize( new Dimension( 600, Integer.MAX_VALUE ) );

        this.scrollErrorMatrix = new JScrollPane( this.errorMatrix );
        this.scrollErrorMatrix.setMinimumSize( new Dimension( 622, 100 ) );
        this.scrollErrorMatrix.setPreferredSize( new Dimension( 622, 100 ) );
        this.scrollErrorMatrix.setMaximumSize( new Dimension( 622, 100 ) );
        this.scrollErrorMatrix.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        
        this.getContentPane().add( title,               new GridBagConstraints( 0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets( 8, 0, 0, 0 ), 0, 0 ) );
        this.getContentPane().add( scrollErrorMatrix,   new GridBagConstraints( 0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets( 0, 0, 32, 0 ), 0, 0 ) );
        this.getContentPane().add( this.profilesPanel,  new GridBagConstraints( 0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.VERTICAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        this.getContentPane().add( this.tunnelPanel,    new GridBagConstraints( 1, 2, 1, 1, 0.0, 1.0, GridBagConstraints.PAGE_END, GridBagConstraints.VERTICAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        this.getContentPane().add( this.forwardsPanel,  new GridBagConstraints( 2, 2, 1, 1, 1.0, 1.0, GridBagConstraints.LAST_LINE_END, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        
        ConfigManager.addConfigManagerListener( this );
        Selection.getInstance().addSelectionListener( this );
    }
    
    public static Dialog getInstance() {
        
        return Dialog.instance;
    }
    
    public Config getConfig() {
        
        return this.config;
    }
    
    public void showVerify( Point p, Icon icon, String hint ) {
        
        this.labelVerify.setIcon( icon );
        this.labelVerify.setText( hint );
        this.windowVerify.setLocation( p.x, p.y - this.windowVerify.getHeight() - 4 );
        this.windowVerify.pack();
        this.windowVerify.setVisible( true );
    }
    
    public void hideVerify() {
        
        this.windowVerify.setVisible( false );
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
            
            this.profilesPanel.setObjects( this.config.getProfiles() );
            this.tunnelPanel.setObjects( null );
            this.forwardsPanel.setObjects( null );
            
            this.validateConfig();
            
        } else if ( id.equals( "PROFILES" ) ) {
            
            Profile profile = new Profile();
            
            profile.setName( this.config.getUniqueProfileName() );
            
            this.config.addProfile( profile );
            this.profilesPanel.refreshBody();
            Selection.getInstance().setSelected( Profile.class, this.profilesPanel.getRowByObject( profile ) );

            this.validateConfig();
        
        } else if ( id.equals( "TUNNEL" ) && Selection.getInstance().getSelectedProfile() != null ) {
            
            Tunnel tunnel = new Tunnel();
            
            tunnel.setAlias( this.config.getUniqueTunnelName( Selection.getInstance().getSelectedProfile() ) );
            tunnel.setUser( System.getenv( "USER" ) );
            tunnel.setHostname( "localhost" );
            
            Selection.getInstance().getSelectedProfile().addTunnel( tunnel );
            this.tunnelPanel.refreshBody();
            Selection.getInstance().setSelected( Tunnel.class, this.tunnelPanel.getRowByObject( tunnel ) );

            this.validateConfig();

        } else if ( id.equals( "FORWARDS" ) && Selection.getInstance().getSelectedTunnel() != null ) {
            
            Forward forward = new Forward();
            
            Selection.getInstance().getSelectedTunnel().addForward( forward );
            this.forwardsPanel.refreshBody();
            Selection.getInstance().setSelected( Forward.class, this.forwardsPanel.getRowByObject( forward ) );
            
            this.validateConfig();

        } else if ( id.equals( "DELETE") ) {
            
            if ( object instanceof Profile ) {
                
                this.config.getProfiles().remove( object );
                Selection.getInstance().setSelected( Profile.class, null );
                
            } else if ( object instanceof Tunnel ) {
                
                Selection.getInstance().getSelectedProfile().getTunnels().remove( object );
                Selection.getInstance().setSelected(  Tunnel.class, null );
                
            } else if ( object instanceof Forward ) {
                
                Selection.getInstance().getSelectedTunnel().getForwards().remove( object );
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
            
            System.out.println( "SAVING CONFIG" );
            
            this.config.save();
            this.configOriginal = new Config( this.config );
            this.validateConfig();
        }
    }
    
    @Override
    public void configManagerConfigChanged( Config oldConfig, Config newConfig, boolean valid ) {
        
        if ( this.config != null && this.config.equals( newConfig ) && this.config.equalsMeta( newConfig ) ) {
            return;
        }
        
        this.config         = newConfig;
        this.configOriginal = new Config( this.config );
        
        this.validateConfig();
        
        this.profilesPanel.setObjects( this.config.getProfiles() );
        
        if ( this.config.getProfiles().size() > 0 ) {
            
            Selection.getInstance().setSelected( Profile.class, this.profilesPanel.getRowByObject( this.config.getProfiles().get( 0 ) ) );
            
            if ( this.config.getProfiles().get( 0 ).getTunnels().size() > 0 ) {
                
                Selection.getInstance().setSelected( Tunnel.class, this.tunnelPanel.getRowByObject( this.config.getProfiles().get( 0 ).getTunnels().get( 0 ) ) );
                
                if ( this.config.getProfiles().get( 0 ).getTunnels().get( 0 ).getForwards().size() > 0 ) {
                    
                    Selection.getInstance().setSelected( Forward.class, this.forwardsPanel.getRowByObject( this.config.getProfiles().get( 0 ).getTunnels().get( 0 ).getForwards().get( 0 ) ) );
                }
            }
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







