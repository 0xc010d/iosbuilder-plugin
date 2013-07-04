package org.jenkinsci.plugins.iosbuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.EnvironmentContributingAction;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.jenkinsci.plugins.iosbuilder.signing.Mobileprovision;
import org.jenkinsci.plugins.iosbuilder.signing.MobileprovisionFactory;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12Archive;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12ArchiveFactory;
import org.kohsuke.stapler.*;

public class iOSBuilder extends Builder {
    private final boolean doInstallPods;
    private final String xcworkspacePath;
    private final String xcodeprojPath;
    private final String target;
    private final String scheme;
    private final String configuration;
    private final String sdk;
    private final String buildDirectory;
    private final String additionalParameters;
    private final boolean doSign;
    private final String pkcs12ArchiveData;
    private final Secret pkcs12ArchivePassword;
    private final String mobileprovisionData;
    private final boolean doBuildIPA;
    private final String ipaNameTemplate;
    private final boolean doZipDSYM;
    private final String dSYMNameTemplate;

    @DataBoundConstructor
    public iOSBuilder(boolean doInstallPods, String xcworkspacePath, String xcodeprojPath, String target, String scheme, String configuration, String sdk, String buildDirectory, String additionalParameters, CodeSign codeSign, boolean doZipDSYM, String dSYMNameTemplate) {
        this.doInstallPods = doInstallPods;
        this.xcworkspacePath = xcworkspacePath;
        this.xcodeprojPath = xcodeprojPath;
        this.target = target;
        this.scheme = scheme;
        this.configuration = configuration;
        this.sdk = sdk;
        this.buildDirectory = buildDirectory;
        this.additionalParameters = additionalParameters;
        this.doSign = codeSign != null;
        this.pkcs12ArchiveData = this.doSign ? codeSign.pkcs12ArchiveData : null;
        this.pkcs12ArchivePassword = this.doSign ? codeSign.pkcs12ArchivePassword : null;
        this.mobileprovisionData = this.doSign ? codeSign.mobileprovisionData : null;
        this.doBuildIPA = this.doSign && codeSign.doBuildIPA;
        this.ipaNameTemplate = this.doSign ? codeSign.ipaNameTemplate : "$APP_NAME-$BUNDLE_VERSION";
        this.doZipDSYM = doZipDSYM;
        this.dSYMNameTemplate = doZipDSYM && !dSYMNameTemplate.isEmpty() ? dSYMNameTemplate : "$APP_NAME-$BUNDLE_VERSION";
    }

    public boolean isDoInstallPods() { return doInstallPods; }
    public String getXcworkspacePath() { return xcworkspacePath; }
    public String getXcodeprojPath() { return xcodeprojPath; }
    public String getTarget() { return target; }
    public String getScheme() { return scheme; }
    public String getConfiguration() { return configuration; }
    public String getSdk() { return sdk; }
    public String getBuildDirectory() { return buildDirectory; }
    public String getAdditionalParameters() { return additionalParameters; }
    public boolean isDoSign() { return doSign; }
    public String getPkcs12ArchiveData() { return pkcs12ArchiveData; }
    public Secret getPkcs12ArchivePassword() { return pkcs12ArchivePassword; }
    public String getMobileprovisionData() { return mobileprovisionData; }
    public boolean isDoBuildIPA() { return doBuildIPA; }
    public String getIpaNameTemplate() { return ipaNameTemplate; }
    public boolean isDoZipDSYM() { return doZipDSYM; }
    public String getdSYMNameTemplate() { return dSYMNameTemplate; }

    public static final class CodeSign {
        private final String pkcs12ArchiveData;
        private final Secret pkcs12ArchivePassword;
        private final String mobileprovisionData;
        private final boolean doBuildIPA;
        private final String ipaNameTemplate;

        @DataBoundConstructor
        public CodeSign(String pkcs12ArchiveFile, String mobileprovisionFile, String pkcs12ArchiveData, String pkcs12ArchivePassword, String mobileprovisionData, boolean doBuildIPA, String ipaNameTemplate) {
            this.pkcs12ArchiveData = pkcs12ArchiveData;
            this.pkcs12ArchivePassword = Secret.fromString(pkcs12ArchivePassword);
            this.mobileprovisionData = mobileprovisionData;
            this.doBuildIPA = doBuildIPA;
            this.ipaNameTemplate = doBuildIPA && !ipaNameTemplate.isEmpty() ? ipaNameTemplate : "$APP_NAME-$BUNDLE_VERSION";
        }
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            boolean result = true;
            String podPath = getDescriptor().getPodPath();
            String securityPath = getDescriptor().getSecurityPath();
            String xcodebuildPath = getDescriptor().getXcodebuildPath();
            String xcrunPath = getDescriptor().getXcrunPath();
            iOSBuilderExecutor executor = new iOSBuilderExecutor(podPath, securityPath, xcodebuildPath, xcrunPath, build, launcher, listener, buildDirectory);
            if (doInstallPods) {
                String projectRootPath = "";
                if (xcworkspacePath != null && !xcworkspacePath.isEmpty() && xcworkspacePath.lastIndexOf(File.separator) >= 0) {
                    projectRootPath = xcworkspacePath.substring(0, xcworkspacePath.lastIndexOf(File.separator));
                }
                else if (xcodeprojPath != null && !xcodeprojPath.isEmpty() && xcodeprojPath.lastIndexOf(File.separator) >= 0) {
                    projectRootPath = xcodeprojPath.substring(0, xcodeprojPath.lastIndexOf(File.separator));
                }
                result = executor.installPods(projectRootPath) == 0;
            }
            if (result && doSign) {
                PKCS12Archive pkcs12Archive = PKCS12ArchiveFactory.newInstance(pkcs12ArchiveData, pkcs12ArchivePassword);
                Mobileprovision mobileprovision = MobileprovisionFactory.newInstance(mobileprovisionData);
                result = executor.installIdentity(pkcs12Archive, mobileprovision) == 0;
            }
            if (result) {
                result = executor.runXcodebuild(xcworkspacePath, xcodeprojPath, target, scheme, configuration, sdk, additionalParameters, doSign) == 0;
            }
            if (result) {
                result = executor.exportInfo() == 0;
            }
            if (result) {
                build.addAction(new EnvironmentContributingActionImpl(executor.getExportedInfo()));
            }
            if (result && doBuildIPA) {
                result = executor.buildIpa(ipaNameTemplate) == 0;
            }
            if (result && doZipDSYM) {
                result = executor.zipDSYM(dSYMNameTemplate) == 0;
            }
            executor.cleanup();
            return result;
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return false;
        }
    }

    private class EnvironmentContributingActionImpl implements EnvironmentContributingAction {
        Map<String, String> exportedEnvVars;

        EnvironmentContributingActionImpl(Map<String, String> exportedEnvVars) {
            this.exportedEnvVars = exportedEnvVars;
        }
        public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
            env.putAll(exportedEnvVars);
        }
        public String getIconFileName() { return null; }
        public String getDisplayName() { return null; }
        public String getUrlName() { return null; }
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
