package org.khee.kheetun.client.verify;

public class VerifierSshKey extends Verifier {

    public boolean verify(Object value) {
        
        String key = (String)value;
        
        //FIXME: be a little windows friendly :]
        //
        return key.length() == 0 || key.matches( "^(/[^/\\s]+)+$" );
    }

}
