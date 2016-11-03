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
    
    public static final Imx KHEETUN             = loadImx( "khee5.png" );
    public static final Imx KHEETUN_ON          = loadImx( "kheetun1.png" );
    public static final Imx KHEETUN_OFF         = loadImx( "kheetun1.png" );
    public static final Imx KHEETUN_WARNING     = loadImx( "kheetun_warn.png" );
    public static final Imx SAVE                = loadImx( "save.png" );
    public static final Imx NEW                 = loadImx( "new.png" );
    public static final Imx LOAD                = loadImx( "load.png" );
    public static final Imx DELETE              = loadImx( "delete.png" );
    public static final Imx RENAME              = loadImx( "rename.png" );
    public static final Imx ACTIVE              = loadImx( "tick.png" );
    public static final Imx INACTIVE            = loadImx( "none.png" );
    public static final Imx PLUS                = loadImx( "plus.png" );
    public static final Imx MINUS               = loadImx( "minus.png" );
    public static final Imx REVERT              = loadImx( "revert.png" );
    public static final Imx LION                = loadImx( "bug.png" );
    public static final Imx START               = loadImx( "play-button.png" );
    public static final Imx STOP                = loadImx( "pause.png" );
    public static final Imx KEY                 = loadImx( "key.png" );
    public static final Imx WARNING             = loadImx( "warning.png" );
    public static final Imx WARNING_DISABLED    = loadImx( "warning_gray.png" );
    public static final Imx OK                  = loadImx( "active.png" );
    public static final Imx EXIT                = loadImx( "next-1.png" );
    public static final Imx CONFIG              = loadImx( "settings.png" );
    public static final Imx PROFILE             = loadImx( "layers.png" );
    public static final Imx LOCAL               = loadImx( "local.png" );
    public static final Imx REMOTE              = loadImx( "remote.png" );
    public static final Imx DETAIL              = loadImx( "detail.png" );
    public static final Imx FORWARDS            = loadImx( "forwards.png" );
    public static final Imx NONE                = loadImx( "none.png" );
    public static final Imx RELOAD              = loadImx( "reload.png" );
    public static final Imx AUTO                = loadImx( "favorite.png" );
    public static final Imx AUTO_DISABLED       = loadImx( "favorite_gray.png" );
    
    public ImageIcon icon;
    public ImageIcon s12;
    public ImageIcon s16;
    public ImageIcon s24;
    public ImageIcon s32;
    public ImageIcon s32a;
    public BufferedImage bimg;
    public BufferedImage bs12;
    public BufferedImage bs16;
    public BufferedImage bs24;
    public BufferedImage bs32;
    
    protected static Imx loadImx( String file ) {
        
        try {
            BufferedImage tmp = ImageIO.read( kheetun.class.getResource( "/images/icons/" + file ) );
            
            BufferedImage rgba = new BufferedImage( tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_ARGB );
            rgba.createGraphics().drawImage( tmp, 0, 0, null );
            
            ImageIcon icon = new ImageIcon( rgba );
            
            BufferedImage bimg12;
            BufferedImage bimg16;
            BufferedImage bimg24;
            BufferedImage bimg32;
            
            bimg32 = new BufferedImage( 32, 32, BufferedImage.TYPE_INT_ARGB );
            bimg32.createGraphics().drawImage( rgba.getScaledInstance( 32, 32, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon32 = new ImageIcon( bimg32 );

            bimg24 = new BufferedImage( 24, 24, BufferedImage.TYPE_INT_ARGB );
            bimg24.createGraphics().drawImage( rgba.getScaledInstance( 24, 24, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon24 = new ImageIcon( bimg24 );

            bimg16 = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB );
            bimg16.createGraphics().drawImage( rgba.getScaledInstance( 16, 16, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon16 = new ImageIcon( bimg16 );

            bimg12 = new BufferedImage( 12, 12, BufferedImage.TYPE_INT_ARGB );
            bimg12.createGraphics().drawImage( rgba.getScaledInstance( 12, 12, Image.SCALE_SMOOTH ), 0, 0, null );
            ImageIcon icon12 = new ImageIcon( bimg12 );
            
            return new Imx( icon, icon12, icon16, icon24, icon32, rgba, bimg12, bimg16, bimg24, bimg32 );
            
        } catch ( Exception e ) {
            logger.error( "Could not load " + file + ": " + e.getMessage() );
            System.exit( 1 );
        }
        
        return null;
    }
    
//    private static BufferedImage recolor( BufferedImage image, Color color ) {
//        
//        System.out.println( "Coloring: " + image + " to " + color );
//        
//        for ( int x = 0 ; x < image.getWidth() ; x++ ) {
//            for ( int y = 0 ; y < image.getHeight() ; y++ ) {
//                
//                Color current = new Color( image.getRGB( x, y ), true );
//                
//                if ( current.getAlpha() == 255 ) {
//                    
//                    image.setRGB( x, y, color.getRGB() );
//                }
//            }
//        }
//        
//        return image;
//    }
    
    protected Imx( ImageIcon icon, ImageIcon icon12, ImageIcon icon16, ImageIcon icon24, ImageIcon icon32,
            BufferedImage bimg, BufferedImage bimg12, BufferedImage bimg16, BufferedImage bimg24, BufferedImage bimg32 ) {
        
        this.setImage( icon16.getImage() );
        
        this.icon   = icon;
        this.s12    = icon12;
        this.s16    = icon16;
        this.s24    = icon24;
        this.s32    = icon32;
        
        this.bimg = bimg;
        this.bs12 = bimg12;
        this.bs16 = bimg16;
        this.bs24 = bimg24;
        this.bs32 = bimg32;
    }

}


