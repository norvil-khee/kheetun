//package org.khee.kheetun.client.gui;
//
//import java.awt.Font;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Point;
//import java.awt.RenderingHints;
//import java.util.ArrayList;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import org.khee.kheetun.client.gui.dialog.Dialog;
//
//public class Khletters {
//    
//    private static final Khletters instance = new Khletters();
//    
//    private class Khletter {
//        
//        public String   letter;
//        public Point    position;
//        public Point    direction       = new Point();
//        public Point    destination     = new Point();
//        public int      y;
//
//        public boolean focused;
//        
//        public Khletter( String letter, int x, int y, double rotation ) {
//            
//            this.letter         = letter;
//            this.position       = new Point( x, y );
//            this.y              = y;
//            
//            this.focused = false;
//            
//            this.randomizeDestination();
//       }
//        
//        public void setDestination( int x, int y, int speed ) {
//            
//            this.destination    = new Point( x, y );
//            
//            int dirX            = x - this.position.x;
//            int dirY            = y - this.position.y;
//            double length       = Math.sqrt( Math.pow( dirX, 2 ) + Math.pow( dirY, 2 ) );
//            
//            this.direction.x    = (int)Math.round( dirX / length * speed );
//            this.direction.y    = (int)Math.round( dirY / length * speed );
//        }
//
//        public void randomizeDestination() {
//            
//            this.setDestination( (int)Math.round( Math.random() * 200 ), this.y, (int)Math.round( Math.random() * 10 ) );
//        }
//    };
//    
//    private ArrayList<Khletter>             letters = new ArrayList<Khletter>();
//    private ArrayList<ArrayList<Khletter>>  words   = new ArrayList<ArrayList<Khletter>>();
//    
//    private Font font = new Font( Font.DIALOG, Font.BOLD, 12 );
//    
//    public Khletters() {
//        
//        Timer timer = new Timer( "kheetun-letter-animator" );
//        
//        timer.schedule( new TimerTask() {
//            
//            @Override
//            public void run() {
//                
//                Khletters.this.animateLetters();
//            }
//        }, 0, 50 );
//        
//        Dialog.getInstance().addDialogScene( Dialog.getInstance().new DialogScene() {
//            
//            @Override
//            public void drawScene(Graphics g) {
//                
//                Khletters.this.paintLetters( g );
//            }
//        });
//    }
//    
//    public static Khletters getInstance() {
//        
//        return Khletters.instance;
//    }
//    
//    private void paintLetters( Graphics g ) {
//        
//        for ( Khletter letter : this.letters ) {
//            
//            Graphics2D g2d = (Graphics2D)g.create();
//            
//            g2d.clipRect( 0, 0, 100, 400 );
//            
//            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
//            g2d.setColor( Kholor.BLUE );
//            g2d.setFont( this.font );
//            g2d.drawString( letter.letter, letter.position.x, letter.position.y );
//            
//            g2d.dispose();
//        }
//    }
//    
//    private void animateLetters() {
//        
//        for ( Khletter letter : this.letters ) {
//            
//            letter.position.x += letter.direction.x > 0 ? Math.min( letter.direction.x, letter.destination.x - letter.position.x ) : Math.max( letter.direction.x, letter.destination.x - letter.position.x );
//            letter.position.y += letter.direction.y > 0 ? Math.min( letter.direction.y, letter.destination.y - letter.position.y ) : Math.max( letter.direction.y, letter.destination.y - letter.position.y );
//            
//            if ( letter.position.x == letter.destination.x && letter.position.y == letter.destination.y ) {
//                
//                if ( ! letter.focused ) {
//                
//                    letter.randomizeDestination();
//                }
//            }
//        }
//        
//        Dialog.getInstance().getGlassPane().repaint();
//    }
//    
//    public void focusWord( int word, int x, int y ) {
//        
//        int diff = 0;
//        
//        for ( Khletter letter : this.words.get( word ) ) {
//            
//            letter.setDestination( x + diff * 14, y, 10 );
//            letter.focused = true;
//            diff++;
//        }
//    }
//    
//    public void releaseWord( int word ) {
//        
//        for ( Khletter letter : this.words.get( word ) ) {
//            
//            letter.randomizeDestination();
//            letter.focused = false;
//        }
//    }
//    
//    public int addLetters( String letters ) {
//        
//        ArrayList<Khletter> word = new ArrayList<Khletter>();
//
//        for ( int i = 0 ; i < letters.length() ; i++ ) {
//            
//            Khletter letter = new Khletter( letters.substring( i, i + 1 ), (int)Math.round( Math.random() * 1000 ), 100, 0.0 );
//
//            this.letters.add( letter );
//            word.add( letter );
//        }
//        
//        this.words.add( word );
//        
//        return this.words.size() - 1;
//        
//    }
//
//}
