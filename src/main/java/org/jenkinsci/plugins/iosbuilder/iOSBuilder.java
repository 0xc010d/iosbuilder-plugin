package org.jenkinsci.plugins.iosbuilder;

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
import org.jenkinsci.plugins.iosbuilder.signing.Mobileprovision;
import org.jenkinsci.plugins.iosbuilder.signing.MobileprovisionFactory;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12Archive;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12ArchiveFactory;
import org.kohsuke.stapler.*;

public class iOSBuilder extends Builder {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private final boolean doInstallPods;
    private final String xcworkspacePath;
    private final String xcodeprojPath;
    private final String target;
    private final String scheme;
    private final String configuration;
    private final String sdk;
    private final String additionalParameters;
    private final boolean doSign;
    private final String pkcs12ArchiveData;
    private final String pkcs12ArchivePassword;
    private final String mobileprovisionData;
    private final boolean doBuildIPA;

    @DataBoundConstructor
    public iOSBuilder(boolean doInstallPods, String xcworkspacePath, String xcodeprojPath, String target, String scheme, String configuration, String sdk, String additionalParameters, CodeSign codeSign) {
        this.doInstallPods = doInstallPods;
        this.xcworkspacePath = xcworkspacePath;
        this.xcodeprojPath = xcodeprojPath;
        this.target = target;
        this.scheme = scheme;
        this.configuration = configuration;
        this.sdk = sdk;
        this.additionalParameters = additionalParameters;
        this.doSign = codeSign != null;
        this.pkcs12ArchiveData = this.doSign ? codeSign.pkcs12ArchiveData : null;
        this.pkcs12ArchivePassword = this.doSign ? codeSign.pkcs12ArchivePassword : null;
        this.mobileprovisionData = this.doSign ? codeSign.mobileprovisionData : null;
        this.doBuildIPA = this.doSign && codeSign.doBuildIPA;
    }

    public boolean isDoInstallPods() { return doInstallPods; }
    public String getXcworkspacePath() { return xcworkspacePath; }
    public String getXcodeprojPath() { return xcodeprojPath; }
    public String getTarget() { return target; }
    public String getScheme() { return scheme; }
    public String getConfiguration() { return configuration; }
    public String getSdk() { return sdk; }
    public String getAdditionalParameters() { return additionalParameters; }
    public boolean isDoSign() { return doSign; }
    public String getPkcs12ArchiveData() { return pkcs12ArchiveData; }
    public String getPkcs12ArchivePassword() { return pkcs12ArchivePassword; }
    public String getMobileprovisionData() { return mobileprovisionData; }
    public boolean isDoBuildIPA() { return doBuildIPA; }

    public static final class CodeSign {
        private final String pkcs12ArchiveData;
        private final String pkcs12ArchivePassword;
        private final String mobileprovisionData;
        private final boolean doBuildIPA;

        @DataBoundConstructor
        public CodeSign(String pkcs12ArchiveFile, String mobileprovisionFile, String pkcs12ArchiveData, String pkcs12ArchivePassword, String mobileprovisionData, boolean doBuildIPA) {
            this.pkcs12ArchiveData = pkcs12ArchiveData;
            this.pkcs12ArchivePassword = pkcs12ArchivePassword;
            this.mobileprovisionData = mobileprovisionData;
            this.doBuildIPA = doBuildIPA;
        }
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            boolean result = true;
            iOSBuilderExecutor executor = new iOSBuilderExecutor(build, launcher, listener, build.getWorkspace(), getDescriptor().getPodPath(), getDescriptor().getSecurityPath(), getDescriptor().getXcodebuildPath(), getDescriptor().getXcrunPath());
            if (doInstallPods) {
                result = executor.installPods() == 0;
            }
            if (result && doSign) {
                PKCS12Archive pkcs12Archive = PKCS12ArchiveFactory.newInstance(pkcs12ArchiveData, pkcs12ArchivePassword);
                Mobileprovision mobileprovision = MobileprovisionFactory.newInstance(mobileprovisionData);
                result = executor.installIdentity(pkcs12Archive, mobileprovision) == 0;
            }
            if (result) {
                result = executor.runXcodebuild(xcworkspacePath, xcodeprojPath, target, scheme, configuration, sdk, additionalParameters, doSign) == 0;
            }
            if (result && doBuildIPA) {
                result = executor.buildIpa() == 0;
            }
            executor.collectArtifacts();
            executor.cleanup();
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BuildStepDescriptorImpl getDescriptor() {
        return (BuildStepDescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class BuildStepDescriptorImpl extends BuildStepDescriptor<Builder> {
        private static final String DEFAULT_XCODEBUILD_PATH = "/usr/bin/xcodebuild";
        private static final String DEFAULT_SECURITY_PATH = "/usr/bin/security";
        private static final String DEFAULT_XCRUN_PATH = "/usr/bin/xcrun";
        private static final String DEFAULT_POD_PATH = "/usr/bin/pod";

        private String xcodebuildPath;
        private String securityPath;
        private String xcrunPath;
        private String podPath;

        private FormValidation checkPath(@QueryParameter String path, String name) throws IOException, ServletException {
            if (path.length() == 0) {
                return FormValidation.error("Please set " + name + " path");
            }
            return FormValidation.ok();
        }

        public void doUploadForm(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            rsp.setContentType("text/html");
            req.getView(iOSBuilder.class, "uploadForm.jelly").forward(req, rsp);
        }

        public void doUpload(StaplerRequest req, StaplerResponse rsp, @QueryParameter String job, @QueryParameter String name) throws IOException, ServletException {
            FileItem fileItem = req.getFileItem("file");
            if (fileItem == null) {
                throw new ServletException("File was not uploaded");
            }
            doUploadForm(req, rsp);
        }

        public FormValidation doCheckXcodebuildPath(@QueryParameter String value) throws IOException, ServletException {
            return checkPath(value, "xcodebuild");
        }
        public FormValidation doCheckSecurityPath(@QueryParameter String value) throws IOException, ServletException {
            return checkPath(value, "security");
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
            xcrunPath = formData.getString("xcrunPath");
            podPath = formData.getString("podPath");
            return super.configure(req, formData);
        }

        public String getXcodebuildPath() { return xcodebuildPath != null ? xcodebuildPath : DEFAULT_XCODEBUILD_PATH; }
        public String getSecurityPath() { return securityPath != null ? securityPath : DEFAULT_SECURITY_PATH; }
        public String getXcrunPath() { return xcrunPath != null ? xcrunPath : DEFAULT_XCRUN_PATH; }
        public String getPodPath() { return podPath != null ? podPath : DEFAULT_POD_PATH; }
    }
}
