package org.jenkinsci.plugins.iosbuilder;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.QuotedStringTokenizer;
import org.jenkinsci.plugins.iosbuilder.signing.Identity;
import org.jenkinsci.plugins.iosbuilder.signing.Mobileprovision;
import org.jenkinsci.plugins.iosbuilder.signing.PKCS12Archive;
import org.jenkinsci.plugins.iosbuilder.util.AppInfoExtractor;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

public class iOSBuilderExecutor {
    private final AbstractBuild build;
    private final Launcher launcher;
    private final BuildListener listener;
    private final String security;
    private final String xcodebuild;
    private final String xcrun;
    private final EnvVars envVars;
    private Identity identity;
    private String keychainName;
    private Mobileprovision mobileprovision;
    private FilePath buildPath;
    private Map<String, AppInfoExtractor.AppInfo> appInfoMap = new HashMap<String, AppInfoExtractor.AppInfo>();

    iOSBuilderExecutor(String securityPath, String xcodebuildPath, String xcrunPath, AbstractBuild build, Launcher launcher, BuildListener listener, String buildDirectory) throws Exception {
        this.security = securityPath;
        this.xcodebuild = xcodebuildPath;
        this.xcrun = xcrunPath;
        this.build = build;
        this.launcher = launcher;
        this.listener = listener;
        this.envVars = build.getEnvironment(listener);
        this.buildPath = new FilePath(this.build.getWorkspace(), envVars.expand(buildDirectory));
    }

    int installIdentity(PKCS12Archive pkcs12Archive, Mobileprovision mobileprovision) {
        int result = 0;
        FilePath identityPath = null;
        try {
            identity = pkcs12Archive.chooseIdentity(mobileprovision.getCertificates());
            if (identity != null) {
                identityPath = build.getWorkspace().createTempFile("identity", ".p12");
                String identityPassword = UUID.randomUUID().toString();
                identity.save(identityPath.write(), identityPassword.toCharArray());

                //create a keychain, import identity
                keychainName = UUID.randomUUID().toString() + ".keychain";
                String keychainPassword = UUID.randomUUID().toString();
                execute(security, "create-keychain", "-p", keychainPassword, keychainName);

                List<String> keychains = executeAndParse(security, "list-keychains", "-d", "user");
                for (int i = 0; i < keychains.size(); i++) {
                    String keychain = keychains.get(i);
                    keychains.set(i, keychain.trim().replaceAll("^\"|\"$", ""));
                }
                ArrayList<String> command = new ArrayList<String>();
                command.add(security);
                command.add("list-keychains");
                command.add("-s");
                command.addAll(keychains);
                command.add(keychainName);
                execute(command);

                execute(security, "import", identityPath.getRemote(), "-k", keychainName, "-P", identityPassword, "-A");
                execute(security, "unlock-keychain", "-p", keychainPassword, keychainName);
                execute(security, "set-keychain-settings", "-u", keychainName);
            }
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            result = 1;
        }
        finally {
            try {
                if (identityPath != null) {
                    identityPath.delete();
                }
            }
            catch (Exception e) {
                e.printStackTrace(listener.getLogger());
            }
        }
        if (result == 0) {
            try {
                this.mobileprovision = mobileprovision;

                String mobileprovisionPath = "/Library/MobileDevice/Provisioning Profiles/" + mobileprovision.getUUID() + ".mobileprovision";
                String mobileprovisionAbsolutePath = envVars.get("HOME").replaceAll("/$", "") + mobileprovisionPath;
                Node node = build.getBuiltOn();
                FilePath mobileprovisionFilePath = node.createPath(mobileprovisionAbsolutePath);
                mobileprovisionFilePath.write().write(mobileprovision.getBytes());
            }
            catch (Exception e) {
                e.printStackTrace(listener.getLogger());
                result = 1;
            }
        }
        return result;
    }

    int runXcodebuild(String xcworkspacePath, String xcodeprojPath, String target, String scheme, String configuration, String sdk, String additionalParameters, boolean codeSign, boolean doIsolateDerivedData, String objRootPath, String symRootPath, String sharedPrecompsDirPath, String moduleCacheDirPath) {
        try {
            ArrayList<String> buildCommand = new ArrayList<String>();
            buildCommand.add(xcodebuild);
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
            buildCommand.add("CONFIGURATION_BUILD_DIR=" + buildPath);
            if (additionalParameters != null && !additionalParameters.isEmpty()) {
                Collections.addAll(buildCommand, QuotedStringTokenizer.tokenize(envVars.expand(additionalParameters)));
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
            if (doIsolateDerivedData) {
                buildCommand.add("OBJROOT=" + envVars.expand(objRootPath));
                buildCommand.add("SYMROOT=" + envVars.expand(symRootPath));
                buildCommand.add("SHARED_PRECOMPS_DIR=" + envVars.expand(sharedPrecompsDirPath));
                buildCommand.add("MODULE_CACHE_DIR=" + envVars.expand(moduleCacheDirPath));
            }
            return executeAt(build.getWorkspace(), buildCommand);
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
    }

    int exportInfo() {
        try {
            List<FilePath> filePaths = buildPath.list();
            for (FilePath filePath : filePaths) {
                if (filePath.isDirectory() && filePath.getName().endsWith(".app")) {
                    AppInfoExtractor.AppInfo appInfo = AppInfoExtractor.extract(filePath);
                    appInfoMap.put(filePath.getBaseName(), appInfo);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
        return 0;
    }

    Map<String, String>getExportedInfo() {
        Map<String, String> info = new HashMap<String, String>();
        if (appInfoMap.size() > 0) {
            Map.Entry<String, AppInfoExtractor.AppInfo> appInfoEntry = appInfoMap.entrySet().iterator().next();
            String appName = appInfoEntry.getKey();
            AppInfoExtractor.AppInfo appInfo = appInfoEntry.getValue();
            info.put("IOSBUILDER_APP_NAME", appName);
            info.put("IOSBUILDER_BUNDLE_NAME", appInfo.getBundleName());
            info.put("IOSBUILDER_BUNDLE_DISPLAY_NAME", appInfo.getBundleDisplayName());
            info.put("IOSBUILDER_BUNDLE_EXECUTABLE", appInfo.getBundleExecutable());
            info.put("IOSBUILDER_BUNDLE_IDENTIFIER", appInfo.getBundleIdentifier());
            info.put("IOSBUILDER_BUNDLE_VERSION", appInfo.getBundleVersion());
            info.put("IOSBUILDER_BUNDLE_SHORT_VERSION_STRING", appInfo.getBundleShortVersionString());
        }
        return info;
    }

    int buildIpa(String ipaNameTemplate) {
        try {
            ipaNameTemplate = envVars.expand(ipaNameTemplate);
            for (Map.Entry<String, AppInfoExtractor.AppInfo> appInfo : appInfoMap.entrySet()) {
                String basename = appInfo.getKey();
                String filename = basename + ".app";
                FilePath filePath = buildPath.child(filename);
                if (filePath.exists() && filePath.isDirectory()) {
                    String outFileName = applyTemplate(basename, ipaNameTemplate) + ".ipa";
                    FilePath outFilePath = build.getWorkspace().child(outFileName);
                    outFilePath.getParent().mkdirs();
                    executeAt(buildPath, xcrun, "-sdk", "iphoneos", "PackageApplication", filename, "-o", outFilePath.getRemote());
                }
            }
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
        return 0;
    }

    int zipDSYM(String dSYMNameTemplate) {
        try {
            dSYMNameTemplate = envVars.expand(dSYMNameTemplate);
            for (Map.Entry<String, AppInfoExtractor.AppInfo> appInfo : appInfoMap.entrySet()) {
                String basename = appInfo.getKey();
                String filename = basename + ".app.dSYM";
                FilePath filePath = buildPath.child(filename);
                if (filePath.exists() && filePath.isDirectory()) {
                    String outFileName = applyTemplate(basename, dSYMNameTemplate) + ".app.dSYM.zip";
                    FilePath outFilePath = build.getWorkspace().child(outFileName);
                    outFilePath.getParent().mkdirs();
                    filePath.zip(outFilePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return 1;
        }
        return 0;
    }

    void cleanup() {
        try {
            execute(security, "delete-keychain", keychainName);
        }
        catch (Exception e) {
            e.printStackTrace(listener.getLogger());
        }
    }

    private String applyTemplate(String basename, String template) {
        String result = template.replaceAll("\\$APP_NAME", basename);
        AppInfoExtractor.AppInfo appInfo = appInfoMap.get(basename);
        if (appInfo != null) {
            String bundleVersion = appInfoMap.get(basename).getBundleVersion();
            if (bundleVersion != null) {
                result = result.replaceAll("\\$BUNDLE_VERSION", bundleVersion);
            }
        }
        return result;
    }

    private int execute(String... args) throws Exception {
        return launcher.launch().envs(envVars).stdout(listener).stderr(listener.getLogger()).cmds(args).join();
    }

    private int execute(List<String> args) throws Exception {
        return launcher.launch().envs(envVars).stdout(listener).stderr(listener.getLogger()).cmds(args).join();
    }

    private int executeAt(FilePath path, String... args) throws Exception {
        return launcher.launch().pwd(path).envs(envVars).stdout(listener).stderr(listener.getLogger()).cmds(args).join();
    }

    private int executeAt(FilePath path, List<String> args) throws Exception {
        return launcher.launch().pwd(path).envs(envVars).stdout(listener).stderr(listener.getLogger()).cmds(args).join();
    }

    private List<String> executeAndParse(String... args) throws Exception {
        OutputStream outputStream = new ByteArrayOutputStream();
        launcher.launch().envs(envVars).stdout(outputStream).stderr(listener.getLogger()).cmds(args).join();
        String output = outputStream.toString();
        return Arrays.asList(output.split(System.getProperty("line.separator")));
    }
}
