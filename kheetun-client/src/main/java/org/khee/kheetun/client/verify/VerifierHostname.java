package org.khee.kheetun.client.verify;

public class VerifierHostname extends VerifierIpAddress {
    
    public boolean verify(Object value) {
        
        String hostname = (String)value;
        return ( hostname.length() > 0 && ( hostname.matches( "^\\D\\S+$" ) || super.verify( hostname ) ) );
    }
}
