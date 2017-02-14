package org.khee.kheetun.client.gui;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class Khlabel extends JLabel {
    
    public Khlabel( String text, TextStyle style ) {
        
        super( text );
        this.setFont( style.getFontActive() );
        this.setForeground( style.getColorActive() );
    }

    public Khlabel( Imx icon, TextStyle style ) {
        
        super( icon );
        this.setFont( style.getFontActive() );
        this.setForeground( style.getColorActive() );
    }
    
    public Khlabel( String text, Imx icon, TextStyle style ) {
        
        super( text, icon, JLabel.LEADING );
        this.setFont( style.getFontActive() );
        this.setForeground( style.getColorActive() );
    }
}
