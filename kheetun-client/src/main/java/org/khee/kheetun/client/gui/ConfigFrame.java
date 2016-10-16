package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.TunnelClient;
import org.khee.kheetun.client.TunnelManager;
import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.client.verify.VerifierFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

public class ConfigFrame extends JFrame {
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    static final long serialVersionUID = 42;
    
    private final ImageIcon iconConnected       = Imx.OK;
    private final ImageIcon iconDisconnected    = Imx.WARNING;
    
    private final File      fileConfig          = new File( System.getProperty( "user.home" ) + "/.kheetun/kheetun.xml" );
    
    private JToolBar toolbarConfig;
    private JToolBar toolbarProfile;
    private JToolBar toolbarTunnel;
    private JToolBar toolbarMisc;
    private JToolBar toolbarNone;
    private JButton buttonSaveConfig;
    private JButton buttonNewConfig;
    private JButton buttonRevertConfig;
    private JButton buttonAddProfile;
    private JButton buttonDeleteProfile;
    private JButton buttonRenameProfile;
    private JButton buttonStartTunnel;
    private JButton buttonStopTunnel;
    private JPanel panelProfiles;
    private JLabel labelProfiles;
    private JComboBox<Profile> comboProfiles;
    private JScrollPane scrollTunnels;
    private JTable tableTunnels;
    private JScrollPane scrollForwards;
    private JTable tableForwards;
    private JButton buttonConnect;
    private JButton buttonAssignIps;
    private JTextField fieldStartIp;
    private JTextField fieldPort;
    
    private boolean connected = false;
    
    private Config configCurrent;
    private Config configOriginal;
    
    private ObjectTableModel<Tunnel> storeTunnels;
    private ObjectTableModel<Forward> storeForwards;
    
    private Profile selectedProfile;
    private Tunnel selectedTunnel;
    private Forward selectedForward;
    
    private AddRenderer buttonAddTunnel;
    private AddRenderer buttonAddForward;
    
    private ArrayList<Tunnel> runningTunnels = new ArrayList<Tunnel>();
    
    private ArrayList<ConfigFrameListener> listeners = new ArrayList<ConfigFrameListener>();
    
    public ConfigFrame() {

        setTitle( "kheetun, your friendly tunnel manager" );
        setIconImage( Imx.KHEETUN.getImage() );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thisWindowClosing(e);
            }
        });
        
        storeTunnels  = new ObjectTableModel<Tunnel>( Tunnel.class );
        storeForwards = new ObjectTableModel<Forward>( Forward.class );  
        
        initComponents();
        initializeEvents();
        setPreferredSize( new Dimension( 1024, 600 ) );
        setMinimumSize( new Dimension( 1024, 600 ) );
        pack();
        setLocationRelativeTo(null);
    }

    private void thisWindowClosing(WindowEvent e) {
        
        setVisible( false );
    }
    
    public void addConfigChangedListener( ConfigFrameListener listener ) {
        
        listeners.add( listener );
    }
    
    private void initializeEvents() {
        
        comboProfiles.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                selectedProfile = (Profile)comboProfiles.getSelectedItem();
                
                storeTunnels.clear();

                if ( selectedProfile != null ) {
                    buttonDeleteProfile.setEnabled( true );
                    buttonAddTunnel.setEnabled( true );
                    buttonRenameProfile.setEnabled( true );
                    storeTunnels.addAll( selectedProfile.getTunnels() );
                    fieldStartIp.setEnabled( true );
                    buttonAssignIps.setEnabled( true );
                    
                    if ( selectedProfile.getBaseBindIp() != null ) {
                        fieldStartIp.setText( selectedProfile.getBaseBindIp() );
                    } else {
                        fieldStartIp.setText( "" );
                    }
                    
                } else {
                    buttonRenameProfile.setEnabled( false );
                    buttonAddTunnel.setEnabled( false );
                    buttonDeleteProfile.setEnabled( false );
                    fieldStartIp.setText( "" );
                    fieldStartIp.setEnabled( false );
                    buttonAssignIps.setEnabled( false );
                }

                tableTunnels.getTableHeader().repaint();

                storeTunnels.fireTableDataChanged();
                if ( storeTunnels.getRowCount() > 0 ) {
                    tableTunnels.getSelectionModel().setSelectionInterval( 0, 0 );
                }
            }
        });
        
        tableTunnels.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                
                selectedTunnel = storeTunnels.get( tableTunnels.getSelectedRow()  );

                storeForwards.clear();
                
                if ( selectedTunnel != null ) {
                    buttonAddForward.setEnabled( true );
                    storeForwards.addAll( selectedTunnel.getForwards() );
                } else {
                    buttonAddForward.setEnabled( false );
                }
                
                tableForwards.getTableHeader().repaint();

                storeForwards.fireTableDataChanged();
                if ( storeForwards.getRowCount() > 0 ) {
                    tableForwards.getSelectionModel().setSelectionInterval( 0, 0 );
                }
            }
        });

        tableForwards.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                
                selectedForward = storeForwards.get( tableForwards.getSelectedRow()  );
            }
        });
        
        
        buttonAddProfile.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                String name = (String)JOptionPane.showInputDialog( null, "Give me a name", "New Profile", JOptionPane.PLAIN_MESSAGE, null, null, "" );
                
                if ( name != null && name.length() > 0 ) {
                    Profile profile = new Profile();
                    profile.setName( name );
                    
                    configCurrent.addProfile( profile );
                    comboProfiles.addItem( profile );
                    comboProfiles.setSelectedItem( profile );
                    
                    checkDirty();
                }
            }
        });
        
        buttonDeleteProfile.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if ( selectedProfile == null ) {
                    return;
                }
                
                int answer = JOptionPane.showConfirmDialog( null, "Are you quite sure?", "Delete Profile", JOptionPane.YES_NO_OPTION );
                
                if ( answer == JOptionPane.YES_OPTION ) {
                    
                    configCurrent.getProfiles().remove( selectedProfile );
                    comboProfiles.removeItem( selectedProfile );
                    
                    checkDirty();
                }
            }
        });
        
        buttonRenameProfile.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if ( selectedProfile == null ) {
                    return;
                }
                
                String name = (String)JOptionPane.showInputDialog( null, "And so thou shalt be rebaptised to:", "Rename Profile", JOptionPane.PLAIN_MESSAGE, null, null, "" );
                
                if ( name != null && name.length() > 0 ) {
                    
                    selectedProfile.setName( name );
                    comboProfiles.repaint();
                    checkDirty();
                    
                   // comboProfiles.getItemAt( comboProfiles.getSelectedIndex() ).
                }
            }
        });

        buttonNewConfig.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if ( checkDirty() && ! confirm() ) {
                    return;
                }

                setConfig( new Config() );
            }
        });
        
        buttonSaveConfig.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                doSave();
            }
        });
        
        buttonRevertConfig.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                setConfig( configOriginal );
            }
        });

        buttonAddTunnel.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if ( selectedProfile == null ) {
                    return;
                }
                
                Tunnel tunnel = new Tunnel();

                selectedProfile.addTunnel( tunnel );
                storeTunnels.addRow( tunnel );
                tableTunnels.getSelectionModel().setSelectionInterval( storeTunnels.getRowCount() - 1, storeTunnels.getRowCount() - 1 );
                checkDirty();
            }
        });
        
        buttonAddForward.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if ( selectedTunnel == null ) {
                    return;
                }
                
                Forward forward = new Forward();

                selectedTunnel.addForward( forward );
                storeForwards.addRow( forward );
                tableForwards.getSelectionModel().setSelectionInterval( storeForwards.getRowCount() - 1, storeForwards.getRowCount() - 1 );
                checkDirty();
            }
        });
        
        buttonStartTunnel.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                doStartTunnel();
            }
        });
        
        buttonStopTunnel.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                doStopTunnel();
                
            }
        });

        storeTunnels.addTableModelListener( new TableModelListener() {
            
            public void tableChanged(TableModelEvent e) {

                if ( e.getType() == TableModelEvent.UPDATE && e.getColumn() != -1 ) {
                    checkDirty();
                }
            }
        });

        storeForwards.addTableModelListener( new TableModelListener() {
            
            public void tableChanged(TableModelEvent e) {

                if ( e.getType() == TableModelEvent.UPDATE && e.getColumn() != -1 ) {
                    
                    checkDirty();
                }
            }
        });
        
        fieldPort.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                try {
                    configCurrent.setPort( new Integer( fieldPort.getText() ) );
                } catch ( NumberFormatException eNumber ) {
                    
                    fieldPort.setText( configCurrent.getPort().toString() );
                    
                    showError( "Numbers only!" );
                    return;
                }
                
                checkDirty();
                ((Component)e.getSource()).getParent().requestFocusInWindow();
            }
        });
        
        fieldStartIp.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                if ( fieldStartIp.getText().matches( "127\\.\\d+\\.\\d+\\.\\d+" ) ) {
                    
                    selectedProfile.setBaseBindIp( fieldStartIp.getText() );
                    assignBindIps();
                    
                } else if ( fieldStartIp.getText().equals( "" ) ) {
                    
                    selectedProfile.setBaseBindIp( null );
                } else {
                    
                    fieldStartIp.setText( selectedProfile.getBaseBindIp() != null ? selectedProfile.getBaseBindIp() : "" );
                    showError( "Please give a valid local IP, ending with 1 (127.x.x.1) - or leave blank to disable auto assignment" );
                }
                checkDirty();
                ((Component)e.getSource()).getParent().requestFocusInWindow();
            }
        });

        
        buttonConnect.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if ( ! connected ) {
                    doConnect();
                }
            }
        });
        
    }
    
    private void assignBindIps() {
        
        HashMap<String, String> ipByHostname = new HashMap<String, String>();
        
        Pattern patternIp = Pattern.compile( "127\\.(?<oct2>\\d+)\\.(?<oct3>\\d+)\\.1" );
        Matcher matcherIp = patternIp.matcher( selectedProfile.getBaseBindIp() );
        
        if ( matcherIp.matches() ) {
            
            Integer oct2 = new Integer( matcherIp.group( "oct2" ) );
            Integer oct3 = new Integer( matcherIp.group( "oct3" ) );
            Integer oct4 = 1;
        
            for ( Tunnel tunnel : selectedProfile.getTunnels() ) {
                
                for ( Forward forward : tunnel.getForwards() ) {
                    
                    if ( forward.getType().equals( Forward.REMOTE ) ) {
                        continue;
                    }
                    
                    String bindIp = "127." + oct2.toString() + "." + oct3.toString() + "." + oct4.toString();
                    
                    if ( ! ipByHostname.containsKey( forward.getForwardedHost() ) ) {
                        
                        ipByHostname.put( forward.getForwardedHost(), bindIp );
                        
                        if ( oct4++ > 255 ) {
                            oct4 = 1;
                            if ( oct3++ > 255 ) {
                                oct3 = 1;
                                if ( oct2++ > 255 ) {
                                    showError( "There are too many forwards in this profile to assign unique local IPs" );
                                    return;
                                }
                            }
                        }
                    } else {
                        bindIp = ipByHostname.get( forward.getForwardedHost() );
                    }
                    
                    forward.setBindIp( bindIp );
                }
            }
            
            storeForwards.fireTableDataChanged();
            checkDirty();
        }
    }
    
    public void error( Tunnel tunnel, String error ) {
        
        showError( error );
    }
    
    public void connected() {
        buttonConnect.setIcon( iconConnected );
        buttonConnect.setToolTipText( "Connected to server" );
        buttonStartTunnel.setEnabled( true );
        buttonStopTunnel.setEnabled( true );
        connected = true;
        TunnelClient.sendQueryTunnels();
    }

    public void disconnected() {
        buttonConnect.setIcon( iconDisconnected );
        buttonConnect.setToolTipText( "Not connected to server, click to retry connection" );
        buttonStartTunnel.setEnabled( false );
        buttonStopTunnel.setEnabled( false );
        connected = false;
    }
    
    public void tunnelStarted( String signature ) {
        
        ArrayList<Tunnel> tunnels = configCurrent.findTunnels( signature );
        
        for( Tunnel tunnel : tunnels ) {
            runningTunnels.add( tunnel );
            tunnel.setActive( true );
        }
        
        int row = tableTunnels.getSelectedRow();
        storeTunnels.fireTableDataChanged();
        tableTunnels.getSelectionModel().setSelectionInterval( row, row );
    }
    
    public void tunnelStopped( String signature ) {
        
        ArrayList<Tunnel> tunnels = configCurrent.findTunnels( signature );
        
        for( Tunnel tunnel : tunnels ) {
            runningTunnels.remove( tunnel );
            tunnel.setActive( false );
        }

        int row = tableTunnels.getSelectedRow();
        storeTunnels.fireTableDataChanged();
        tableTunnels.getSelectionModel().setSelectionInterval( row, row );
    }
    
    public void activeTunnels( ArrayList<String> signatures ) {
        
        runningTunnels.clear();
        
        for ( String signature : signatures ) {
            tunnelStarted( signature );
        }
    }
    
    private void doConnect() {
        
//        TunnelClient.init();
//        TunnelClient.connect( configCurrent.getPort() );
    }

    private void doSave() {
        
        // if port differs from before, reconnect
        //
        if ( configCurrent.getPort() != configOriginal.getPort() ) {
            
            if ( connected ) {  
                TunnelClient.disconnect();
            }
            doConnect();
        }
            
        configOriginal = new Config( configCurrent );
        
        try {
            configOriginal.save( fileConfig );
        } catch ( Exception e ) {
            
            logger.error( e.getMessage() );
            showError( e.getMessage() );
        } 
        
        for ( ConfigFrameListener listener : listeners ) {
            listener.configChanged( configOriginal );
        }
        
        checkDirty();
    }
    
   
    private void doStartTunnel() {
        
        if ( selectedTunnel == null ) {
            return;
        }
        
        if ( selectedTunnel.getForwards().size() == 0 ) {
            showError( "Starting a tunnel with no forwards... that's pretty smart." );
            return;
        }
        
        TunnelManager.startTunnel( selectedTunnel );
    }
    
    private void doStopTunnel() {
        
        if ( selectedTunnel == null ) {
            return;
        }
        
        TunnelManager.stopTunnel( selectedTunnel );
    }    
    
    private boolean confirm() {

        int answer = JOptionPane.showConfirmDialog( this, "Are you quite sure? All your precious changes would be in vain!", "New Config", JOptionPane.YES_NO_OPTION );
        
        if ( answer == JOptionPane.NO_OPTION ) {
            return false;
        }
        
        return true;
    }
    
    private void showError( String error ) {
        
        JOptionPane.showMessageDialog( this, error, "Error", JOptionPane.ERROR_MESSAGE );
    }

    private void initComponents() {

        Container contentPane = getContentPane();
        contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
        
        toolbarConfig       = new JToolBar();
        toolbarProfile      = new JToolBar();
        toolbarTunnel       = new JToolBar();
        toolbarMisc         = new JToolBar();
        toolbarNone         = new JToolBar();
        buttonSaveConfig          = new JButton( Imx.SAVE );
        buttonSaveConfig.setToolTipText( "Save Configuration" );
        buttonNewConfig           = new JButton( Imx.NEW );
        buttonNewConfig.setToolTipText( "New Configuration" );
        buttonRevertConfig        = new JButton( Imx.REVERT );
        buttonRevertConfig.setToolTipText( "Undo all changes" );
        buttonAddProfile    = new JButton( Imx.PLUS );
        buttonAddProfile.setToolTipText( "Create Profile" );
        buttonDeleteProfile = new JButton( Imx.MINUS );
        buttonDeleteProfile.setToolTipText( "Remove Profile" );
        buttonRenameProfile = new JButton( Imx.RENAME );
        buttonRenameProfile.setToolTipText( "Rename Profile" );
        buttonStartTunnel   = new JButton( Imx.START );
        buttonStartTunnel.setToolTipText( "Start Tunnel" );
        buttonStopTunnel    = new JButton( Imx.STOP );
        buttonStopTunnel.setToolTipText( "Stop Tunnel" );
        buttonAssignIps     = new JButton( Imx.LION );
        buttonAssignIps.setToolTipText( "Automatically assign bind IPs" );
        buttonConnect       = new JButton( iconDisconnected );
        buttonConnect.setToolTipText( "No connection to server" );
        
        fieldPort           = new JTextField();
        fieldPort.setHorizontalAlignment( JTextField.RIGHT );
        fieldStartIp        = new JTextField();

        fieldStartIp.setEnabled( false );
        buttonSaveConfig.setEnabled( false );
        buttonRevertConfig.setEnabled( false );
        buttonDeleteProfile.setEnabled( false );
        buttonRenameProfile.setEnabled( false );
        buttonStartTunnel.setEnabled( false );
        buttonStopTunnel.setEnabled( false );
        buttonAddProfile.setEnabled( false );
        buttonAssignIps.setEnabled( false );
        
        JComboBox<String> comboForwardType = new JComboBox<String>( new String[] { "local", "remote" } );
        
        
        JButton[] buttons   = { buttonSaveConfig, buttonRevertConfig, buttonAddProfile, buttonDeleteProfile, buttonRenameProfile, buttonStartTunnel, buttonStopTunnel, buttonAssignIps };
        
        for( JButton button : buttons ) {
            button.setPreferredSize( new Dimension( 32,  32 ) );
        }
        
        panelProfiles = new JPanel();
        labelProfiles = new JLabel();

        scrollTunnels = new JScrollPane();
        scrollTunnels.setBorder( new TitledBorder( "Tunnels" ) );
        scrollTunnels.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        tableTunnels = new JTable( storeTunnels );
        tableTunnels.setAutoCreateColumnsFromModel( true );
        DefaultTableColumnModel columns = (DefaultTableColumnModel)tableTunnels.getColumnModel();
        tableTunnels.setShowGrid( false );
        tableTunnels.setRowHeight( 24 );

        ValueRenderer rendererPort  = new ValueRenderer( Integer.class, VerifierFactory.getPortVerifier(),      JTextField.RIGHT, new Insets( 0, 0, 0, 16 ) );
        ValueRenderer rendererIP    = new ValueRenderer( String.class,  VerifierFactory.getIpAddressVerifier(), JTextField.LEFT,  new Insets( 0, 16, 0, 0 ) );
        ValueRenderer rendererHost  = new ValueRenderer( String.class,  VerifierFactory.getHostnameVerifier(),  JTextField.LEFT,  new Insets( 0, 8, 0, 0 ) );
        ValueRenderer rendererUser  = new ValueRenderer( String.class,  VerifierFactory.getUserVerifier(),      JTextField.LEFT,  new Insets( 0, 8, 0, 0 ) );
        ValueRenderer rendererAlias = new ValueRenderer( String.class,  VerifierFactory.getAliasVerifier(),     JTextField.LEFT,  new Insets( 0, 8, 0, 0 ) );
        
        
        DeleteButtonRenderer rendererDeleteTunnel = new DeleteButtonRenderer( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                int index = tableTunnels.getSelectedRow();
                selectedProfile.getTunnels().remove( selectedTunnel );
                storeTunnels.removeRow( selectedTunnel );
                
                if ( index > storeTunnels.getRowCount() - 1 ) {
                    index = storeTunnels.getRowCount() - 1;
                }
                
                tableTunnels.getSelectionModel().setSelectionInterval( index, index );
                checkDirty();
            }
        });

        DeleteButtonRenderer rendererDeleteForward = new DeleteButtonRenderer( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                int index = tableForwards.getSelectedRow();
                selectedTunnel.getForwards().remove( selectedForward );
                storeForwards.removeRow( selectedForward );

                if ( index > storeForwards.getRowCount() - 1 ) {
                    index = storeForwards.getRowCount() - 1;
                }
                
                tableForwards.getSelectionModel().setSelectionInterval( index, index );
                checkDirty();
            }
        });
        
        columns.getColumn( columns.getColumnIndex( "Status" ) ).setMinWidth( 50 );
        columns.getColumn( columns.getColumnIndex( "Status" ) ).setMaxWidth( 50 );
        columns.getColumn( columns.getColumnIndex( "Status" ) ).setCellRenderer( new ActiveRenderer() );
        columns.getColumn( columns.getColumnIndex( "Alias" ) ).setMinWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Alias" ) ).setMaxWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Alias" ) ).setCellRenderer( rendererAlias );
        columns.getColumn( columns.getColumnIndex( "Alias" ) ).setCellEditor( rendererAlias );
        columns.getColumn( columns.getColumnIndex( "User" ) ).setMinWidth( 200 );
        columns.getColumn( columns.getColumnIndex( "User" ) ).setCellRenderer( rendererUser );
        columns.getColumn( columns.getColumnIndex( "User" ) ).setCellEditor( rendererUser );
        columns.getColumn( columns.getColumnIndex( "Host" ) ).setMinWidth( 200 );
        columns.getColumn( columns.getColumnIndex( "Host" ) ).setCellRenderer( rendererHost );
        columns.getColumn( columns.getColumnIndex( "Host" ) ).setCellEditor( rendererHost );
        columns.getColumn( columns.getColumnIndex( "SSH Key" ) ).setMinWidth( 160 );
        columns.getColumn( columns.getColumnIndex( "SSH Key" ) ).setMaxWidth( 160 );
        columns.getColumn( columns.getColumnIndex( "SSH Key" ) ).setCellRenderer( new FileRenderer() );
        columns.getColumn( columns.getColumnIndex( "SSH Key" ) ).setCellEditor( new FileRenderer() );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setMinWidth( 50 );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setMaxWidth( 50 );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setCellRenderer( new DeleteButtonRenderer( null ) );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setCellEditor( rendererDeleteTunnel );
        
        buttonAddTunnel = new AddRenderer( tableTunnels.getTableHeader(), columns.getColumnIndex( "Delete" ) );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setHeaderRenderer( buttonAddTunnel );
        tableTunnels.getTableHeader().setReorderingAllowed( false );
        tableTunnels.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        
        scrollForwards = new JScrollPane();
        scrollForwards.setBorder( new TitledBorder( "Forwards" ) );
        scrollForwards.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        tableForwards = new JTable( storeForwards );
        tableForwards.setAutoCreateColumnsFromModel( true );
        columns = (DefaultTableColumnModel)tableForwards.getColumnModel();
        tableForwards.setShowGrid( false );
        tableForwards.setRowHeight( 24 );
//        columns.getColumn( columns.getColumnIndex( "Status" ) ).setMinWidth( 50 );
//        columns.getColumn( columns.getColumnIndex( "Status" ) ).setMaxWidth( 50 );
        columns.getColumn( columns.getColumnIndex( "Type" ) ).setMinWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Type" ) ).setMaxWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Type" ) ).setCellRenderer( new ForwardTypeRenderer() );
        columns.getColumn( columns.getColumnIndex( "Type" ) ).setCellEditor( new DefaultCellEditor( comboForwardType ) );
        columns.getColumn( columns.getColumnIndex( "Bind IP" ) ).setMinWidth( 120 );
        columns.getColumn( columns.getColumnIndex( "Bind IP" ) ).setMaxWidth( 120 );
        columns.getColumn( columns.getColumnIndex( "Bind IP" ) ).setCellEditor( rendererIP );
        columns.getColumn( columns.getColumnIndex( "Bind IP" ) ).setCellRenderer( rendererIP );
        columns.getColumn( columns.getColumnIndex( "Bind Port" ) ).setMinWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Bind Port" ) ).setMaxWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Bind Port" ) ).setCellEditor( rendererPort );
        columns.getColumn( columns.getColumnIndex( "Bind Port" ) ).setCellRenderer( rendererPort );
        columns.getColumn( columns.getColumnIndex( "Forwarded Host" ) ).setMinWidth( 200 );
        columns.getColumn( columns.getColumnIndex( "Forwarded Host" ) ).setCellRenderer( rendererHost );
        columns.getColumn( columns.getColumnIndex( "Forwarded Host" ) ).setCellEditor( rendererHost );
        columns.getColumn( columns.getColumnIndex( "Forwarded Port" ) ).setMinWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Forwarded Port" ) ).setMaxWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Forwarded Port" ) ).setCellEditor( rendererPort );
        columns.getColumn( columns.getColumnIndex( "Forwarded Port" ) ).setCellRenderer( rendererPort );
        columns.getColumn( columns.getColumnIndex( "Hosts Entry" ) ).setMinWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Hosts Entry" ) ).setMaxWidth( 100 );
        columns.getColumn( columns.getColumnIndex( "Comment" ) ).setMinWidth( 160 );
        columns.getColumn( columns.getColumnIndex( "Comment" ) ).setMaxWidth( 160 );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setMinWidth( 50 );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setMaxWidth( 50 );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setCellRenderer( new DeleteButtonRenderer( null ) );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setCellEditor( rendererDeleteForward );

        buttonAddForward = new AddRenderer( tableForwards.getTableHeader(), columns.getColumnIndex( "Delete" ) );
        columns.getColumn( columns.getColumnIndex( "Delete" ) ).setHeaderRenderer( buttonAddForward );
        tableForwards.getTableHeader().setReorderingAllowed( false );
        tableForwards.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        
        toolbarConfig.setFloatable(false);
        toolbarConfig.setRollover(true);
        toolbarProfile.setFloatable(false);
        toolbarProfile.setRollover(true);
        toolbarTunnel.setFloatable(false);
        toolbarTunnel.setRollover(true);
        toolbarMisc.setFloatable(false);
        toolbarMisc.setRollover(true);
        toolbarNone.setFloatable(false);
        toolbarNone.setRollover(true);

        toolbarConfig.add(buttonNewConfig);
        toolbarConfig.add(buttonSaveConfig);
        toolbarConfig.add(buttonRevertConfig);
        toolbarConfig.add( Box.createHorizontalStrut( 32 ) );

        toolbarProfile.add(buttonAddProfile);
        toolbarProfile.add(buttonDeleteProfile);
        toolbarProfile.add(buttonRenameProfile);
        toolbarProfile.add( Box.createHorizontalStrut( 32 ) );
        
        toolbarTunnel.add(buttonStartTunnel);
        toolbarTunnel.add(buttonStopTunnel);
        toolbarTunnel.add( Box.createHorizontalStrut( 32 ) );
        
        toolbarNone.setMargin( new Insets( 0,  0,  0,  8 ) );
        toolbarNone.add( Box.createHorizontalGlue() );
        fieldPort.setMaximumSize( new Dimension( 100, fieldPort.getPreferredSize().height ) );
        fieldPort.setMinimumSize( new Dimension( 100, fieldPort.getPreferredSize().height ) );
        fieldPort.setPreferredSize( new Dimension( 100, fieldPort.getPreferredSize().height ) );
        toolbarNone.add( new JLabel( "Port" ) );
        toolbarNone.add( Box.createHorizontalStrut( 8 ) );
        toolbarNone.add( fieldPort );
        toolbarNone.add( Box.createHorizontalStrut( 8 ) );
        toolbarNone.add( buttonConnect );

        scrollTunnels.setViewportView(tableTunnels);
        scrollForwards.setViewportView(tableForwards);

        panelProfiles.setLayout( new BoxLayout( panelProfiles, BoxLayout.LINE_AXIS ) );

        comboProfiles = new JComboBox<Profile>();
        comboProfiles.setMaximumSize( new Dimension( 200, comboProfiles.getPreferredSize().height ) );
        comboProfiles.setMinimumSize( new Dimension( 200, comboProfiles.getPreferredSize().height ) );
        comboProfiles.setPreferredSize( new Dimension( 200, comboProfiles.getPreferredSize().height ) );
        fieldStartIp.setMaximumSize( new Dimension( 200, comboProfiles.getPreferredSize().height ) );
        fieldStartIp.setMinimumSize( new Dimension( 200, comboProfiles.getPreferredSize().height ) );
        fieldStartIp.setPreferredSize( new Dimension( 200, comboProfiles.getPreferredSize().height ) );
        comboProfiles.setRenderer( new ProfileRenderer() );
        labelProfiles.setText("Profile");
        labelProfiles.setHorizontalAlignment(SwingConstants.LEFT);
        panelProfiles.add( Box.createHorizontalStrut( 5 ) );
        panelProfiles.add(labelProfiles);
        panelProfiles.add( Box.createHorizontalStrut( 10 ) );
        panelProfiles.add(comboProfiles);
        panelProfiles.add( Box.createHorizontalStrut( 32 ) );
        panelProfiles.add( new JLabel( "Base bind IP" ) );
        panelProfiles.add( Box.createHorizontalStrut( 5 ) );
        panelProfiles.add( fieldStartIp );
//        panelProfiles.add( Box.createHorizontalStrut( 10 ) );
//        panelProfiles.add( buttonAssignIps );
        
        
        JPanel panelTunnels = new JPanel();
        panelTunnels.setLayout( new BoxLayout( panelTunnels, BoxLayout.X_AXIS ) );
        panelTunnels.add( scrollTunnels );
        JPanel panelForwards = new JPanel();
        panelForwards.setLayout( new BoxLayout( panelForwards, BoxLayout.X_AXIS ) );
        panelForwards.add( scrollForwards );
        
        toolbarConfig.setMaximumSize( new Dimension( toolbarConfig.getPreferredSize().width, toolbarConfig.getPreferredSize().height ) ) ;
        toolbarConfig.setMinimumSize( new Dimension( toolbarConfig.getPreferredSize().width, toolbarConfig.getPreferredSize().height ) ) ;
        toolbarProfile.setMaximumSize( new Dimension( toolbarProfile.getPreferredSize().width, toolbarProfile.getPreferredSize().height ) ) ;
        toolbarProfile.setMinimumSize( new Dimension( toolbarProfile.getPreferredSize().width, toolbarProfile.getPreferredSize().height ) ) ;
        toolbarTunnel.setMaximumSize( new Dimension( toolbarTunnel.getPreferredSize().width, toolbarTunnel.getPreferredSize().height ) ) ;
        toolbarTunnel.setMinimumSize( new Dimension( toolbarTunnel.getPreferredSize().width, toolbarTunnel.getPreferredSize().height ) ) ;
        toolbarMisc.setMaximumSize( new Dimension( toolbarMisc.getPreferredSize().width * 2, toolbarMisc.getPreferredSize().height ) ) ;
        toolbarMisc.setMinimumSize( new Dimension( toolbarMisc.getPreferredSize().width * 2, toolbarMisc.getPreferredSize().height ) ) ;
        toolbarNone.setMaximumSize( new Dimension( Integer.MAX_VALUE, toolbarTunnel.getPreferredSize().height ) ) ;

        panelProfiles.setMaximumSize( new Dimension( Integer.MAX_VALUE, panelProfiles.getPreferredSize().height ) ) ;
        panelProfiles.setMinimumSize( new Dimension( Integer.MAX_VALUE, panelProfiles.getPreferredSize().height ) ) ;
        panelTunnels.setPreferredSize( new Dimension( 800, 200 ) );
        panelTunnels.setMinimumSize( new Dimension( Integer.MAX_VALUE, 200 ) );
        panelTunnels.setMaximumSize( new Dimension( Integer.MAX_VALUE, 200 ) );
        
        JPanel panelToolbars = new JPanel();
        panelToolbars.setLayout( new BoxLayout( panelToolbars, BoxLayout.X_AXIS ) );
        
        JPanel panelToolbarConfig = new JPanel();
        panelToolbarConfig.setLayout( new BoxLayout( panelToolbarConfig, BoxLayout.PAGE_AXIS ) );
        
        JPanel panelToolbarProfile = new JPanel();
        panelToolbarProfile.setLayout( new BoxLayout( panelToolbarProfile, BoxLayout.Y_AXIS ) );
        
        JPanel panelToolbarTunnel = new JPanel();
        panelToolbarTunnel.setLayout( new BoxLayout( panelToolbarTunnel, BoxLayout.Y_AXIS ) );
        
        JPanel panelToolbarMisc = new JPanel();
        panelToolbarMisc.setLayout( new BoxLayout( panelToolbarMisc, BoxLayout.Y_AXIS ) );
        
        JPanel panelToolbarNone = new JPanel();
        panelToolbarNone.setLayout( new BoxLayout( panelToolbarNone, BoxLayout.Y_AXIS ) );
        
        JLabel labelConfig = new JLabel( "Config" );
        labelConfig.setOpaque( true );
        labelConfig.setMaximumSize( new Dimension( toolbarConfig.getPreferredSize().width, labelConfig.getPreferredSize().height ) );
        labelConfig.setBackground( new Color( 147, 203, 255 ) );
        panelToolbarConfig.add( labelConfig );
        toolbarConfig.setAlignmentX( 0.0f );
        panelToolbarConfig.add( toolbarConfig );

        JLabel labelProfile = new JLabel( "Profile" );
        labelProfile.setOpaque( true );
        labelProfile.setMaximumSize( new Dimension( toolbarProfile.getPreferredSize().width, labelProfile.getPreferredSize().height ) );
        labelProfile.setBackground( new Color( 127, 183, 255 ) );
        panelToolbarProfile.add( labelProfile );
        toolbarProfile.setAlignmentX( 0.0f );
        panelToolbarProfile.add( toolbarProfile );
        
        JLabel labelTunnel = new JLabel( "Tunnel" );
        labelTunnel.setOpaque( true );
        labelTunnel.setMaximumSize( new Dimension( toolbarTunnel.getPreferredSize().width, labelTunnel.getPreferredSize().height ) );
        labelTunnel.setBackground( new Color( 107, 163, 255 ) );
        panelToolbarTunnel.add( labelTunnel );
        toolbarTunnel.setAlignmentX( 0.0f );
        panelToolbarTunnel.add( toolbarTunnel );
        
        JLabel labelMisc = new JLabel( "General" );
        labelMisc.setOpaque( true );
        labelMisc.setMaximumSize( new Dimension( toolbarMisc.getPreferredSize().width, labelMisc.getPreferredSize().height ) );
        labelMisc.setBackground( new Color( 87, 143, 255 ) );
        panelToolbarMisc.add( labelMisc );
        toolbarMisc.setAlignmentX( 0.0f );
        panelToolbarMisc.add( toolbarMisc );
        
        JLabel labelNone = new JLabel( " " );
        labelNone.setOpaque( true );
        labelNone.setMaximumSize( new Dimension( Integer.MAX_VALUE, labelNone.getPreferredSize().height ) );
        labelNone.setBackground( new Color( 67, 123, 255 ) );
        panelToolbarNone.add( labelNone );
        toolbarNone.setAlignmentX( 0.0f );
        panelToolbarNone.add( toolbarNone );
        toolbarNone.setPreferredSize( new Dimension( 200, toolbarNone.getPreferredSize().height ) );

        panelToolbars.add( panelToolbarConfig );
        panelToolbars.add( panelToolbarProfile );
        panelToolbars.add( panelToolbarTunnel );
//        panelToolbars.add( panelToolbarMisc );
        panelToolbars.add( panelToolbarNone );
        
        contentPane.add( panelToolbars );
        contentPane.add( Box.createVerticalStrut( 16 ) );
        contentPane.add( panelProfiles );
        contentPane.add( Box.createVerticalStrut( 16 ) );
        contentPane.add( panelTunnels );
        contentPane.add( Box.createVerticalStrut( 16 ) );
        contentPane.add( panelForwards );
        
    }
    
    private boolean checkDirty() {
        
        if ( configCurrent == null ) {
            return false;
        }
        
        boolean different = ! configCurrent.equals( configOriginal );
        boolean dirty     = ! configCurrent.isValid();
        
        if ( different ) {
            
            buttonSaveConfig.setEnabled( true );
            buttonRevertConfig.setEnabled( true );

        } else {
            
            buttonSaveConfig.setEnabled( false );
            buttonRevertConfig.setEnabled( false );
        }
        
        if ( dirty ) {
            buttonSaveConfig.setEnabled( false );
        }
        
        return different;
    }
    
    public void setConfig( Config config ) {
        
        this.configCurrent = config;
        this.configOriginal = new Config( this.configCurrent );
        
        buttonAddProfile.setEnabled( true );
        fieldPort.setText( config.getPort().toString() );

        
        comboProfiles.removeAllItems();
        for ( Profile profile : configCurrent.getProfiles() ) {
            comboProfiles.addItem( profile );
        }
        
        checkDirty();
    }
    
}

class ProfileRenderer extends JLabel implements ListCellRenderer<Profile> {

    static final long serialVersionUID = 42;
    
    public ProfileRenderer() {
        super();
        setOpaque( true );
    }
    
    public Component getListCellRendererComponent( JList<? extends Profile> list, Profile value, int index, boolean isSelected, boolean cellHasFocus) {
                
        if ( value != null ) {
            setText( value.getName() );
        } else {
            setText( "" );
        }
        
        if ( isSelected ) {
            setBackground( Color.GREEN );
        } else {
            setBackground( Color.WHITE );
        }

        return this;
    }
}

class AddRenderer extends JButton implements TableCellRenderer, MouseListener {
    
    static final long serialVersionUID = 42;
    
    private JTableHeader header;
    private int column;
    
    public AddRenderer( JTableHeader header, int column ) {
        super();
        this.header = header;
        this.column = column;
        setEnabled( false );
        
        setIcon( Imx.PLUS );
        setMargin( new Insets( 0, 0, 0, 0 ) );
        
        header.addMouseListener( this );
    }
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int column = header.getColumnModel().getColumnIndexAtX( e.getX() );
        
        if ( column == this.column ) {
            getModel().setPressed( true );
            getModel().setArmed( true );
            header.repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {

        int column = header.getColumnModel().getColumnIndexAtX( e.getX() );
        
        if ( column != this.column ) {
            getModel().setArmed( false );
        }

        getModel().setPressed( false );
        header.repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        getModel().setArmed( false ); 
        getModel().setPressed( false );
        header.repaint();        
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        return this;
    }
}

class FileRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
    
    static final long serialVersionUID = 42;

    private JToolBar toolbar;
    private File value;
    private JButton buttonClear;
    private JButton buttonSelect;
    private JLabel label;
    
    public FileRenderer() {
        
        buttonSelect = new JButton( Imx.LOAD.s12 );
        buttonSelect.setRequestFocusEnabled( false );;
        buttonSelect.setHorizontalAlignment( SwingConstants.LEFT );
        buttonSelect.setHorizontalTextPosition( SwingConstants.RIGHT );
        buttonSelect.addActionListener( this );
        buttonSelect.setActionCommand( "edit" );

        buttonClear = new JButton( Imx.DELETE.s12 );
        buttonClear.setRequestFocusEnabled( false );
        buttonClear.addActionListener( this );
        buttonClear.setActionCommand( "clear" );
        
        label = new JLabel("...");
        Dimension max = new Dimension( 90, label.getPreferredSize().height );
        label.setMinimumSize( max );
        label.setMaximumSize( max );
        label.setPreferredSize( max );
        label.setFont( new Font( label.getFont().getName(), Font.PLAIN, label.getFont().getSize() - 2 ));
        
        
        toolbar = new JToolBar();
        toolbar.setRequestFocusEnabled( false );
        toolbar.setFloatable( false );
        toolbar.setOpaque( true );;
        toolbar.setRollover( false );
        toolbar.add( buttonSelect );
        toolbar.add( label );
        toolbar.add( Box.createHorizontalGlue() );
        toolbar.add( buttonClear );
    }
    
    @Override
    public boolean isCellEditable(EventObject e) {
        
        if ( e instanceof MouseEvent ) {
            
            if ( ((MouseEvent)e).getClickCount() == 1 ) {
                return true;
            }
        }
        
        return false;
    }

    public void actionPerformed(ActionEvent event ) {
        
        if ( event.getActionCommand().equals( "edit" ) ) {

            JFileChooser chooser = new JFileChooser();
            
            chooser.setFileHidingEnabled( false );
            
            if ( value == null ) {
            
                File home = new File( System.getProperty( "user.home" ) + "/.ssh" );
                if ( home.isDirectory() ) {
                    chooser.setCurrentDirectory( home );
                }
            } else {
                
                chooser.setSelectedFile( value );
            }
            
            int answer = chooser.showOpenDialog( null );
            
            if ( answer == JFileChooser.APPROVE_OPTION ) {
                
                value = chooser.getSelectedFile();
                
                KeyPair keypair = null;
                try {
                    keypair = KeyPair.load( new JSch(), value.getAbsolutePath() );
                } catch ( JSchException e ) {
                    
                    value = null;
                    JOptionPane.showMessageDialog( null, "Aww, come on. That's not even a valid private key." );
                }
                
                if ( keypair != null && keypair.isEncrypted() ) {
                    
                    JOptionPane.showMessageDialog( null, "Uh oh, this key is encrypted.\nI could forward the passphrase to the tunnel manager, but I would do so unencrypted.\nSo either use an unprotected key or rely on the SSH agent, which I also support." );
                }
            }

            stopCellEditing();
        }
        
        if ( event.getActionCommand().equals( "clear" ) ) {
            
            value = null;

            stopCellEditing();
        }
        
    }
    
    
    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public Object getCellEditorValue() {
        return value;
    }
    

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        File file = (File)value;
        
        if ( file != null && file.getName().length() > 0 ) {
            label.setForeground( Color.BLACK );
            label.setText( file.getName() );
        } else {
            label.setForeground( Color.GRAY );
            label.setText( "SSH AGENT" );
        }
        
        return toolbar;
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        
        File file = (File)value;
        
        if ( file != null && file.getName().length() > 0  ) {
            label.setForeground( Color.BLACK );
            label.setText( file.getName() );
        } else {
            label.setForeground( Color.GRAY );
            label.setText( "SSH AGENT" );
        }

        this.value = file;
        
        return toolbar;
    }
}


class DeleteButtonRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
    
    static final long serialVersionUID = 42;

    private JButton button;
    private ActionListener listener;
    private Object target;
    
    public DeleteButtonRenderer( ActionListener listener ) {
        
        this.listener = listener;
        
        button = new JButton( Imx.DELETE );
        button.setRequestFocusEnabled( false );;
        button.addActionListener( this );
    }
    
    @Override
    public boolean isCellEditable(EventObject e) {
        
        if ( e instanceof MouseEvent ) {

            if ( ((MouseEvent)e).getClickCount() == 1 ) {
                return true;
            }
        }
        
        return false;
    }

    public void actionPerformed( ActionEvent event ) {

        ActionEvent deleteEvent = new ActionEvent( target, event.getID(), "delete" );

        stopCellEditing();
        
        listener.actionPerformed( deleteEvent );
    }
    
    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        return button;
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        
        target = ((ObjectTableModel<?>)table.getModel()).get( row );
        
        return button;
    }
}


class ActiveRenderer implements TableCellRenderer {
    
    static final long serialVersionUID = 42;

    private JLabel label;
    
    public ActiveRenderer() {
        label = new JLabel( Imx.INACTIVE );
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        Boolean active = (Boolean)value;
        
        if ( active ) {
            label.setIcon( Imx.ACTIVE );
        } else {
            label.setIcon( Imx.INACTIVE );
        }
        
        return label;
    }
}

class ForwardTypeRenderer implements TableCellRenderer {
    
    static final long serialVersionUID = 42;

    private JLabel label;
    
    public ForwardTypeRenderer() {
        label = new JLabel( Imx.LOCAL );
        label.setBorder( new EmptyBorder( new Insets( 0, 8, 0, 8 ) ) );
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        String type = (String)value;

        label.setText( type );
        
        if ( type.equals( Forward.LOCAL ) ) {
            label.setHorizontalAlignment( JLabel.LEADING );
            label.setIcon( Imx.LOCAL );
        } else {
            label.setHorizontalAlignment( JLabel.TRAILING );
            label.setIcon( Imx.REMOTE );
        }
        
        return label;
    }
}
 




