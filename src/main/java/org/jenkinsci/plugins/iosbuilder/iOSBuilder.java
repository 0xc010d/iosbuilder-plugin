package org.jenkinsci.plugins.iosbuilder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

public class iOSBuilder extends Builder {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private final boolean usePods;
    private final String xcworkspacePath;
    private final String xcodeprojPath;
    private final String target;
    private final String scheme;
    private final String configuration;
    private final String additionalParameters;
    private final String sdk;
    private final boolean codeSign;
    private final String key;
    private final String password;
    private final String certificate;
    private final String mobileprovision;
    private final boolean buildIPA;

    @DataBoundConstructor
    public iOSBuilder(boolean usePods, String xcworkspacePath, String xcodeprojPath, String target, String scheme, String configuration, String additionalParameters, String sdk, CodeSign codeSign) {
        this.usePods = usePods;
        this.xcworkspacePath = xcworkspacePath;
        this.xcodeprojPath = xcodeprojPath;
        this.target = target;
        this.scheme = scheme;
        this.configuration = configuration;
        this.additionalParameters = additionalParameters;
        this.sdk = sdk;
        this.codeSign = codeSign != null;
        this.key = this.codeSign ? codeSign.key : null;
        this.password = this.codeSign ? codeSign.password : null;
        this.certificate = this.codeSign ? codeSign.certificate : null;
        this.mobileprovision = this.codeSign ? codeSign.mobileprovision : null;
        this.buildIPA = this.codeSign ? codeSign.buildIPA : false;
    }

    public boolean getUsePods() { return usePods; };
    public String getXcworkspacePath() { return xcworkspacePath; }
    public String getXcodeprojPath() { return xcodeprojPath; }
    public String getTarget() { return target; }
    public String getScheme() { return scheme; }
    public String getConfiguration() { return configuration; }
    public String getAdditionalParameters() { return additionalParameters; }
    public String getSdk() { return sdk; }
    public boolean getCodeSign() { return codeSign; }
    public String getKey() { return key; }
    public String getPassword() { return password; }
    public String getCertificate() { return certificate; }
    public String getMobileprovision() { return mobileprovision; }
    public boolean getBuildIPA() { return buildIPA; }

    public static final class CodeSign {
        private final String key;
        private final String password;
        private final String certificate;
        private final String mobileprovision;
        private final boolean buildIPA;

        @DataBoundConstructor
        public CodeSign(String key, String password, String certificate, String mobileprovision, boolean buildIPA) {
            this.key = key;
            this.password = password;
            this.certificate = certificate;
            this.mobileprovision = mobileprovision;
            this.buildIPA = buildIPA;
        }

        public String getKey() { return key; }
        public String getPassword() { return password; }
        public String getCertificate() { return certificate; }
        public String getMobileprovision() { return mobileprovision; }
        public boolean getBuildIPA() { return buildIPA; }
    }

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
        private static final String DEFAULT_POD_PATH = "/usr/bin/pod";

        private String xcodebuildPath;
        private String securityPath;
        private String opensslPath;
        private String xcrunPath;
        private String podPath;

        private void processFile(StaplerRequest req, JSONObject formData, String fileKey, String valueKey) throws FormException {
            FileItem file = null;
            try {
                file = req.getFileItem((String)formData.get(fileKey));
            }
            catch (Exception e) {}

            if (file != null && file.getSize() != 0) {
                byte[] data = file.get();
                byte[] encodedData = Base64.encodeBase64(data);
                formData.put(valueKey, new String(encodedData));
            }
        }

        @Override
        public iOSBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            JSONObject codeSign = (JSONObject)formData.get("codeSign");
            if (codeSign != null) {
                processFile(req, codeSign, "keyFile", "key");
                processFile(req, codeSign, "certificateFile", "certificate");
                processFile(req, codeSign, "mobileprovisionFile", "mobileprovision");
                formData.put("codeSign", codeSign);
            }
            return (iOSBuilder)super.newInstance(req, formData);
        }

        private FormValidation checkPath(@QueryParameter String path, String name) throws IOException, ServletException {
            if (path.length() == 0) {
                return FormValidation.error("Please set " + name + " path");
            }
            File file = new File(path);
            if (!file.isFile()) {
                return FormValidation.error("Please set correct " + name + " path");
            }
            if (!file.canExecute()) {
                return FormValidation.error("Please set executable path for " + name);
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckXcodebuildPath(@QueryParameter String value) throws IOException, ServletException {
            return checkPath(value, "xcodebuild");
        }
        public FormValidation doCheckSecurityPath(@QueryParameter String value) throws IOException, ServletException {
            return checkPath(value, "security");
        }
        public FormValidation doCheckOpensslPath(@QueryParameter String value) throws IOException, ServletException {
            return checkPath(value, "openssl");
        }
        public FormValidation doCheckXcrunPath(@QueryParameter String value) throws IOException, ServletException {
            return checkPath(value, "xcrun");
        }
        public FormValidation doCheckPodPath(@QueryParameter String value) throws IOException, ServletException {
            return checkPath(value, "pod");
        }

        // TODO: Get available SDKs list from xcodebuild
        public ListBoxModel doFillSdkItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("iOS SDK", "iphoneos");
            items.add("iOS Simulator SDK", "iphonesimulator");
            return items;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "iOS builder";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            xcodebuildPath = formData.getString("xcodebuildPath");
            securityPath = formData.getString("securityPath");
            opensslPath = formData.getString("opensslPath");
            xcrunPath = formData.getString("xcrunPath");
            podPath = formData.getString("podPath");
            return super.configure(req, formData);
        }

        public String getXcodebuildPath() { return xcodebuildPath != null ? xcodebuildPath : DEFAULT_XCODEBUILD_PATH; }
        public String getSecurityPath() { return securityPath != null ? securityPath : DEFAULT_SECURITY_PATH; }
        public String getOpensslPath() { return opensslPath != null ? opensslPath : DEFAULT_OPENSSL_PATH; }
        public String getXcrunPath() { return xcrunPath != null ? xcrunPath : DEFAULT_XCRUN_PATH; }
        public String getPodPath() { return podPath != null ? podPath : DEFAULT_POD_PATH; }
    }
}
