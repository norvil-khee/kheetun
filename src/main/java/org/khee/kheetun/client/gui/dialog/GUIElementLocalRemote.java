package org.khee.kheetun.client.gui.dialog;

import java.awt.Cursor;
import java.awt.Font;
import java.lang.reflect.Field;

import javax.swing.JLabel;

import org.khee.kheetun.client.config.Forward;

@SuppressWarnings("serial")
public class GUIElementLocalRemote extends GUIElement {
    
    private JLabel      labelText       = new JLabel( Forward.LOCAL );
    
    public GUIElementLocalRemote( ConfigPanelRow<?> row, Class<?> valueClass, Field field ) {
        
        super( row, valueClass, field );
        
        this.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
        
        this.labelText.setFont( new Font( Font.DIALOG, Font.BOLD, 12 ) );
        
        this.updateValue( (String)this.getValue() );
        
        this.body.add( this.labelText );
    }
    
    @Override
    protected Object processUpdate(Object value) {

        String string = (String)value;
        
        if ( string.equals( Forward.LOCAL ) ) {
            this.labelText.setText( "LOCAL" );
        } else {
            this.labelText.setText( "REMOTE" );
        }
        
        return value;
    }
    
    @Override
    protected void processBeginEdit() {
        
        if ( ((String)this.getValue()).equals( Forward.LOCAL ) ) {
            
            this.updateValue( Forward.REMOTE );
        } else {
            
            this.updateValue( Forward.LOCAL );
        }
        
        this.endEdit( true );
    }
    
    @Override
    protected void processEndEdit(boolean confirm) {
        // TODO Auto-generated method stub
        
    }
}
