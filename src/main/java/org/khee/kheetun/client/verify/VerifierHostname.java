package org.khee.kheetun.client.verify;

public class VerifierHostname extends VerifierIpAddress {
    
    protected static VerifierHostname instance = new VerifierHostname();
    
    public static VerifierHostname getInstance() {
        return instance;
    }    
    
    @Override
    public String verify(Object value) {
        
        String hostname = (String)value;
        
        if ( hostname.length() > 0 && hostname.matches( "^\\D\\S+$" ) ) {
            return null;
        }
        
        if ( hostname.length() > 0 && super.verify( value ) == null ) {
            return null;
        }
        
        return "Invalid hostname";
    }
}
