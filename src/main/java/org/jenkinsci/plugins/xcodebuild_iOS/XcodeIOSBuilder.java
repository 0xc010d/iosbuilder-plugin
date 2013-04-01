package org.jenkinsci.plugins.xcodebuild_iOS;

import java.util.Arrays;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.Hudson;
import hudson.model.Item;
import org.apache.commons.fileupload.FileItem;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import java.lang.Thread;
import org.apache.commons.codec.binary.Base64;

public class XcodeIOSBuilder extends Builder {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private final String xcworkspacePath;
    private final String xcodeprojPath;
    private final String target;
    private final String scheme;
    private final String configuration;
    private final String additionalParameters;
    private final String sdk;
    private final String key;
    private final String keyPassword;
    private final String certificate;
    private final String mobileprovision;
    private final boolean buildIPA;

    @DataBoundConstructor
    public XcodeIOSBuilder(String xcworkspacePath, String xcodeprojPath, String target, String scheme, String configuration, String additionalParameters, String sdk, String key, String keyPassword, String certificate, String mobileprovision, boolean buildIPA) {
        this.xcworkspacePath = xcworkspacePath;
        this.xcodeprojPath = xcodeprojPath;
        this.target = target;
        this.scheme = scheme;
        this.configuration = configuration;
        this.additionalParameters = additionalParameters;
        this.sdk = sdk;
        this.key = key;
        this.keyPassword = keyPassword;
        this.certificate = certificate;
        this.mobileprovision = mobileprovision;
        this.buildIPA = buildIPA;
    }

    public String getXcworkspacePath() { return xcworkspacePath; }
    public String getXcodeprojPath() { return xcodeprojPath; }
    public String getTarget() { return target; }
    public String getScheme() { return scheme; }
    public String getConfiguration() { return configuration; }
    public String getAdditionalParameters() { return additionalParameters; }
    public String getSdk() { return sdk; }
    public String getKey() { return key; }
    public String getKeyPassword() { return keyPassword; }
    public String getCertificate() { return certificate; }
    public String getMobileprovision() { return mobileprovision; }
    public boolean getBuildIPA() { return buildIPA; }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        String xcodebuild = getDescriptor().getXcodebuildPath();
        String security = getDescriptor().getSecurityPath();
        String openssl = getDescriptor().getOpensslPath();
        String xcrun = getDescriptor().getXcrunPath();

        listener.getLogger().println("Hello!");

        return true;
    }

    @Override
    public BuildStepDescriptorImpl getDescriptor() {
        return (BuildStepDescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class BuildStepDescriptorImpl extends BuildStepDescriptor<Builder> {
        private static final String DEFAULT_XCODEBUILD_PATH = "/usr/bin/xcodebuild";
        private static final String DEFAULT_SECURITY_PATH = "/usr/bin/security";
        private static final String DEFAULT_OPENSSL_PATH = "/usr/bin/openssl";
        private static final String DEFAULT_XCRUN_PATH = "/usr/bin/xcrun";

        private String xcodebuildPath;
        private String securityPath;
        private String opensslPath;
        private String xcrunPath;

        private void processFile(StaplerRequest req, JSONObject formData, String name, String key) throws FormException {
            FileItem file = null;
            try {
                file = req.getFileItem(name);
            }
            catch (Exception e) {}

            if (file != null && file.getSize() != 0) {
                byte[] data = file.get();
                byte[] encodedData = Base64.encodeBase64(data);
                formData.put(key, new String(encodedData));
            }
        }

        @Override
        public XcodeIOSBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            processFile(req, formData, (String)formData.get("keyFile"), "key");
            processFile(req, formData, (String)formData.get("certificateFile"), "certificate");
            processFile(req, formData, (String)formData.get("mobileprovisionFile"), "mobileprovision");
            return (XcodeIOSBuilder)super.newInstance(req, formData);
        }

        private FormValidation doCheckPath(@QueryParameter String value, String name) throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set " + name + " path");
            }
            File file = new File(value);
            if (!file.isFile()) {
                return FormValidation.error("Please set correct " + name + " path");
            }
            if (!file.canExecute()) {
                return FormValidation.error("Please set executable path for " + name);
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckXcodebuildPath(@QueryParameter String value) throws IOException, ServletException {
            return doCheckPath(value, "xcodebuild");
        }
        public FormValidation doCheckSecurityPath(@QueryParameter String value) throws IOException, ServletException {
            return doCheckPath(value, "security");
        }
        public FormValidation doCheckOpensslPath(@QueryParameter String value) throws IOException, ServletException {
            return doCheckPath(value, "openssl");
        }
        public FormValidation doCheckXcrunPath(@QueryParameter String value) throws IOException, ServletException {
            return doCheckPath(value, "xcrun");
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "xcodebuild-iOS";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            xcodebuildPath = formData.getString("xcodebuildPath");
            securityPath = formData.getString("securityPath");
            opensslPath = formData.getString("opensslPath");
            xcrunPath = formData.getString("xcrunPath");
            save();
            return super.configure(req, formData);
        }

        public String getXcodebuildPath() { return xcodebuildPath != null ? xcodebuildPath : getDefaultXcodebuildPath(); }
        public String getSecurityPath() { return securityPath != null ? securityPath : getDefaultSecurityPath(); }
        public String getOpensslPath() { return opensslPath != null ? opensslPath : getDefaultOpensslPath(); }
        public String getXcrunPath() { return xcrunPath != null ? xcrunPath : getDefaultXcrunPath(); }

        public String getDefaultXcodebuildPath() { return DEFAULT_XCODEBUILD_PATH; }
        public String getDefaultSecurityPath() { return DEFAULT_SECURITY_PATH; }
        public String getDefaultOpensslPath() { return DEFAULT_OPENSSL_PATH; }
        public String getDefaultXcrunPath() { return DEFAULT_XCRUN_PATH; }
    }
}
