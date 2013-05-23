package org.jenkinsci.plugins.iosbuilder;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
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
    private final FilePath projectRootPath;
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

    iOSBuilderExecutor(AbstractBuild build, Launcher launcher, BuildListener listener, FilePath projectRootPath, String podPath, String securityPath, String xcodebuildPath, String xcrunPath) throws Exception {
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        this.projectRootPath = projectRootPath;
        this.podPath = podPath;
        this.securityPath = securityPath;
        this.xcodebuildPath = xcodebuildPath;
        this.xcrunPath = xcrunPath;
        try {
            envVars = build.getEnvironment(listener);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Could not get BuildListener environment");
        }
    }

    int installPods() throws Exception {
        try {
            String action = projectRootPath.child("Podfile.lock").exists() ? "update" : "install";
            return launcher.launch().envs(envVars).cmds(podPath, action).stdout(listener).pwd(projectRootPath).join();
        }
        catch (Exception e) {
            e.printStackTrace();
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
                launcher.launch().envs(envVars).cmds(securityPath, "create-keychain", "-p", keychainPassword, keychainName).stdout(listener).join();
                launcher.launch().envs(envVars).cmds(securityPath, "import", identityPath, "-k", keychainName, "-P", identityPassword, "-A").stdout(listener).join();
                launcher.launch().envs(envVars).cmds(securityPath, "unlock-keychain", "-p", keychainPassword, keychainName).stdout(listener).join();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.mobileprovision = mobileprovision;

            String mobileprovisionPath = "Library/MobileDevice/Provisioning Profiles/" + mobileprovision.getUUID() + ".mobileprovision";
            mobileprovisionFilePath = new FilePath(new File(envVars.get("HOME"), mobileprovisionPath));
            //TODO: set the flag which shows that we'll need to delete mobileprovision
            mobileprovisionFilePath.write().write(mobileprovision.getBytes());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    int runXcodebuild(String xcworkspacePath, String xcodeprojPath, String target, String scheme, String configuration, String sdk, String additionalParameters, boolean codeSign) {
        try {
            ArrayList<String> buildCommand = new ArrayList<String>();
            buildCommand.add(xcodebuildPath);
            if (xcworkspacePath != null && !xcworkspacePath.isEmpty()) {
                buildCommand.add("-workspace");
                buildCommand.add(new File(xcworkspacePath).getName());
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
                buildCommand.add(additionalParameters);
            }
            if (codeSign) {
                if (mobileprovision != null) {
                    buildCommand.add("PROVISIONING_PROFILE="+ mobileprovision.getUUID());
                }
                if (identity != null) {
                    buildCommand.add("CODE_SIGN_IDENTITY=" + identity.getCommonName());
                    String keychainPath = envVars.get("HOME").replaceAll("/$", "") + "/Library/Keychains/" + keychainName;
                    buildCommand.add("OTHER_CODE_SIGN_FLAGS=--keychain " + keychainPath);
                }
            }
            buildPath = new FilePath(new File("/tmp")).createTempDir("build", "").getRemote();
            buildCommand.add("CONFIGURATION_BUILD_DIR="+ buildPath);
            return launcher.launch().envs(envVars).cmds(buildCommand).stdout(listener).pwd(projectRootPath).join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    int buildIpa() {
        try {
            List<FilePath> filePaths = new FilePath(new File(buildPath)).list();
            for (Iterator<FilePath> iterator = filePaths.iterator(); iterator.hasNext(); ) {
                FilePath filePath = iterator.next();
                if (filePath.isDirectory() && filePath.getName().endsWith("app")) {
                    launcher.launch().envs(envVars).cmds(xcrunPath, "-sdk", "iphoneos", "PackageApplication", "-v", filePath.getName(), "--sign", identity.getCommonName(), "--embed", mobileprovisionFilePath.getRemote(), "-o", filePath.getRemote().replaceAll("app$", "ipa")).stdout(listener).pwd(buildPath).join();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    void collectArtifacts(String artifactsTemplate) {
        try {
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
            e.printStackTrace();
        }
    }

    void cleanup() {
        try {
            new FilePath(new File(identityPath)).delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            launcher.launch().envs(envVars).cmds(securityPath, "delete-keychain", keychainName).stdout(listener).join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileNameWithTemplate(FilePath filePath, String artifactsTemplate, String extensionRegex) {
        String fileBasename = filePath.getName().replaceAll(extensionRegex, "");
        String newFileBasename = artifactsTemplate.replaceAll("\\$APP_NAME", fileBasename);
        return filePath.getName().replaceFirst(fileBasename, newFileBasename);
    }
}
