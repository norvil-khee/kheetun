package org.khee.kheetun.client.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.Khbutton;

@SuppressWarnings("serial")
public class GUIElementFileSelector extends GUIElement implements ActionListener {
    
    private JLabel          textLabel   = new JLabel();
    private JFileChooser    fileChooser = new JFileChooser();
    
    public GUIElementFileSelector( ConfigPanelRow<?> row, Class<?> valueClass, Field field ) {
        
        super( row, valueClass, field );
        
        this.body.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
        
        this.textLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
        this.textLabel.setHorizontalAlignment( JLabel.LEFT );
        this.textLabel.setAlignmentY( JComponent.CENTER_ALIGNMENT );
        this.textLabel.setFont( GUI.FIELD.get( field ).font );
        
        this.updateValue( (File)this.getValue() );
        
        this.fileChooser.addActionListener( this );
        
        Khbutton buttonDelete = new Khbutton( "", Imx.CROSS.size( 8 ) ) {
            
            @Override
            public void click() {
                
                GUIElementFileSelector.this.editing = true;
                GUIElementFileSelector.this.fileChooser.setSelectedFile( null );
                GUIElementFileSelector.this.endEdit( true );
            };
        };
        
        
        this.body.add( buttonDelete );
        this.body.add( Box.createHorizontalStrut( 4 ) );
        this.body.add( this.textLabel );
    }
    
    @Override
    protected Object processUpdate( Object value ) {

        File file = (File)value;

        if ( file != null ) {
            this.textLabel.setText( file.getName() );
            this.textLabel.setForeground( Color.BLACK );
        } else {
            this.textLabel.setText( "SSH Agent" );
            this.textLabel.setForeground( Color.LIGHT_GRAY );
        }
        
        return value;
    }
    
    @Override
    protected void processBeginEdit() {
        
        if ( this.getValue() != null ) {
            this.fileChooser.setCurrentDirectory( (File)this.getValue() );
        }
        this.fileChooser.showOpenDialog( this );
    }
    
    @Override
    protected void processEndEdit( boolean confirm ) {
        
        if ( confirm ) {
            this.updateValue( this.fileChooser.getSelectedFile() );
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if ( e.getSource() instanceof JFileChooser ) {
            
            if ( e.getActionCommand().equals( JFileChooser.APPROVE_SELECTION ) ) {
                System.out.println( "Confirm file" );
                
                this.endEdit( true );
            } else {

                System.out.println( "Undo file" );
                this.endEdit( false );
            }
        }
    }    
}
