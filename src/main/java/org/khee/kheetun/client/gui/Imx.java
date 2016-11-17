package org.khee.kheetun.client.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.KheetunClient;


public class Imx extends ImageIcon {
    
    public static final long serialVersionUID = 42;
    
    private static Logger logger = LogManager.getLogger( "kheetun" );
    
    public static final Imx KHEETUN             = loadImx( "kheetun_logo.png" );
    public static final Imx TRAY_OK             = loadImx( "tray_ok.png" );
    public static final Imx TRAY_ERROR          = loadImx( "tray_warn.png" );
    public static final Imx RUNNING             = loadImx( "running.png" );
    public static final Imx STOPPED             = loadImx( "none.png" );
    public static final Imx START               = loadImx( "play.png" );
    public static final Imx STOP                = loadImx( "stop.png" );
    public static final Imx WARNING             = loadImx( "warning.png" );
    public static final Imx WARNING_DISABLED    = loadImx( "warning_gray.png" );
    public static final Imx EXIT                = loadImx( "exit.png" );
    public static final Imx PROFILE             = loadImx( "profile.png" );
    public static final Imx PROFILE_SORT        = loadImx( "profile_sort.png" );
    public static final Imx NONE                = loadImx( "none.png" );
    public static final Imx AUTO                = loadImx( "autostart.png" );
    public static final Imx AUTO_DISABLED       = loadImx( "autostart_off.png" );
    public static final Imx INFO                = loadImx( "info.png" );
    
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
            BufferedImage tmp = ImageIO.read( KheetunClient.class.getResource( "/images/icons/" + file ) );
            
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


