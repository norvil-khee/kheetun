package org.khee.kheetun.client.config;

public interface ConfigManagerListener {
    
    public void configManagerConfigChanged( Config oldConfig, Config newConfig, boolean valid );
    public void configManagerGlobalConfigChanged( GlobalConfig oldConfig, GlobalConfig newConfig, boolean valid );
}
