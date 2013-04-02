package org.jenkinsci.plugins.iosbuilder;

import java.util.logging.Logger;
import hudson.Plugin;

public class PluginImpl extends Plugin {
    public void start() throws Exception {
        Logger.getLogger(PluginImpl.class.getName()).info("iOSBuilder plugin loaded");
    }
}
