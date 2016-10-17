package org.khee.kheetun.client.config;

import java.util.ArrayList;

public interface ConfigManagerListener {
    
    public void configManagerConfigChanged( Config config );
    public void configManagerConfigInvalid( Config config, ArrayList<String> errorStack );
    public void configManagerConfigValid( Config config );
}
