package org.khee.kheetun.server;

import java.io.IOException;

import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Buffer;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.USocketFactory;

public class SSHSocketAgentConnector implements Connector {

    private USocketFactory factory;
    private String socket;

    public SSHSocketAgentConnector( USocketFactory factory, String socket ) throws AgentProxyException {
        
        this.factory = factory;
        this.socket  = socket;

        USocketFactory.Socket sock = null;
        
        try {
        
            sock = open();
        } catch (IOException e) {
            
            throw new AgentProxyException(e.toString());
        } catch (Exception e) {
            
            throw new AgentProxyException(e.toString());
        } finally {
            
            try {
                if (sock != null)
                    sock.close();
            } catch (IOException e) {
                throw new AgentProxyException(e.toString());
            }
        }
    }

    public String getName() {
        return "ssh-agent";
    }

    public boolean isConnectorAvailable() {
        return this.socket != null;
    }

    public boolean isAvailable() {
        return isConnectorAvailable();
    }

    private USocketFactory.Socket open() throws IOException {

        if ( this.socket == null ) {
            throw new IOException("SSH_AUTH_SOCK is not defined.");
        }
        
        return factory.open( this.socket );
    }
    

    public void query(Buffer buffer) throws AgentProxyException {
        
        USocketFactory.Socket sock = null;
        
        try {
            sock = open();
            sock.write(buffer.buffer, 0, buffer.getLength());
            buffer.rewind();
            int i = sock.readFull(buffer.buffer, 0, 4);
            i = buffer.getInt();
            buffer.rewind();
            buffer.checkFreeSize(i);
            i = sock.readFull(buffer.buffer, 0, i);
        
        } catch (IOException e) {
        
            throw new AgentProxyException(e.toString());
        
        } finally {
         
            try {
                if (sock != null)
                    sock.close();
            } catch (IOException e) {
                throw new AgentProxyException(e.toString());
            }
        }
    }
}
