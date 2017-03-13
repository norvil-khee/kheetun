package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class Khdialog extends JDialog {
    
    public static final int             TYPE_INFO       = 1;
    public static final int             TYPE_QUESTION   = 2;
    public static final int             TYPE_REMEMBER   = 32;
    
    public static final int             ANSWER_NO       = 1;
    public static final int             ANSWER_YES      = 2;
    
    private int                         answer          = ANSWER_NO;
    
    private JCheckBox                   checkRemember;
    
    public Khdialog( Imx icon, String title, String text, int type ) {
        
        this.setModal( true );
        this.setAlwaysOnTop( true );
        this.setIconImage( icon.getImage() );
        this.setResizable( false );
        
        this.getRootPane().setBorder( BorderFactory.createLineBorder( Kholor.BLUE ) );
        
        this.getContentPane().setLayout( new GridBagLayout() );
        
        JLabel labelTitle = new JLabel( title ) {
            
            @Override
            protected void paintComponent(Graphics g) {
                
                g.setColor( Kholor.BLUE );
                g.fillRect( 0, 0, 32, 32 );
                
                g.drawImage( icon.color( Color.WHITE ).getImage(), 8, 8, null );
                
                super.paintComponent(g);
            }
        };
        
        labelTitle.setFont( TextStyle.CONFIG_HEADER.getFontActive() );
        labelTitle.setForeground( TextStyle.CONFIG_HEADER.getColorActive() );
        labelTitle.setHorizontalAlignment( JLabel.CENTER );
        labelTitle.setPreferredSize( new Dimension( labelTitle.getPreferredSize().width, 32 ) );
        
        JLabel labelText = new JLabel( text );
        labelText.setFont( TextStyle.TUNNEL_RUNNING.getFontActive() );
        labelText.setHorizontalAlignment( SwingConstants.CENTER );
        
        this.getContentPane().add( labelTitle,  new GridBagConstraints( 0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 16, 0 ), 0, 0 ) );
        this.getContentPane().add( labelText,   new GridBagConstraints( 0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 16, 0 ), 0, 0 ) );
        
        if ( ( type & Khdialog.TYPE_QUESTION ) == Khdialog.TYPE_QUESTION ) {
            
            Khbutton buttonYes = new Khbutton( "YES", Imx.YES ) {
                
                @Override
                public void click() {
                    
                    Khdialog.this.answer = Khdialog.ANSWER_YES;
                    Khdialog.this.setVisible( false );
                }
                
            };
            
            Khbutton buttonNo  = new Khbutton( "NO", Imx.NO ) {
                
                @Override
                public void click() {

                    Khdialog.this.answer = Khdialog.ANSWER_NO;
                    Khdialog.this.setVisible( false );
                }
                
            };
            
            buttonNo.setMinimumSize( buttonYes.getMinimumSize() );
            buttonNo.setMaximumSize( buttonYes.getMaximumSize() );
            buttonNo.setPreferredSize( buttonYes.getPreferredSize() );
            
            this.getContentPane().add( buttonYes,   new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.BOTH, new Insets( 0, 0, 4, 8 ), 0, 0 ) );
            this.getContentPane().add( buttonNo,    new GridBagConstraints( 1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_END, GridBagConstraints.BOTH, new Insets( 0, 8, 4, 0 ), 0, 0 ) );
        }
        
        if ( ( type & Khdialog.TYPE_REMEMBER ) == Khdialog.TYPE_REMEMBER ) {
            
            this.checkRemember = new JCheckBox( "Remember this and do not ask again!" );
            this.checkRemember.setFocusPainted( false );
            this.checkRemember.setFont( new Font( Font.DIALOG, Font.PLAIN, checkRemember.getFont().getSize() ) );
            this.checkRemember.setForeground( Kholor.BLUE );
            this.checkRemember.setHorizontalAlignment( SwingConstants.CENTER );
            
            this.getContentPane().add( checkRemember, new GridBagConstraints( 0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.PAGE_END, GridBagConstraints.HORIZONTAL, new Insets( 0, 8, 4, 8 ), 0, 0 ) );
        }
        
        this.pack();
    }
    
    public int getAnswer() {
        
        return this.answer;
    }
    
    public boolean getRemember() {
        
        return this.checkRemember.isSelected();
    }

}
