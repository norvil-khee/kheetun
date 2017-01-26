package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.khee.kheetun.client.config.Config;
import org.khee.kheetun.client.config.Forward;
import org.khee.kheetun.client.config.Profile;
import org.khee.kheetun.client.config.Tunnel;
import org.khee.kheetun.client.gui.dialog.GUI;

@SuppressWarnings("serial")
public class ErrorMatrix extends JPanel {
    
    public static final int ANCHOR_TOP_LEFT     = 0;
    public static final int ANCHOR_TOP_RIGHT    = 1;
    private int             row                 = 0;
    private JLabel          labelNoErrors       = new JLabel( "No errors, life's good." );
    
    class ErrorMatrixLabel extends JLabel {
        
        public ErrorMatrixLabel( String text, Imx icon, int gap ) {
            
            this.initComponents( text, icon, gap );
        }

        public ErrorMatrixLabel( String text, Imx icon ) {
            
            this.initComponents( text, icon, 0 );
        }
        
        private void initComponents( String text, Imx icon, int gap ) {
            
            this.setText( text );
            this.setIcon( icon );
            this.setIconTextGap( 4 );
            this.setBorder( BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder( 0, 0, 1, 1, new Color( 240, 240, 240  ) ), 
                    BorderFactory.createEmptyBorder( 4, 4 + gap, 4, 4 )
                ) );
            this.setOpaque( true );
            this.setBackground( ErrorMatrix.this.row % 2 == 0 ? Kholor.EVEN_ROW : Kholor.ODD_ROW );
            this.setForeground( Color.BLACK );
        }
    };
    
    public ErrorMatrix() {
        
        this.setLayout( new GridBagLayout() );
        this.setVisible( false );
        
        this.labelNoErrors.setFont( new Font( Font.DIALOG, Font.PLAIN, 24 ) );
        this.labelNoErrors.setForeground( Kholor.DECENT_GREY );
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setPaint( new GradientPaint( 0.0f, this.getHeight() - this.getHeight() / 2, Color.WHITE, 0.0f, this.getHeight(), Color.LIGHT_GRAY ) );
        g2d.fillRect( 0, 0, this.getWidth(), this.getHeight() );
    }
    
    public void showErrors( Config config ) {
        
        this.row = 0;
        
        this.setVisible( false );
        
        this.removeAll();
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.weightx = 1.0f;
        c.weighty = 0.0f;
        c.gridy   = 0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        
        int countProfiles = 0;
        
        c.gridx  = 0;
        c.anchor = GridBagConstraints.PAGE_START;
        
        for ( Profile profile : config.getProfiles() ) {
            
            countProfiles++;
            
            String profileName = "Profile " + ( profile.getName() == null || profile.getName().length() == 0 ? "[noname:" + countProfiles + "]" : profile.getName() );
            
            for ( Field field : profile.getErrors().keySet() ) {
            
                this.row++;
                
                this.add( new ErrorMatrixLabel( profileName, Imx.WARNING.color( Kholor.ERROR ) ), c );
                
                c.gridx = 1;
                this.add( new ErrorMatrixLabel( GUI.FIELD.get( field ).hint, GUI.FIELD.get( field ).icon ), c );
                
                c.gridx = 2;
                this.add( new ErrorMatrixLabel( profile.getErrors().get( field ), Imx.NONE ), c );

                c.gridx = 0;
                c.gridy++;
            }
            
            int countTunnels = 0;
            
            for ( Tunnel tunnel : profile.getTunnels() ) {
                
                countTunnels++;
                
                String tunnelName = "Tunnel " + ( tunnel.getAlias() == null || tunnel.getAlias().length() == 0 ? "[noname:" + countTunnels + "]" : tunnel.getAlias() );
                
                if ( profile.getErrors().size() == 0 && tunnel.getErrors().size() > 0 ) {
                    
                    this.row++;
                    
                    this.add( new ErrorMatrixLabel( profileName, Imx.WARNING.color( Kholor.ERROR ) ), c );
                    
                    c.gridy++;
                }
                
                for ( Field field : tunnel.getErrors().keySet() ) {

                    this.row++;
                    
                    this.add( new ErrorMatrixLabel( tunnelName, Imx.WARNING.color( Kholor.ERROR ), 16 ), c );
                    
                    c.gridx = 1;
                    this.add( new ErrorMatrixLabel( GUI.FIELD.get( field ).hint, GUI.FIELD.get( field ).icon ), c );
                    
                    c.gridx = 2;
                    this.add( new ErrorMatrixLabel( tunnel.getErrors().get( field ), Imx.NONE ), c );

                    c.gridx = 0;
                    c.gridy++;
                }
                
                int countForwards = 0;
                
                for ( Forward forward : tunnel.getForwards() ) {
                    
                    countForwards++;
                    
                    String forwardName = "Forward " + countForwards;
                    
                    if ( profile.getErrors().size() == 0 && tunnel.getErrors().size() == 0 && forward.getErrors().size() > 0 ) {
                        
                        this.row++;
                        
                        this.add( new ErrorMatrixLabel( profileName, Imx.WARNING.color( Kholor.ERROR ) ), c );
                        
                        c.gridy++;
                    }
                    
                    if ( tunnel.getErrors().size() == 0 && forward.getErrors().size() > 0 ) {
                        
                        this.row++;
                        
                        this.add( new ErrorMatrixLabel( tunnelName, Imx.WARNING.color( Kholor.ERROR ), 16 ), c );
                        
                        c.gridy++;
                    }
                    
                    for ( Field field : forward.getErrors().keySet() ) {

                        this.row++;
                        
                        this.add( new ErrorMatrixLabel( forwardName, Imx.WARNING.color( Kholor.ERROR ), 32 ), c );
                        
                        c.gridx = 1;
                        this.add( new ErrorMatrixLabel( GUI.FIELD.get( field ).hint, GUI.FIELD.get( field ).icon ), c );
                        
                        c.gridx = 2;
                        this.add( new ErrorMatrixLabel( forward.getErrors().get( field ), Imx.NONE ), c );

                        c.gridx = 0;
                        c.gridy++;
                    }
                }
            }
        }
        
        if ( row == 0 ) {
            
            this.add( this.labelNoErrors, c );
        }
        
        c.gridy++;
        c.weighty = 1.0f;
        c.gridx = GridBagConstraints.REMAINDER;
        
        this.add( Box.createVerticalGlue(), c );
        
//        this.revalidate();
        
//        if ( anchor == ErrorMatrix.ANCHOR_TOP_LEFT ) {
//            
//            this.setLocation( p.x, p.y );
//            
//        } else if ( anchor == ErrorMatrix.ANCHOR_TOP_RIGHT ) {
//            
//            this.setLocation( p.x - this.getWidth(), p.y );
//        }
        
        this.setVisible( true );
    }

}
