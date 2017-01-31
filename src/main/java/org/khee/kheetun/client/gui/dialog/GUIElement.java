package org.khee.kheetun.client.gui.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.Kholor;

@SuppressWarnings("serial")
public abstract class GUIElement extends JPanel implements MouseListener, KeyListener {
    
    private ConfigPanelRow<?>       row;
    protected Object                value;
    protected Class<?>              valueclass;
    private Field                   field;
    private Method                  getter;
    private Method                  setter;
    protected boolean               editing         = false;
    protected JLabel                labelIcon;
    protected JLabel                labelHint       = null;
    protected JPanel                body            = new JPanel();
    protected Imx                   icon;
    private String                  hint            = null;
    private ArrayList<GUIElementListener> listeners = new ArrayList<GUIElementListener>();
    
    public GUIElement( ConfigPanelRow<?> row, Class<?> valueClass, Field field ) {
        
        this.setName( row.getObject().getClass() + ":" + field.getName() );
        
        this.row            = row;
        this.valueclass     = valueClass;
        this.field          = field;
        
        Method getter = null;
        Method setter = null;
        
        try {

            getter = field.getDeclaringClass().getMethod( "get" + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 ) );
            
        } catch ( NoSuchMethodException e ) {
            
            try {
            
                getter = field.getDeclaringClass().getMethod( "is" + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 ) );
                
            } catch ( NoSuchMethodException eNoSuchMethod ) {
                
                eNoSuchMethod.printStackTrace();
            }
        }
        
        try {
            setter = field.getDeclaringClass().getMethod( "set" + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 ), field.getType() );
            
        } catch ( NoSuchMethodException eNoSuchMethod ) {
            
            eNoSuchMethod.printStackTrace();
        }
        
        this.getter         = getter;
        this.setter         = setter;
        
        this.setOpaque( false );
        this.setLayout( new GridBagLayout() );
        this.setAlignmentX( JComponent.LEFT_ALIGNMENT );
        
        this.icon = GUI.FIELD.get( field ).icon;
        
        if ( row.getId() % 2 == 0 ) {
            icon = icon.lighten( 32 );
        }
        
        this.labelIcon = new JLabel( this.icon );
        this.labelIcon.setOpaque( false );
        this.labelIcon.setBackground( new Color( 200, 255, 200 ) );
        this.labelIcon.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        
        this.hint = GUI.FIELD.get( field ).hint;
        if ( this.hint == null ) {
            this.hint = field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 );
        }
        
        this.labelHint = new JLabel( this.hint );
        this.labelHint.setFont( new Font( Font.DIALOG, Font.PLAIN, 10 ) );
        this.labelHint.setForeground( Kholor.DIALOG_HINT );
        this.labelHint.setAlignmentX( JLabel.LEFT_ALIGNMENT );
        this.labelHint.setHorizontalTextPosition( JLabel.LEFT );
        
        this.body.setOpaque( false );
        this.body.setLayout( new BoxLayout( this.body, BoxLayout.X_AXIS ) );
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx         = 1;
        c.gridy         = 0;
        c.weightx       = 1.0;
        c.weighty       = 1.0;
        c.anchor        = GridBagConstraints.BASELINE_LEADING;
       this.add( this.labelHint, c );
        
        c.gridx         = 1;
        c.gridy         = 1;
        c.gridwidth     = 1;
        c.fill          = GridBagConstraints.BOTH;
        this.add( this.body, c );

        c.gridx         = 0;
        c.gridy         = 1;
        c.weightx       = 0.0;
        c.weighty       = 0.0;
        this.add( this.labelIcon, c );
                 
        this.addGuiElementListener( Dialog.getInstance() );
    }
    
    public Object getValue() {
        
        try {
            
            return this.valueclass.cast( this.getter.invoke( row.getObject().getClass().cast( row.getObject() ) ) );  
                    
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void setValue( Object value ) {
        
        try {
            
            this.valueclass.cast( this.setter.invoke( row.getObject().getClass().cast( row.getObject() ), this.valueclass.cast( value ) ) );
            
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    
    public void updateValue( Object value ) {
        
        value = this.processUpdate( value );

        this.setValue( value );
        
        Dialog.getInstance().getConfig().validate();

        if ( row.getObject().getError( this.field ) != null ) {
            
            this.labelIcon.setIcon( this.icon.color( Kholor.ERROR ) );
        } else {
            
            this.labelIcon.setIcon( this.icon );
        }
        
    }
    
    protected abstract Object processUpdate( Object value );
    

    public void hover() {
        
        this.labelHint.setForeground( Kholor.DIALOG_HINT_HOVER );
        
        if ( this.row.getObject().getError( this.field ) != null ) {
            
            Dialog.getInstance().showVerify( this.getLocationOnScreen(), Imx.WARNING.color( Kholor.ERROR ), row.getObject().getError( this.field ) );
        }     
    }
    
    public void unhover() {
        
        this.labelHint.setForeground( Kholor.DIALOG_HINT );

        Dialog.getInstance().hideVerify();
    }
    
    public void beginEdit() {
        
        for ( GUIElementListener listener : this.listeners ) {
            
            listener.guiElementBeginEdit( this );
        }
        
        this.editing = true;
        this.processBeginEdit();
    }
    
    protected abstract void processBeginEdit();
    
    public void endEdit( boolean confirm ) {
        
        if ( ! this.editing ) {
            return;
        }
        
        this.editing = false;
        this.processEndEdit( confirm );
        
        for ( GUIElementListener listener : this.listeners ) {
            
            listener.guiElementEndEdit( this, confirm );
        }
    }
    
    protected abstract void processEndEdit( boolean confirm );
    
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        
        if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            
            this.endEdit( false );
        }
        
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            
            this.endEdit( true );
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void addGuiElementListener( GUIElementListener listener ) {
        
        this.listeners.add( listener );
    }
}
