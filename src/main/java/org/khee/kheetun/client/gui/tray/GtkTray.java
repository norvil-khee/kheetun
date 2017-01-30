package org.khee.kheetun.client.gui.tray;

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
import org.khee.kheetun.client.gui.Imx;

public class GtkTray extends Tray {
    
    private StatusIcon  statusIcon      = new StatusIcon(); 
    private Menu        hack            = new Menu();
    private Pixbuf      current;
    
    public GtkTray() {
        
        statusIcon.connect( new Activate() {
            
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
        
        statusIcon.connect( new PopupMenu() {
            
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
    protected void processSetIcon( Imx icon ) {
        
        try {
            
            current = GtkTray.pixbufFromBufferedImage( Imx.imageToBufferedImage( icon.getImage() ) );
            this.statusIcon.setFromPixbuf( current );
            
        } catch ( IOException e ) {
            
            logger.error( "Could not set tray icon: " + e.getMessage() );
        }
    }
    
}
