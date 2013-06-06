package org.jenkinsci.plugins.iosbuilder;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.QuotedStringTokenizer;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.iosbuilder.signing.Identity;
import org.jenkinsci.plugins.iosbuilder.signing.Mobileprovision;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12Archive;
import org.jenkinsci.plugins.iosbuilder.util.Zip;

import java.io.*;
import java.util.*;

public class iOSBuilderExecutor {
    private final AbstractBuild build;
    private final Launcher launcher;
    private final BuildListener listener;
    private final String podPath;
    private final String securityPath;
    private final String xcodebuildPath;
    private final String xcrunPath;
    private EnvVars envVars = null;
    private Identity identity;
    private String identityPath;
    private String identityPassword;
    private String keychainName;
    private String keychainPassword;
    private Mobileprovision mobileprovision;
    private FilePath mobileprovisionFilePath;
    private String buildPath;

    iOSBuilderExecutor(String podPath, String securityPath, String xcodebuildPath, String xcrunPath, AbstractBuild build, Launcher launcher, BuildListener listener, String buildDirectory) throws Exception {
        this.podPath = podPath;
        this.securityPath = securityPath;
        this.xcodebuildPath = xcodebuildPath;
        this.xcrunPath = xcrunPath;
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        try {
            envVars = build.getEnvironment(listener);
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            throw new Exception("Could not get BuildListener environment");
        }
        buildPath = new FilePath(this.build.getWorkspace(), envVars.expand(buildDirectory)).getRemote();
    }

    int installPods(String projectRootPath) throws Exception {
        try {
            FilePath rootPath = new FilePath(build.getWorkspace(), envVars.expand(projectRootPath));
            String action = rootPath.child("Podfile.lock").exists() ? "update" : "install";
            return launcher.launch().envs(envVars).cmds(podPath, action).stdout(listener).stderr(listener.getLogger()).pwd(rootPath).join();
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            throw new Exception("Can not install pods");
        }
    }

    int installIdentity(PKCS12Archive pkcs12Archive, Mobileprovision mobileprovision) {
        try {
            identity = pkcs12Archive.chooseIdentity(mobileprovision.getCertificates());
            if (identity != null) {
                FilePath filePath = new FilePath(new File("/tmp")).createTempFile("identity", ".p12");
                identityPassword = UUID.randomUUID().toString();
                identity.save(filePath.write(), identityPassword.toCharArray());
                identityPath = filePath.getRemote();

                //create a keychain, import identity
                keychainName = UUID.randomUUID().toString() + ".keychain";
                keychainPassword = UUID.randomUUID().toString();
                launcher.launch().envs(envVars).cmds(securityPath, "create-keychain", "-p", keychainPassword, keychainName).stdout(listener).stderr(listener.getLogger()).join();
                launcher.launch().envs(envVars).cmds(securityPath, "import", identityPath, "-k", keychainName, "-P", identityPassword, "-A").stdout(listener).stderr(listener.getLogger()).join();
                launcher.launch().envs(envVars).cmds(securityPath, "unlock-keychain", "-p", keychainPassword, keychainName).stdout(listener).stderr(listener.getLogger()).join();
            }
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
        try {
            this.mobileprovision = mobileprovision;

            String mobileprovisionPath = "Library/MobileDevice/Provisioning Profiles/" + mobileprovision.getUUID() + ".mobileprovision";
            mobileprovisionFilePath = new FilePath(new File(envVars.get("HOME"), mobileprovisionPath));
            //TODO: set the flag which shows that we'll need to delete mobileprovision
            mobileprovisionFilePath.write().write(mobileprovision.getBytes());
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
        return 0;
    }

    int runXcodebuild(String xcworkspacePath, String xcodeprojPath, String target, String scheme, String configuration, String sdk, String additionalParameters, boolean codeSign) {
        try {
            ArrayList<String> buildCommand = new ArrayList<String>();
            buildCommand.add(xcodebuildPath);
            if (xcworkspacePath != null && !xcworkspacePath.isEmpty()) {
                buildCommand.add("-workspace");
                buildCommand.add(xcworkspacePath);
            }
            if (xcodeprojPath != null && !xcodeprojPath.isEmpty()) {
                buildCommand.add("-project");
                buildCommand.add(xcodeprojPath);
            }
            if (target != null && !target.isEmpty()) {
                buildCommand.add("-target");
                buildCommand.add(target);
            }
            if (scheme != null && !scheme.isEmpty()) {
                buildCommand.add("-scheme");
                buildCommand.add(scheme);
            }
            if (configuration != null && !configuration.isEmpty()) {
                buildCommand.add("-configuration");
                buildCommand.add(configuration);
            }
            buildCommand.add("-sdk");
            buildCommand.add(sdk);
            if (additionalParameters != null && !additionalParameters.isEmpty()) {
                for (String parameter : QuotedStringTokenizer.tokenize(envVars.expand(additionalParameters))) {
                    buildCommand.add(parameter);
                }
            }
            if (codeSign) {
                if (mobileprovision != null) {
                    buildCommand.add("PROVISIONING_PROFILE="+ mobileprovision.getUUID());
                }
                if (identity != null) {
                    buildCommand.add("CODE_SIGN_IDENTITY=" + identity.getCommonName());
                    buildCommand.add("OTHER_CODE_SIGN_FLAGS=--keychain " + keychainName);
                }
            }
            buildCommand.add("CONFIGURATION_BUILD_DIR=" + buildPath);
            return launcher.launch().envs(envVars).cmds(buildCommand).stdout(listener).stderr(listener.getLogger()).pwd(build.getWorkspace()).join();
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
        }
        return 0;
    }

    int buildIpa() {
        try {
            List<FilePath> filePaths = new FilePath(new File(buildPath)).list();
            for (Iterator<FilePath> iterator = filePaths.iterator(); iterator.hasNext(); ) {
                FilePath filePath = iterator.next();
                if (filePath.isDirectory() && filePath.getName().endsWith("app")) {
                    launcher.launch().envs(envVars).cmds(xcrunPath, "-sdk", "iphoneos", "PackageApplication", "-v", filePath.getName(), "--sign", identity.getCommonName(), "--embed", mobileprovisionFilePath.getRemote(), "-o", filePath.getRemote().replaceAll("app$", "ipa")).stdout(listener).stderr(listener.getLogger()).pwd(buildPath).join();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
        return 0;
    }

    int archiveArtifacts(String artifactsTemplate) {
        try {
            artifactsTemplate = envVars.expand(artifactsTemplate);
            List<FilePath> filePaths = new FilePath(new File(buildPath)).list();
            for (Iterator<FilePath> iterator = filePaths.iterator(); iterator.hasNext(); ) {
                FilePath filePath = iterator.next();
                if (filePath.isDirectory() && filePath.getName().endsWith(".app.dSYM")) {
                    String fileName = getFileNameWithTemplate(filePath, artifactsTemplate, "\\.app\\.dSYM$");
                    File zipFile = new File(build.getArtifactsDir(), fileName + ".zip");
                    Zip.archive(filePath, zipFile);
                }
                if (!filePath.isDirectory() && filePath.getName().endsWith(".ipa")) {
                    String fileName = getFileNameWithTemplate(filePath, artifactsTemplate, "\\.ipa$");
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(build.getArtifactsDir(), fileName));
                    IOUtils.copy(filePath.read(), fileOutputStream);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
        return 0;
    }

    void cleanup() {
        try {
            new FilePath(new File(identityPath)).delete();
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
        }
        try {
            launcher.launch().envs(envVars).cmds(securityPath, "delete-keychain", keychainName).stdout(listener).stderr(listener.getLogger()).join();
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
        }
    }

    private String getFileNameWithTemplate(FilePath filePath, String artifactsTemplate, String extensionRegex) {
        String fileBasename = filePath.getName().replaceAll(extensionRegex, "");
        String newFileBasename = artifactsTemplate.replaceAll("\\$APP_NAME", fileBasename);
        return filePath.getName().replaceFirst(fileBasename, newFileBasename);
    }
}
