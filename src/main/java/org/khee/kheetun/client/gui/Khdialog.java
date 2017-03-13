package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class Khdialog extends JDialog {
    
    public static final int             TYPE_INFO           = 1;
    public static final int             TYPE_YES_NO         = 2;
    public static final int             TYPE_YES_NO_CANCEL  = 4;
    public static final int             TYPE_OK_CANCEL      = 8;
    public static final int             TYPE_INPUT          = 16;
    public static final int             TYPE_PASSWORD       = 32;
    public static final int             TYPE_REMEMBER       = 128;
    
    public static final int             ANSWER_NO           = 1;
    public static final int             ANSWER_YES          = 2;
    public static final int             ANSWER_OK           = 4;
    public static final int             ANSWER_CANCEL       = 8;
    
    private int                         answer              = ANSWER_CANCEL;
    
    private JCheckBox                   checkRemember;
    private JTextField                  fieldInput;
    private JPasswordField              fieldPassword;
    
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
        
        this.getContentPane().add( labelTitle,  new GridBagConstraints( 0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 16, 0 ), 0, 0 ) );
        this.getContentPane().add( labelText,   new GridBagConstraints( 0, 1, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 8, 16, 8 ), 0, 0 ) );
        
        if ( ( type & Khdialog.TYPE_YES_NO ) == Khdialog.TYPE_YES_NO ) {
            
            Khbutton buttonYes = this.createButton( "YES", Imx.YES, Khdialog.ANSWER_YES );
            Khbutton buttonNo = this.createButton( "NO", Imx.NO, Khdialog.ANSWER_NO );
            
            this.getContentPane().add( buttonYes,   new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.BOTH, new Insets( 0, 0, 4, 8 ), 0, 0 ) );
            this.getContentPane().add( buttonNo,    new GridBagConstraints( 2, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_END, GridBagConstraints.BOTH, new Insets( 0, 8, 4, 0 ), 0, 0 ) );
        }

        if ( ( type & Khdialog.TYPE_YES_NO_CANCEL ) == Khdialog.TYPE_YES_NO_CANCEL ) {
            
            Khbutton buttonYes      = this.createButton( "YES", Imx.YES, Khdialog.ANSWER_YES );
            Khbutton buttonNo       = this.createButton( "NO", Imx.NO, Khdialog.ANSWER_NO );
            Khbutton buttonCancel   = this.createButton( "CANCEL", Imx.NO, Khdialog.ANSWER_CANCEL );
            
            this.getContentPane().add( buttonYes,       new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.BOTH, new Insets( 0, 0, 4, 4 ), 0, 0 ) );
            this.getContentPane().add( buttonNo,        new GridBagConstraints( 1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.PAGE_END, GridBagConstraints.BOTH, new Insets( 0, 4, 4, 4 ), 0, 0 ) );
            this.getContentPane().add( buttonCancel,    new GridBagConstraints( 2, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_END, GridBagConstraints.BOTH, new Insets( 0, 4, 4, 0 ), 0, 0 ) );
        }
        
        if ( ( type & Khdialog.TYPE_PASSWORD ) == Khdialog.TYPE_PASSWORD ) {
            
            this.fieldPassword = new JPasswordField();
            this.fieldPassword.setPreferredSize( new Dimension( 200, 24 ) );
            this.fieldPassword.setFont( TextStyle.CONFIG_HEADER.getFontActive() );
            this.fieldPassword.addKeyListener( new KeyListener() {
                
                @Override
                public void keyTyped(KeyEvent e) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void keyReleased(KeyEvent e) {
                    
                    if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
                        
                        Khdialog.this.answer = Khdialog.ANSWER_OK;
                        Khdialog.this.setVisible( false );
                    }
                    
                    if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                        
                        Khdialog.this.answer = Khdialog.ANSWER_CANCEL;
                        Khdialog.this.setVisible( false );
                    }
                }
                
                @Override
                public void keyPressed(KeyEvent e) {
                    // TODO Auto-generated method stub
                    
                }
            });
            
            Khbutton buttonOk       = this.createButton( "OK", Imx.YES, Khdialog.ANSWER_OK );
            Khbutton buttonCancel   = this.createButton( "CANCEL", Imx.NO, Khdialog.ANSWER_CANCEL );
            
            this.getContentPane().add( this.fieldPassword,  new GridBagConstraints( 0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 8, 4, 8 ), 0, 0 ) );
            this.getContentPane().add( buttonOk,            new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets( 0, 0, 4, 4 ), 0, 0 ) );
            this.getContentPane().add( buttonCancel,        new GridBagConstraints( 2, 3, 1, 1, 1.0, 0.0, GridBagConstraints.LAST_LINE_END, GridBagConstraints.NONE, new Insets( 0, 4, 4, 0 ), 0, 0 ) );
        }
        

        if ( ( type & Khdialog.TYPE_REMEMBER ) == Khdialog.TYPE_REMEMBER ) {
            
            this.checkRemember = new JCheckBox( "Remember and do not ask again" );
            this.checkRemember.setFocusPainted( false );
            this.checkRemember.setFont( new Font( Font.DIALOG, Font.PLAIN, checkRemember.getFont().getSize() ) );
            this.checkRemember.setForeground( Kholor.BLUE );
            this.checkRemember.setHorizontalAlignment( SwingConstants.CENTER );
            
            this.getContentPane().add( checkRemember, new GridBagConstraints( 0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.PAGE_END, GridBagConstraints.HORIZONTAL, new Insets( 0, 8, 4, 8 ), 0, 0 ) );
        }
        
        this.pack();
    }
    
    private Khbutton createButton( String text, Imx icon, int answer ) {
        
        Khbutton button = new Khbutton( text, icon ) {
            
            @Override
            public void click() {
                
                Khdialog.this.answer = answer;
                Khdialog.this.setVisible( false );
            }
        };
        
        button.setMinimumSize( new Dimension( 90, button.getMinimumSize().height ) );
        button.setMaximumSize( new Dimension( 90, button.getMaximumSize().height ) );
        button.setPreferredSize( new Dimension( 90, button.getPreferredSize().height ) );
        
        return button;
    }
    
    public int getAnswer() {
        
        return this.answer;
    }
    
    public boolean getRemember() {
        
        return this.checkRemember.isSelected();
    }
    
    public char[] getPassword() {
        
        return this.fieldPassword.getPassword();
    }

}
