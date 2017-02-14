package org.khee.kheetun.client.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.khee.kheetun.client.KheetunClient;

@SuppressWarnings("serial")
public class AnImx extends JPanel implements ComponentListener {
    
    private BufferedImage               board;
    private ArrayList<BufferedImage>    animation;
    private int                         frame = 0;
    private int                         scale;
    private Timer                       timer;
    private long                        milliseconds;
    private String                      file;

    public AnImx( String file, long milliseconds, int size, int scale ) {
        
        this.file           = file;
        this.scale          = scale;
        this.milliseconds   = milliseconds;
        
        setPreferredSize( new Dimension( scale,  scale ) );
        setMinimumSize( new Dimension( scale,  scale ) );
        setMaximumSize( new Dimension( scale,  scale ) );
        setOpaque( false );
        
        try {
            this.board = ImageIO.read( KheetunClient.class.getResource( "/images/icons/" + file ) );
            
            this.animation = new ArrayList<BufferedImage>();
            
            int x = 0;
            int y = 0;
            
            while ( y < this.board.getHeight() ) {
                
                this.animation.add( this.board.getSubimage( x, y, size, size ) );
                
                if ( ( x += size ) >= this.board.getWidth() ) {
                    x = 0;
                    y += size;
                }
            }
            
        } catch ( Exception e ) {
            
            e.printStackTrace();
        }
        
        this.addComponentListener( this );
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        g.drawImage( this.animation.get( this.frame ).getScaledInstance( this.scale, this.scale, Image.SCALE_SMOOTH ), 0, 0, null );
        g.finalize();
    }
    
    @Override
    public void componentHidden(ComponentEvent e) {
        
        if ( this.timer != null ) {
            this.timer.cancel();
        }
    }
    
    @Override
    public void componentMoved(ComponentEvent e) {
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
    }
    
    @Override
    public void componentShown(ComponentEvent e) {
        
        this.timer = new Timer( "kheetun-animx-timer-" + this.file );
        
        this.timer.schedule( new TimerTask() {
            
            @Override
            public void run() {
                
                if ( ++AnImx.this.frame > AnImx.this.animation.size() - 1 ) {
                    AnImx.this.frame = 0;
                }
                AnImx.this.repaint();
            }
        }, AnImx.this.milliseconds, AnImx.this.milliseconds );
    }
}
