package org.khee.kheetun.client.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import org.khee.kheetun.client.config.Base;
import org.khee.kheetun.client.gui.Imx;
import org.khee.kheetun.client.gui.Khbutton;
import org.khee.kheetun.client.gui.Kholor;

@SuppressWarnings("serial")
class ConfigPanelRow<C extends Base> extends JPanel implements MouseListener, MouseMotionListener, ComponentListener {
    
    public static int                       counter         = 0;

    private C                               object;
    private ConfigPanel<C>                  panel;
    private int                             id              = ConfigPanelRow.counter++;
    private boolean                         selected        = false;
    private boolean                         hovered         = false;

    private GUIElement                      hoveredElement  = null;
    private ArrayList<GUIElement>           elements        = new ArrayList<GUIElement>();

    private ArrayList<SelectionListener>    listeners = new ArrayList<SelectionListener>();
    
    private BufferedImage                   imageBasic;
    private BufferedImage                   imageHovered;
    private BufferedImage                   imageSelected;
    private BufferedImage                   imageBackground;
    
    public ConfigPanelRow( ConfigPanel<C> panel, C object ) {
        
        this.panel  = panel;
        this.object = object;
        
        this.setOpaque( false );
        this.setDoubleBuffered( true );
        this.setAlignmentX( Component.LEFT_ALIGNMENT );
        
        this.setLayout( new GridBagLayout() );
        
        this.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.LIGHT_GRAY ) );

        this.addMouseListener( this );
        this.addMouseMotionListener( this );
        this.addComponentListener( this );
        this.addConfigPanelRowListener( Dialog.getInstance() );
        
        for ( Field field : object.getClass().getDeclaredFields() ) {
            
            if ( GUI.FIELD.containsKey( field ) ) {
                
                GUI gui = GUI.FIELD.get( field );
                
                try {
                    
                    int width       = gui.width;
                    int gridx       = gui.gridx;
                    int gridy       = gui.gridy;
                    int gridwidth   = gui.gridwidth;
                    int gridheight  = gui.gridheight;
                    int anchor      = gui.anchor;
                    int fill        = gui.fill;
                    
                    GUIElement guiElement = gui.component.getDeclaredConstructor( ConfigPanelRow.class, Class.class, Field.class ).newInstance( this, field.getType(), field );
                    
                    guiElement.setPreferredSize( new Dimension( width, 36 ) );
                    guiElement.setMinimumSize( new Dimension( width, 36 ) );
                    guiElement.setMaximumSize( new Dimension( width, 36 ) );
                    guiElement.setVisible( ! gui.hidden );
                    
                    Insets i = new Insets( 0, 0, 0, 0 );
                    
                    GridBagConstraints c = new GridBagConstraints( gridx, gridy, gridwidth, gridheight, 0.0, 1.0, anchor, fill, i, 0, 0 );
                    this.add( guiElement, c );
                    
                    this.elements.add( guiElement );
                    
                } catch ( Exception e ) {
                    
                    e.printStackTrace();
                }
            }
        }
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridheight    = GridBagConstraints.REMAINDER;
        
        Khbutton buttonDelete = new Khbutton( "", Imx.CROSS.color( Color.DARK_GRAY ) );
        buttonDelete.setStyle( new Color( 0, true ), Color.BLACK, null, null );
        buttonDelete.setId( "DELETE" );
        buttonDelete.setObject( this.object );
        buttonDelete.addButtonListener( Dialog.getInstance() );

        c.fill          = GridBagConstraints.HORIZONTAL;
        c.weightx       = 1.0f;
        
        this.add( Box.createHorizontalGlue(), c );
        
        c.weightx       = 0.0f;
        c.anchor        = GridBagConstraints.FIRST_LINE_END;
        this.add( buttonDelete, c );
    }
    
    private void createBackgroundImages() {
        
        Color color = this.id % 2 == 0 ? Kholor.EVEN_ROW : Kholor.ODD_ROW;
        
        this.imageHovered = new BufferedImage( this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB );
        
        Graphics2D g2dHovered = (Graphics2D)this.imageHovered.getGraphics();
        
        g2dHovered.setPaint( new GradientPaint( 0.0f, 0.0f, Kholor.HOVER, 200.0f, 0.0f, color ) );
        g2dHovered.fillRect( 0, 0, this.getWidth(), this.getHeight() );
        
        
        this.imageSelected = new BufferedImage( this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB );
        
        Graphics2D g2dSelected = (Graphics2D)this.imageSelected.getGraphics();

        g2dSelected.setPaint( new GradientPaint( 0.0f, 0.0f, Kholor.SELECTED, 200.0f, 0.0f, color ) );
        g2dSelected.fillRect( 0, 0, this.getWidth(), this.getHeight() );

        
        this.imageBasic = new BufferedImage( this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB );
        
        Graphics2D g2dBasic = (Graphics2D)this.imageBasic.getGraphics();

        g2dBasic.setPaint( color );
        g2dBasic.fillRect( 0, 0, this.getWidth(), this.getHeight() );
        
        this.imageBackground = this.hovered ? this.imageHovered : this.selected ? this.imageSelected : this.imageBasic;
        
        this.repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        
        g.drawImage( this.imageBackground, 0, 0, null );
    }
    
    public int getId() {
        
        return this.id;
    }
    
    public C getObject() {
        
        return this.object;
    }
    
    public ConfigPanel<C> getPanel() {
        
        return this.panel;
    }
    
    public void hover() {
        
        this.hovered = true;
        this.imageBackground = Selection.getInstance().isSelected( this.object ) ? this.imageSelected : this.imageHovered;
        this.repaint();
    }
    
    public void unhover() {
        
        this.hovered = false;
        this.imageBackground = Selection.getInstance().isSelected( this.object ) ? this.imageSelected : this.imageBasic;
        this.repaint();
    }
    
    public void select() {

        this.selected = true;
        this.imageBackground = this.imageSelected;
        this.setGuiElementsVisible( true );
        this.repaint();
    }
    
    public void unselect() {
        
        this.selected = false;
        this.imageBackground = this.imageBasic;
        this.setGuiElementsVisible( false );
        this.repaint();
    }
    
    private void setGuiElementsVisible( boolean visible ) {
        
        for ( GUIElement element : this.elements ) {
            
            if ( visible ) {
                element.select();
            } else {
                element.unselect();
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        
        Dialog.getInstance().endEdit();
        
        Selection.getInstance().setSelected( this.object.getClass(), this );
        
        Component c = this.getComponentAt( e.getPoint() );
        
        if ( e.getClickCount() == 2 && c instanceof GUIElement ) {
            
            GUIElement guiElement = (GUIElement)c;
            
            guiElement.beginEdit();
            
            return;
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        
        this.hover();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {

        Component c = this.getComponentAt( e.getPoint() );
        
        if ( c instanceof GUIElement && c != this.hoveredElement ) {
            
            if ( this.hoveredElement != null ) {
                
                this.hoveredElement.unhover();
            }
            
            this.hoveredElement = (GUIElement)c;
            
            this.hoveredElement.hover();
        }
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        
        this.unhover();

        if ( this.hoveredElement != null ) {
            
            this.hoveredElement.unhover();
            
            this.hoveredElement = null;
        }

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
    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
        
        this.createBackgroundImages();
    }
    
    @Override
    public void componentShown(ComponentEvent e) {
        
        this.createBackgroundImages();
    }
    
    public void addConfigPanelRowListener( SelectionListener listener ) {
        
        this.listeners.add( listener );
    }
};
