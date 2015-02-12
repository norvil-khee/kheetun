package org.khee.kheetun.client.verify;

public class VerifierFactory {
    
    private static VerifierIpAddress ipAddressVerifier  = new VerifierIpAddress();
    private static VerifierHostname  hostnameVerifier   = new VerifierHostname();
    private static VerifierPort      portVerifier       = new VerifierPort();
    private static VerifierAlias     aliasVerifier      = new VerifierAlias();
    private static VerifierUser      userVerifier       = new VerifierUser();
    private static VerifierSshKey    sshKeyVerifier     = new VerifierSshKey();
    
    public static VerifierIpAddress getIpAddressVerifier() {
        return ipAddressVerifier;
    }
    public static VerifierHostname getHostnameVerifier() {
        return hostnameVerifier;
    }
    public static VerifierPort getPortVerifier() {
        return portVerifier;
    }
    public static VerifierAlias getAliasVerifier() {
        return aliasVerifier;
    }
    public static VerifierUser getUserVerifier() {
        return userVerifier;
    }
    public static VerifierSshKey getSshKeyVerifier() {
        return sshKeyVerifier;
    }
}
