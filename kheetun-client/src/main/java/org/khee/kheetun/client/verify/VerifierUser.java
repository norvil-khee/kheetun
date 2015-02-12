package org.khee.kheetun.client.verify;

public class VerifierUser extends Verifier {

    public boolean verify(Object value) {
        
        String user = (String)value;
        return ( user.length() > 0 && user.matches( "^\\S+$" ) );
    }
}
