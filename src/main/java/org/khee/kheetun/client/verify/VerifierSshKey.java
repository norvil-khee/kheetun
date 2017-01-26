package org.khee.kheetun.client.verify;

import java.io.File;

public class VerifierSshKey extends Verifier {
    
    protected static VerifierSshKey instance = new VerifierSshKey();
    
    public static VerifierSshKey getInstance() {
        return instance;
    }        

    public String verify(Object value) {
        
        File keyFile = (File)value;
        
        if ( keyFile == null || keyFile.getAbsolutePath().matches( "^(/[^/\\s]+)+$" ) ) {
            return null;
        }
        
        return "Invalid SSH key file";
    }

}
