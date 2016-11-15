package org.khee.kheetun.client.verify;

public class VerifierIpAddress extends Verifier {

    public boolean verify( Object value ) {
        
        String ipAddress = (String)value;
        return ipAddress.matches( "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}" );
    }
}
