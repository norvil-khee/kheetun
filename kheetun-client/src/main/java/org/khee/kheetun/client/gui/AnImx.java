package org.khee.kheetun.client.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.khee.kheetun.client.kheetun;

public class AnImx extends JPanel {
    
    public static final long serialVersionUID = 42;
    
    private BufferedImage board;
    private ArrayList<BufferedImage> animation;
    private int frame = 0;
    private int scale;
    
    
    
    public AnImx( String file, long milliseconds, int size, int scale ) {
        
        this.scale = scale;
        
        setDoubleBuffered( true );
        setPreferredSize( new Dimension( scale,  scale ) );
        setMinimumSize( new Dimension( scale,  scale ) );
        setMaximumSize( new Dimension( scale,  scale ) );
        setOpaque( false );
        
        try {
            board = ImageIO.read( kheetun.class.getResource( "/images/" + file ) );
            
            animation = new ArrayList<BufferedImage>();
            
            int x = 0;
            int y = 0;
            
            while ( y < board.getHeight() ) {
                
                animation.add( board.getSubimage( x, y, size, size ) );
                
                if ( ( x += size ) >= board.getWidth() ) {
                    x = 0;
                    y += size;
                }
            }
            
            Timer timer = new Timer();
            
            timer.schedule( new TimerTask() {
                
                @Override
                public void run() {
                    
                    if ( ++frame > animation.size() - 1 ) {
                        frame = 0;
                    }
                    repaint();
                }
            }, milliseconds, milliseconds );
            
        } catch ( Exception e ) {
            
            
            e.printStackTrace();
        }
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        g.drawImage( animation.get( frame ).getScaledInstance( scale, scale, Image.SCALE_SMOOTH ), 0, 0, null );
        g.finalize();
    }
    
    
    
    

}
