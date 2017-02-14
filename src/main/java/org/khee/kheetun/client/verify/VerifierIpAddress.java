package org.khee.kheetun.client.verify;

public class VerifierIpAddress extends Verifier {
    
    protected static VerifierIpAddress instance = new VerifierIpAddress();
    
    public static VerifierIpAddress getInstance() {
        return instance;
    }    
        
    @Override
    public String verify( Object value ) {
        
        String ipAddress = (String)value;
        
        if ( ipAddress.matches( "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}" ) ) {
            return null;
        }
        
        return "Invalid IP address";
    }
}
