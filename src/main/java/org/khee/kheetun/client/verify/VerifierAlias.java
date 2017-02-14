package org.khee.kheetun.client.verify;

public class VerifierAlias extends Verifier {
    
    protected static VerifierAlias instance = new VerifierAlias();
    
    public static VerifierAlias getInstance() {
        return instance;
    }
    
    @Override
    public String verify(Object value) {
        
        if ( value == null ) {
            return "Value must not be null";
        }
        
        String alias = (String)value;
        
        if ( alias.length() > 0 ) {
            return null;
        }
        
        return "Value must not be empty";
    }
}
