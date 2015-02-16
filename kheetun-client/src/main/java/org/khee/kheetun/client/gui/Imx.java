package org.khee.kheetun.client.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.khee.kheetun.client.kheetun;


public class Imx extends ImageIcon {
    
    public static final long serialVersionUID = 42;
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    public static final Imx KHEETUN     = loadImx( "kheetun32.png" );
    public static final Imx KHEETUN_ON  = loadImx( "kheetun_on.png" );
    public static final Imx KHEETUN_OFF = loadImx( "kheetun32.png" );
    public static final Imx SAVE        = loadImx( "save.png" );
    public static final Imx NEW         = loadImx( "new.png" );
    public static final Imx LOAD        = loadImx( "load.png" );
    public static final Imx DELETE      = loadImx( "delete.png" );
    public static final Imx RENAME      = loadImx( "rename.png" );
    public static final Imx ACTIVE      = loadImx( "online.png" );
    public static final Imx INACTIVE    = loadImx( "offline.png" );
    public static final Imx PLUS        = loadImx( "plus.png" );
    public static final Imx MINUS       = loadImx( "minus.png" );
    public static final Imx REVERT      = loadImx( "revert.png" );
    public static final Imx LION        = loadImx( "bug.png" );
    public static final Imx START       = loadImx( "play.png" );
    public static final Imx STOP        = loadImx( "stop.png" );
    public static final Imx KEY         = loadImx( "key.png" );
    public static final Imx WARNING     = loadImx( "warning.png" );
    public static final Imx OK          = loadImx( "active.png" );
    public static final Imx EXIT        = loadImx( "exit.png" );
    public static final Imx CONFIG      = loadImx( "config.png" );
    public static final Imx PROFILE     = loadImx( "profile.png" );
    public static final Imx LOCAL       = loadImx( "local.png" );
    public static final Imx REMOTE      = loadImx( "remote.png" );
    public static final Imx DETAIL      = loadImx( "detail.png" );
    public static final Imx FORWARDS    = loadImx( "forwards.png" );

    public ImageIcon s12;
    public ImageIcon s16;
    public ImageIcon s24;
    public ImageIcon s32;
    public BufferedImage bs12;
    public BufferedImage bs16;
    public BufferedImage bs24;
    public BufferedImage bs32;
    
    protected static Imx loadImx( String file ) {
        
        
        try {
            BufferedImage tmp = ImageIO.read( kheetun.class.getResource( "/images/" + file ) );
     
            BufferedImage bimg12;
            BufferedImage bimg16;
            BufferedImage bimg24;
            BufferedImage bimg32;
            
            bimg32 = new BufferedImage( 32, 32, BufferedImage.TYPE_INT_ARGB );
            bimg32.createGraphics().drawImage( tmp.getScaledInstance( 32, 32, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon32 = new ImageIcon( bimg32 );

            bimg24 = new BufferedImage( 24, 24, BufferedImage.TYPE_INT_ARGB );
            bimg24.createGraphics().drawImage( tmp.getScaledInstance( 24, 24, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon24 = new ImageIcon( bimg24 );

            bimg16 = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB );
            bimg16.createGraphics().drawImage( tmp.getScaledInstance( 16, 16, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon16 = new ImageIcon( bimg16 );

            bimg12 = new BufferedImage( 12, 12, BufferedImage.TYPE_INT_ARGB );
            bimg12.createGraphics().drawImage( tmp.getScaledInstance( 12, 12, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon12 = new ImageIcon( bimg12 );
            
            return new Imx( icon12, icon16, icon24, icon32, bimg12, bimg16, bimg24, bimg32 );
            
        } catch ( Exception e ) {
            logger.error( e.getMessage() );
            System.exit( 1 );
        }
        
        return null;
    }
    
    protected Imx( ImageIcon icon12, ImageIcon icon16, ImageIcon icon24, ImageIcon icon32,
            BufferedImage bimg12, BufferedImage bimg16, BufferedImage bimg24, BufferedImage bimg32 ) {
        
        this.setImage( icon16.getImage() );
        
        this.s12 = icon12;
        this.s16 = icon16;
        this.s24 = icon24;
        this.s32 = icon32;
        
        this.bs12 = bimg12;
        this.bs16 = bimg16;
        this.bs24 = bimg24;
        this.bs32 = bimg32;
    }

}
