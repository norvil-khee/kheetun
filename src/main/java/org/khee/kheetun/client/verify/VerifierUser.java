package org.khee.kheetun.client.verify;

public class VerifierUser extends Verifier {
    
    protected static VerifierUser instance = new VerifierUser();
    
    public static VerifierUser getInstance() {
        return instance;
    }  

    @Override
    public String verify(Object value) {
        
        String user = (String)value;
        
        if ( user.length() > 0 && user.matches( "^\\S+$" ) ) {
            
            return null;
        }
        
        return "Value must not be empty";
    }
}
