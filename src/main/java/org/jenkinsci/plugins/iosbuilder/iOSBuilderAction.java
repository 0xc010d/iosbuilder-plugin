package org.jenkinsci.plugins.iosbuilder;

import hudson.model.Action;

public class iOSBuilderAction implements Action {
    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "iOS Builder";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUrlName() {
        return "iosbuilder";
    }
}
