package org.khee.kheetun.client.gui;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.Menu;
import org.gnome.gtk.StatusIcon;
import org.gnome.gtk.StatusIcon.Activate;
import org.gnome.gtk.StatusIcon.PopupMenu;

public class GtkTray extends Tray {
    
    public static Pixbuf OFFLINE;
    public static Pixbuf ONLINE;
    public static Pixbuf WARNING;
    public static Pixbuf NONE;
    
    static {
        try {
            ONLINE      = pixbufFromBufferedImage( Imx.KHEETUN_ON.bs32 );
            OFFLINE     = pixbufFromBufferedImage( Imx.KHEETUN_OFF.bs32 );
            WARNING     = pixbufFromBufferedImage( Imx.KHEETUN.bs32 );
            NONE        = pixbufFromBufferedImage( Imx.NONE.bs32 );
            
        } catch ( IOException e ) {
            
            System.err.println( "Failed to load tray icons: " + e.getMessage() );
            System.exit( 1 );
        }
    };
    
    private StatusIcon  icon        = new StatusIcon(); 
    private Menu        hack        = new Menu();
    private Pixbuf      current;
    
    public GtkTray() {
        
        Thread blinkThread = new Thread( this );
        blinkThread.setName( "kheetun-tray-blink-thread" );
        blinkThread.start();

        icon.connect( new Activate() {
            
            public void onActivate(StatusIcon tray) {
                
                // why we are hacking our way to the correct position of the popup menu?
                // because, first of all, we want transparent tray icons
                // then we want to use a swing menu
                // unfortunately JAVAs SystemTray implementation does not allow transparency
                // however GTK does not allow swing popup menus
                // so we are going this way...
                // 
                hack.popup( tray );
                hack.hide();
                TrayManager.toggleMenu( new Point( hack.getWindow().getOriginX(), hack.getWindow().getOriginY() ) );
            }
        });
        
        icon.connect( new PopupMenu() {
            
            @Override
            public void onPopupMenu(StatusIcon tray, int arg1, int arg2) {

                hack.popup( tray );
                hack.hide();
                TrayManager.toggleMenu( new Point( hack.getWindow().getOriginX(), hack.getWindow().getOriginY() ) );
            }
        });
    }
    
    private static Pixbuf pixbufFromBufferedImage( BufferedImage image ) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( image, "png", baos );

        return new Pixbuf( baos.toByteArray() );
    }
    
    @Override
    public void run() {
        
        try {
        
            while( true ) {
                
                if ( blinking ) { 
                    icon.setFromPixbuf( NONE );
                    Thread.sleep( 500 );
                    icon.setFromPixbuf( current );
                    Thread.sleep( 500 );
                } else {
                    Thread.sleep( 1000 );
                }
            }
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void setIcon(Imx icon) {
        
        try {
            
            current = GtkTray.pixbufFromBufferedImage( icon.bs32 );
            this.icon.setFromPixbuf( current );
            
        } catch ( IOException e ) {
            
            logger.error( "Could not set tray icon: " + e.getMessage() );
        }
    }
    
}