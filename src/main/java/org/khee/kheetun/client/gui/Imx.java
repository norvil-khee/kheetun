package org.khee.kheetun.client.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.khee.kheetun.client.KheetunClient;


public class Imx extends ImageIcon {
    
    public static final long serialVersionUID = 42;
    
    private static Logger logger = LogManager.getLogger( "kheetun" );

    private static HashMap<String, Imx> database    = new HashMap<String, Imx>();
    
    public static final Imx KHEETUN             = Imx.createImx( "kheetun_logo.png" );
    public static final Imx TRAY                = Imx.createImx( "tray.png" );
    public static final Imx CONFIGURATION       = Imx.createImx( "configuration.png" );
    public static final Imx SAVE                = Imx.createImx( "save.png" );
    public static final Imx EDIT                = Imx.createImx( "edit.png" );
    public static final Imx RUNNING             = Imx.createImx( "running.png" );
    public static final Imx STOPPED             = Imx.createImx( "none.png" );
    public static final Imx START               = Imx.createImx( "play.png" );
    public static final Imx STOP                = Imx.createImx( "stop.png" );
    public static final Imx WARNING             = Imx.createImx( "warning.png" );
    public static final Imx WARNING_DISABLED    = Imx.createImx( "warning_gray.png" );
    public static final Imx EXIT                = Imx.createImx( "exit.png" );
    public static final Imx PROFILE             = Imx.createImx( "profile.png" );
    public static final Imx PROFILE_SORT        = Imx.createImx( "profile_sort.png" );
    public static final Imx NONE                = Imx.createImx( "none.png" );
    public static final Imx AUTO                = Imx.createImx( "autostart.png" );
    public static final Imx INFO                = Imx.createImx( "info.png" );
    public static final Imx KEY                 = Imx.createImx( "key.png" );
    public static final Imx POWER               = Imx.createImx( "power.png" );
    public static final Imx USER                = Imx.createImx( "user.png" );
    public static final Imx HOST                = Imx.createImx( "host.png" );
    public static final Imx PORT                = Imx.createImx( "port.png" );
    public static final Imx REVERT              = Imx.createImx( "revert.png" );
    public static final Imx DIRECTION           = Imx.createImx( "direction.png" );
    public static final Imx CROSS               = Imx.createImx( "cross.png" );
    public static final Imx PLUS                = Imx.createImx( "plus.png" );
    public static final Imx UNDO                = Imx.createImx( "undo.png" );
    public static final Imx NEW                 = Imx.createImx( "new.png" );
    
    private String  file;
    private Integer scale;
    private Integer mask;
    
    public Imx disabled() {
        
        return Imx.createImx( this.file, this.scale, 0 );
    }
    
    public Imx hover() {
        
        return this.lighten( 128 );
    }
    
    public Imx lighten( Integer degree ) {
        
        return Imx.createImx( this.file, this.scale, 
                this.mask & 0xFF000000 
              | Math.min( ( this.mask & 0xFF0000 ) + ( degree << 16 ), 0xFF0000 ) 
              | Math.min( ( this.mask & 0xFF00 )   + ( degree << 8  ), 0xFF00   ) 
              | Math.min( ( this.mask & 0xFF )     + ( degree ),       0xFF     ) ); 
    }
    
    public Imx shadow( int mask ) {
        
        return Imx.createImx( this.file, this.scale, mask & 0xFFFFFF );
    }
    
    public Imx red() {
        
        return Imx.createImx( this.file, this.scale, 0xFFFF0000 );
    }
    
    public Imx size( Integer size ) {
        
        return Imx.createImx( this.file, size, this.mask );
    }
    
    public Imx color( Color color ) {
        
        return Imx.createImx( this.file, this.scale, color.getRGB() );
    }
    
    
    public static Imx createImx( String file ) {
        
        return Imx.createImx( file, 16, 0xFF359eff );
    }
    
    public static Imx createImx( String file, Integer scale, Integer mask ) {
        
        String id = file + ":" + scale.toString() + ":" + Integer.toHexString( mask );
        
        if ( Imx.database.containsKey( id ) ) {

            return Imx.database.get( id );
        }
        
        try {
            
            logger.trace( "Creating new image: " + file + ", " + scale + "x" + scale + "[" + Integer.toHexString( mask ) + "]" );
            
            BufferedImage tmp = ImageIO.read( KheetunClient.class.getResource( "/images/icons/" + file ) );
            
            BufferedImage rgba = new BufferedImage( tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_ARGB );
            rgba.createGraphics().drawImage( tmp, 0, 0, null );
            
            BufferedImage scaled = new BufferedImage( scale, scale, BufferedImage.TYPE_INT_ARGB );
            scaled.createGraphics().drawImage( Imx.recolor( rgba, mask ).getScaledInstance( scale, scale, Image.SCALE_SMOOTH ), 0, 0, null );

            Imx imx = new Imx( file, scale, mask );
            imx.setImage( scaled );
            
            Imx.database.put( id, imx );
            
            return imx;
                       
        } catch ( Exception e ) {
            
            logger.error( "Could not load " + file + ": " + e.getMessage() );
            logger.debug( "", e );
            System.exit( 1 );
        }        
        
        return null;
    }

    protected Imx( String file, Integer scale, Integer mask ) {
        
        this.file       = file;
        this.scale      = scale;
        this.mask       = mask;
    }
    
    private static BufferedImage recolor( BufferedImage image, int mask ) {
        
        for ( int x = 0 ; x < image.getWidth() ; x++ ) {
            for ( int y = 0 ; y < image.getHeight() ; y++ ) {
                
                Color color         = new Color( image.getRGB( x, y ), true );
                
                if ( color.getAlpha() != 0 ) {
                    
                    image.setRGB( x, y, new Color( mask, false ).getRGB() );
                    continue;
                }
                
//                if ( color.getRGB() == 0xFF000000 ) {
//                    continue;
//                }
//                
//                Color colorMask = new Color( mask, true );
//                
//                int alpha = color.getAlpha();
//                
//                int red   = Math.round( 255.0f - ( 255.0f - color.getRed() )   / 255.0f * ( 255.0f - colorMask.getRed() ) );
//                int green = Math.round( 255.0f - ( 255.0f - color.getGreen() ) / 255.0f * ( 255.0f - colorMask.getGreen() ) );
//                int blue  = Math.round( 255.0f - ( 255.0f - color.getBlue() )  / 255.0f * ( 255.0f - colorMask.getBlue() ) );
//                
//                image.setRGB( x, y, new Color( red, green, blue, alpha ).getRGB() );
            }
        }
        
        return image;
    }
    
    public static BufferedImage imageToBufferedImage( Image image ) {
        
        if ( image instanceof BufferedImage ) {
            
            return (BufferedImage)image;
        }

        BufferedImage bimage = new BufferedImage( image.getWidth( null ), image.getHeight( null ), BufferedImage.TYPE_INT_ARGB );

        Graphics2D g2d = bimage.createGraphics();
        g2d.drawImage( image, 0, 0, null );
        g2d.dispose();

        // Return the buffered image
        return bimage;
    }
    
}


