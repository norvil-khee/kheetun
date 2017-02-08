package org.khee.kheetun.client.gui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.khee.kheetun.client.config.Base;
import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.Khbutton;
import org.khee.kheetun.client.gui.Kholor;
import org.khee.kheetun.client.gui.TextStyle;

@SuppressWarnings("serial")
class ConfigPanel<T extends Base> extends JPanel {
    
    private JPanel                              body;
    private ArrayList<T>                        objects          = null;
    private HashMap<Integer, ConfigPanelRow<T>> rowsCache        = new HashMap<Integer, ConfigPanelRow<T>>();
    private JLabel                              labelNoItems     = new JLabel();
    
    public ConfigPanel( Dialog dialog, String header, Imx icon, TextStyle textStyle ) {
        
        this.setLayout( new GridBagLayout() );
        
        JLabel labelHeader = new JLabel( header, JLabel.LEFT );
        labelHeader.setForeground( textStyle.getColorActive() );
        labelHeader.setFont( textStyle.getFontActive() );
        
        Khbutton buttonAdd = new Khbutton( "ADD", Imx.PLUS );
        buttonAdd.setWidth( 60 );
        buttonAdd.addButtonListener( dialog );
        buttonAdd.setId( header + ":ADD" );

        Khbutton buttonCopy = new Khbutton( "COPY", Imx.COPY );
        buttonCopy.setWidth( 60 );
        buttonCopy.addButtonListener( dialog );
        buttonCopy.setId( header + ":COPY" );
        
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout( new BoxLayout( panelButtons, BoxLayout.X_AXIS ) );
        panelButtons.setAlignmentX( JComponent.LEFT_ALIGNMENT );
        
        panelButtons.add( labelHeader );
        panelButtons.add( Box.createHorizontalGlue() );
        panelButtons.add( buttonCopy );
        panelButtons.add( Box.createHorizontalStrut( 8 ) );
        panelButtons.add( buttonAdd );
        
        this.labelNoItems.setText( "<html><body>No " + header + " here.<br>Click ADD to add one.</body></html>" );
        this.labelNoItems.setFont( new Font( Font.DIALOG, Font.PLAIN, 24 ) );
        this.labelNoItems.setForeground( Kholor.DECENT_GREY );
        
        int scrollBarSize = ((Integer)UIManager.get("ScrollBar.width")).intValue();
        
        panelButtons.add( Box.createRigidArea( new Dimension( scrollBarSize, 1 ) ) );

        this.body = new JPanel(){
            
            @Override
            public void paintComponent(Graphics g) {
                
                Graphics2D g2d = (Graphics2D)g;
                
                g2d.setPaint( new GradientPaint( 0.0f, this.getHeight() - this.getHeight() / 2, Color.WHITE, 0.0f, this.getHeight(), Color.LIGHT_GRAY ) );
                g2d.fillRect( 0, 0, this.getWidth(), this.getHeight() );
            }
        };
        
        this.body.setLayout( new GridBagLayout() );
        this.body.setAlignmentX( JComponent.LEFT_ALIGNMENT );
        
        JScrollPane scrollPane = new JScrollPane( this.body, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scrollPane.setAlignmentX( JComponent.LEFT_ALIGNMENT );
        scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.weighty = 0.0f;
        c.weightx = 1.0f;
        c.gridy   = 0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        this.add( panelButtons, c );

        c.weighty = 1.0f;
        c.gridy   = 1;
        c.fill    = GridBagConstraints.BOTH;
        this.add( scrollPane, c );
    }
    
    public void setObjects( ArrayList<T> objects ) {
        
        this.objects = objects;
        
        this.refreshBody();
    }
    
    public ConfigPanelRow<? extends Base> getRowByObject( Base object ) {
        
        return this.rowsCache.get( object.getId() );
    }
    
    public HashMap<Integer, ConfigPanelRow<T>> getRowsCache() {
        
        return this.rowsCache;
    }
    
    public void cacheRowsByObjects( ArrayList<T> objects ) {
        
        for ( T object : objects ) {
            
            this.rowsCache.put( object.getId(), new ConfigPanelRow<T>( this, object ) );
        }
    }
    
    public void refreshBody() {
        
        ConfigPanelRow.counter = 0;
        
        this.body.setVisible( false );
        this.body.removeAll();
        
        if ( this.objects != null ) {
            
            GridBagConstraints c = new GridBagConstraints();
            
            c.gridy     = 0;
            c.weightx   = 1.0;
            c.weighty   = 0.0;
            c.anchor    = GridBagConstraints.PAGE_START;
            c.fill      = GridBagConstraints.HORIZONTAL;
            
            for ( T object : this.objects ) {
                
                if ( ! this.rowsCache.containsKey( object.getId() ) ) {
                    
                    this.rowsCache.put( object.getId(), new ConfigPanelRow<T>( this, object ) );
                }
                
                ConfigPanelRow<T> row = this.rowsCache.get( object.getId() );
                
                this.body.add( row, c );
                
                c.gridy++;
            }

            if ( this.body.getComponents().length == 0 ) {
                
                this.body.add( this.labelNoItems, c );
                c.gridy++;
            }
            
            c.fill      = GridBagConstraints.BOTH;
            c.weighty   = 1.0;
            
            this.body.add( Box.createVerticalBox(), c );
        }
        
        
        this.body.setVisible( true );
        this.body.revalidate();
    }
    
}