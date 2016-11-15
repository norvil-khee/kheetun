package org.khee.kheetun.client.verify;

public class VerifierAlias extends Verifier {

    public boolean verify(Object value) {
        
        String alias = (String)value;
        return alias.length() > 0;
    }

}
