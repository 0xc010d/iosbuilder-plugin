package org.jenkinsci.plugins.iosbuilder;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.apache.commons.io.FilenameUtils;
import org.jenkinsci.plugins.iosbuilder.signing.Identity;
import org.jenkinsci.plugins.iosbuilder.signing.Mobileprovision;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12Archive;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

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

    //TODO: rewrite to allow using slaves
    int installIdentity(PKCS12Archive pkcs12Archive, Mobileprovision mobileprovision) {
        try {
            identity = pkcs12Archive.chooseIdentity(mobileprovision.getCertificates());
            if (identity != null) {
                FilePath filePath = new FilePath(new File(envVars.get("TMPDIR"))).createTempFile("identity.", ".p12").absolutize();
                identityPassword = UUID.randomUUID().toString();
                identity.save(filePath.write(), identityPassword.toCharArray());
                identityPath = filePath.getRemote();

                //create a keychain, import identity
                keychainName = UUID.randomUUID().toString();
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
            FilePath mobileprovisionFilePath = new FilePath(new File(envVars.get("HOME"), mobileprovisionPath));
            //TODO: set the flag which shows that we'll need to delete mobileprovision
            mobileprovisionFilePath.write().write(mobileprovision.getBytes());

            Logger.getLogger(getClass().getName()).info(mobileprovisionFilePath.getRemote());
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
                    buildCommand.add("OTHER_CODE_SIGN_FLAGS=--keychain " + keychainName);
                }
            }
            return launcher.launch().envs(envVars).cmds(buildCommand).stdout(listener).pwd(projectRootPath).join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    int buildIpa() {
        return 0;
    }

    int cleanup() {
        try {
            new FilePath(new File(identityPath)).delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
