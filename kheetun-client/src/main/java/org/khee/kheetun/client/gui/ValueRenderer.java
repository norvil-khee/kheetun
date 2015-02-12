package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import org.khee.kheetun.client.verify.Verifier;

public class ValueRenderer extends DefaultCellEditor implements TableCellRenderer, FocusListener {

    static final long serialVersionUID = 42;
    
    private JLabel          label;
    private JTextField      editor;
    private CompoundBorder  borderValid;
    private CompoundBorder  borderInvalid;
    private Verifier        verifier;
    private Class<?>        inputClass;
    
    public ValueRenderer( Class<?> inputClass, Verifier verifier, int align, Insets insets ) {
        
        super( new JTextField() );
        
        this.verifier   = verifier;
        this.inputClass = inputClass;
        
        editor = (JTextField)getComponent();
        editor.setOpaque( true );
        editor.setBorder( null );
        editor.setMargin( insets );
        editor.setHorizontalAlignment( align );
        editor.setFont( new Font( editor.getFont().getName(), Font.BOLD, editor.getFont().getSize() ));
        
        borderValid   = new CompoundBorder( null, new EmptyBorder( insets ) );
        borderInvalid = new CompoundBorder( new LineBorder( Color.RED ), new EmptyBorder( insets ) );
        
        editor.addFocusListener( this );
        editor.setBorder( borderValid );
        
        label  = new JLabel( "" );
        label.setBorder( new EmptyBorder( insets ) );
        label.setHorizontalAlignment( align );
        label.setOpaque( true );
        label.setFont( new Font( label.getFont().getName(), Font.PLAIN, label.getFont().getSize() ));
        
    }
    
    public void focusGained(FocusEvent e) {
        
        editor.setBorder( borderValid );
        editor.setSelectionStart( 0 );
        editor.setSelectionEnd( editor.getText().length() );
    }
    
    public void focusLost(FocusEvent e) {
    }
    
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        
        boolean isEditable = super.isCellEditable(anEvent);
        
        if ( isEditable ) {
            editor.setCaretPosition( editor.getText().length() );
        }
        
        return isEditable;
    }
    
    @Override
    public boolean stopCellEditing() {
        
        if ( verifier.verify( getCellEditorValue() ) ) {
            fireEditingStopped();
            return true;
        }
        
        editor.setBorder( borderInvalid );
        return false;
    }
    
    @Override
    public Object getCellEditorValue() {
        
        if ( inputClass == Integer.class ) {

            try {
                Integer value = Integer.parseInt( editor.getText() );
                return value;

            } catch ( NumberFormatException e ) {
                
                return -1;
            } 
            
        } else {
            
            return editor.getText();
        }
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if ( value != null ) {
            label.setText( value.toString() );
        }
        
        if ( isSelected ) {
            label.setBackground( table.getSelectionBackground() );
            label.setForeground( table.getSelectionForeground() );
        } else {
            label.setBackground( table.getBackground() );
            label.setForeground( table.getForeground() );
        }
        
        if ( inputClass == Integer.class ) {
            editor.setText( Integer.toString( (Integer)value ) );
        } else {
            editor.setText( (String)value );
        }
        
        if ( verifier.verify( getCellEditorValue() ) ) {
            label.setBorder( borderValid );
        } else {
            label.setBorder( borderInvalid );
        }
        
        return label;
    }
    

    
    
}
