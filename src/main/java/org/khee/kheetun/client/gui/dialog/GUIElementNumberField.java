package org.khee.kheetun.client.gui.dialog;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GUIElementNumberField extends GUIElement {
    
    private JTextField      textField   = new JTextField();
    private JLabel          textLabel   = new JLabel();
    
    public GUIElementNumberField( ConfigPanelRow<?> row, Class<?> valueClass, Field field ) {
        
        super( row, valueClass, field );
        
        this.body.setCursor( new Cursor( Cursor.TEXT_CURSOR ) );

        this.textField.setAlignmentX( Component.LEFT_ALIGNMENT );
        this.textField.setVisible( false );
        this.textField.setText( ((Integer)this.getValue()).toString() );
        this.textField.addKeyListener( this );
        
        this.textLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
        this.textLabel.setPreferredSize( this.textField.getPreferredSize() );
        this.textLabel.setMinimumSize( this.textField.getMinimumSize() );
        this.textLabel.setMaximumSize( this.textField.getMaximumSize() );
        this.textLabel.setFont( new Font( Font.DIALOG, Font.PLAIN, 12 ) );
        
        this.updateValue( (Integer)this.getValue() );

        this.body.add( this.textField );
        this.body.add( this.textLabel );
    }
    
    @Override
    protected Object processUpdate( Object value ) {
        
        this.textField.setText( ((Integer)value).toString() );
        this.textLabel.setText( ((Integer)value).toString() );
        
        return value;
    }
    
    @Override
    protected void processBeginEdit() {
        
        this.textLabel.setVisible( false );
        this.textField.setText( ((Integer)this.getValue()).toString() );
        this.textField.setVisible( true );
        this.textField.requestFocus();
    }
    
    @Override
    protected void processEndEdit( boolean confirm ) {
        
        this.textLabel.setVisible( true );
        this.textField.setVisible( false );
        
        if ( confirm ) {
            this.updateValue( new Integer( this.textField.getText() ) );
        }
    }

}
