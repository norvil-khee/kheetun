package org.khee.kheetun.client.verify;

import java.io.File;

public class VerifierConfigFile extends Verifier {
    
    protected static VerifierConfigFile instance = new VerifierConfigFile();
    
    public static VerifierConfigFile getInstance() {
        return instance;
    }
    
    @Override
    public String verify(Object value) {
        
        File file = (File)value;
        
        if ( file == null ) {
            
            return "Config file is null";
        }
        
        return null;
    }
}
