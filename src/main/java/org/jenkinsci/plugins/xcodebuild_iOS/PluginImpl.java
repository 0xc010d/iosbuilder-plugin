package org.jenkinsci.plugins.xcodebuild_iOS;

import hudson.Plugin;
import java.util.logging.Logger;
import org.kohsuke.stapler.StaplerRequest;
import net.sf.json.JSONObject;
import hudson.model.Descriptor;
import javax.servlet.ServletException;
import java.io.IOException;

public class PluginImpl extends Plugin {
    public void start() throws Exception {
        Logger.getLogger(PluginImpl.class.getName()).info("XcodebuildIOS plugin loaded");
    }
}
