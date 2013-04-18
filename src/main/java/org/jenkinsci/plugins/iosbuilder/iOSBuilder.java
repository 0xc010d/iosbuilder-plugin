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
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.jenkinsci.plugins.iosbuilder.signing.Certificate;
import org.jenkinsci.plugins.iosbuilder.signing.Mobileprovision;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12Archive;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import sun.misc.BASE64Encoder;

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
    private final String pkcs12ArchiveData;
    private final String pkcs12ArchivePassword;
    private final String certificateData;
    private final String mobileprovisionData;
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
        this.pkcs12ArchiveData = this.codeSign ? codeSign.pkcs12ArchiveData : null;
        this.pkcs12ArchivePassword = this.codeSign ? codeSign.pkcs12ArchivePassword : null;
        this.certificateData = this.codeSign ? codeSign.certificateData : null;
        this.mobileprovisionData = this.codeSign ? codeSign.mobileprovisionData : null;
        this.buildIPA = this.codeSign ? codeSign.buildIPA : false;
    }

    public boolean isUsePods() { return usePods; }
    public String getXcworkspacePath() { return xcworkspacePath; }
    public String getXcodeprojPath() { return xcodeprojPath; }
    public String getTarget() { return target; }
    public String getScheme() { return scheme; }
    public String getConfiguration() { return configuration; }
    public String getAdditionalParameters() { return additionalParameters; }
    public String getSdk() { return sdk; }
    public boolean isCodeSign() { return codeSign; }
    public String getPkcs12ArchiveData() { return pkcs12ArchiveData; }
    public String getPkcs12ArchivePassword() { return pkcs12ArchivePassword; }
    public String getCertificateData() { return certificateData; }
    public String getMobileprovisionData() { return mobileprovisionData; }
    public boolean isBuildIPA() { return buildIPA; }

    public static final class CodeSign {
        private final String pkcs12ArchiveData;
        private final String pkcs12ArchivePassword;
        private final String certificateData;
        private final String mobileprovisionData;
        private final boolean buildIPA;

        @DataBoundConstructor
        public CodeSign(String pkcs12ArchiveData, String pkcs12ArchivePassword, String certificateData, String mobileprovisionData, boolean buildIPA) {
            this.pkcs12ArchiveData = pkcs12ArchiveData;
            this.pkcs12ArchivePassword = pkcs12ArchivePassword;
            this.certificateData = certificateData;
            this.mobileprovisionData = mobileprovisionData;
            this.buildIPA = buildIPA;
        }
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        PKCS12Archive pkcs12Archive = PKCS12Archive.getInstance(this.pkcs12ArchiveData, this.pkcs12ArchivePassword);
        Mobileprovision mobileprovision = Mobileprovision.getInstance(this.mobileprovisionData);
        Certificate certificate = pkcs12Archive.chooseCertificate(mobileprovision);
        if (certificate != null) {
            listener.getLogger().println(certificate.getCommonName() + " / " + certificate.getExpirationDate());
        }

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
            try {
                FileItem file = req.getFileItem((String)formData.get(fileKey));
                if (file.getSize() != 0) {
                    formData.put(valueKey, new BASE64Encoder().encode(file.get()));
                }
            }
            catch (Exception e) {}
        }

        @Override
        public iOSBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            JSONObject codeSign = (JSONObject)formData.get("codeSign");
            if (codeSign != null) {
                processFile(req, codeSign, "pkcs12ArchiveFile", "pkcs12ArchiveData");
                processFile(req, codeSign, "certificateFile", "certificateData");
                processFile(req, codeSign, "mobileprovisionFile", "mobileprovisionData");
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
