package org.khee.kheetun.client.gui.dialog;

import java.awt.Cursor;
import java.lang.reflect.Field;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.khee.kheetun.client.gui.Kholor;

@SuppressWarnings("serial")
public class GUIElementBoolean extends GUIElement {
    
    private JLabel  labelTrue;
    private JLabel  labelFalse;
    
    public GUIElementBoolean( ConfigPanelRow<?> row, Class<?> valueClass, Field field ) {
        
        super( row, valueClass, field );
        
        this.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
        
        this.labelTrue  = new JLabel( (String)(GUI.FIELD.get( field ).param1), (ImageIcon)(GUI.FIELD.get( field ).param2), JLabel.LEFT );
        this.labelTrue.setVisible( false );
        this.labelFalse = new JLabel( (String)(GUI.FIELD.get( field ).param3), (ImageIcon)(GUI.FIELD.get( field ).param4), JLabel.LEFT );
        this.labelFalse.setForeground( Kholor.DIALOG_BOOLEAN_FALSE );
        this.labelFalse.setVisible( false );
        
        this.body.add( this.labelTrue );
        this.body.add( this.labelFalse );
        
        this.updateValue( (Boolean)this.getValue() );
    }
    
    @Override
    protected Object processUpdate( Object value ) {

        Boolean bool = (Boolean)value;
        
        if ( bool == null ) {
            bool = false;
        }
        
        this.labelTrue.setVisible( bool );
        this.labelFalse.setVisible( ! bool );
        
        return value;
    }
    
    @Override
    protected void processBeginEdit() {
        
        if ( (Boolean)this.getValue() == null || ! (Boolean)this.getValue() ) {
            
            this.updateValue( (Boolean)true );
        } else {
            this.updateValue( (Boolean)false );
        }
        
        this.endEdit( true );
    }
    
    @Override
    protected void processEndEdit( boolean confirm ) {
    }

}
