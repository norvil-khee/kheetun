package org.khee.kheetun.server.daemon;

import org.khee.kheetun.client.config.Tunnel;

import com.jcraft.jsch.Session;

public interface PingDaemonListener {

    public void PingFailed( PingDaemon daemon, Tunnel tunnel, Session session );
    public void PingUpdate( Tunnel tunnel, long ping );
}
