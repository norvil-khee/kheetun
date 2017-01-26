package org.khee.kheetun.client.verify;

public class VerifierPort extends Verifier {

    protected static VerifierPort instance = new VerifierPort();
    
    public static VerifierPort getInstance() {
        return instance;
    }    
    
    @Override
    public String verify(Object value) {
        
        Integer port = (Integer)value;
        
        if ( port > 0 && port < 0x10000 ) {
            return null;
        }
        
        return "Invalid port (must be greater 0 and equal or less than 65535)";
    }
}
