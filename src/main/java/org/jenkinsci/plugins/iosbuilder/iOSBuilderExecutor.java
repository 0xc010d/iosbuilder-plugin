package org.jenkinsci.plugins.iosbuilder;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.iosbuilder.signing.Identity;
import org.jenkinsci.plugins.iosbuilder.signing.Mobileprovision;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12Archive;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
            buildPath = new FilePath(new File(envVars.get("TMPDIR"))).createTempDir(UUID.randomUUID().toString(), "").absolutize().getRemote();
            buildCommand.add("CONFIGURATION_BUILD_DIR="+ buildPath);
            int result = launcher.launch().envs(envVars).cmds(buildCommand).stdout(listener).pwd(projectRootPath).join();
            if (result == 0) {
                result = collectBuildArtifacts();
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int collectBuildArtifacts() {
        try {
            List<FilePath> filePaths = new FilePath(new File(buildPath)).list();
            for (Iterator<FilePath> iterator = filePaths.iterator(); iterator.hasNext(); ) {
                FilePath filePath = iterator.next();
                if (filePath.isDirectory() && filePath.getName().endsWith("dSYM")) {
                    File zipFile = new File(build.getArtifactsDir(), filePath.getName() + ".zip");
                    zip(filePath, zipFile);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1;
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
        try {
            launcher.launch().envs(envVars).cmds(securityPath, "delete-keychain", keychainName).stdout(listener).join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void zip(FilePath directory, File zipFile) throws Exception {
        zipFile.getParentFile().mkdirs();
        zipFile.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        try {
            zip(directory, "", zipOutputStream);
        }
        catch (Exception e) {
            zipOutputStream.close();
        }
    }

    private void zip(FilePath directory, String name, ZipOutputStream zipOutputStream) throws Exception {
        byte[] buffer = new byte[1024];
        List<FilePath> children = directory.list();
        for (Iterator<FilePath> iterator = children.iterator(); iterator.hasNext();) {
            FilePath child = iterator.next();
            String fileName = (name.isEmpty() || name.endsWith("/") ? name : name + "/") + child.getName();
            if (child.isDirectory()) {
                zip(child, fileName, zipOutputStream);
            }
            else {
                zipOutputStream.putNextEntry(new ZipEntry(fileName));

                InputStream inputStream = child.read();
                try {
                    while (true) {
                        int readCount = inputStream.read(buffer);
                        if (readCount < 0) {
                            break;
                        }
                        zipOutputStream.write(buffer, 0, readCount);
                    }
                } finally {
                    inputStream.close();
                }

                zipOutputStream.closeEntry();
            }
        }
    }
}
