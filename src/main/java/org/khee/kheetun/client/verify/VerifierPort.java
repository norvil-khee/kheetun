package org.khee.kheetun.client.verify;

public class VerifierPort extends Verifier {
    
    public boolean verify(Object value) {
        
        Integer port = (Integer)value;
        return ( port > 0 && port < 0x10000 );
    }
}
