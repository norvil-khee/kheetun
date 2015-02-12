package org.khee.kheetun.client.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class Profile {
    
    private String                  name;
    private ArrayList<Tunnel>       tunnels;
    
    
    public Profile() {
        tunnels = new ArrayList<Tunnel>();
    }
    
    public Profile( Profile source ) {
    
        this.tunnels    = new ArrayList<Tunnel>();
        this.name       = source.name;
        
        for( Tunnel tunnel : source.getTunnels() ) { 
            
            addTunnel( new Tunnel( tunnel ) );
        }
    }
    
    @XmlAttribute
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlElement( name="tunnel" )
    public ArrayList<Tunnel> getTunnels() {
        return tunnels;
    }
    public void setTunnels(ArrayList<Tunnel> tunnels) {
        this.tunnels = tunnels;
    }
    
    public void addTunnel( Tunnel tunnel ) {
        tunnels.add( tunnel );
    }
    
    public boolean isValid() {
        
        for( Tunnel tunnel : getTunnels() ) {
            
            if ( ! tunnel.isValid() ) {
                return false;
            }
        }
        return true; 
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if ( obj == null ) {
            return false;
        }
        
        Profile compare = (Profile)obj;
        
        ArrayList<Tunnel> tunnels = this.getTunnels();
        ArrayList<Tunnel> tunnelsCompare = compare.getTunnels();
        
        // not the same amount of tunnels? not equal!
        //
        if ( tunnels.size() != tunnelsCompare.size() ) {
            return false;
        }
        
        // tunnels differ? not equal!
        //
        for ( int index = 0 ; index < tunnels.size() ; index++ ) {
            
            if ( ! tunnels.get( index ).equals( tunnelsCompare.get( index )) ) {
                return false;
            }
        }

        return this.getName().equals( compare.getName() );
    }

}
