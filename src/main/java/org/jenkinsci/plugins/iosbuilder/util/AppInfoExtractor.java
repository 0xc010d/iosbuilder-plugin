package org.jenkinsci.plugins.iosbuilder.util;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import hudson.FilePath;
import hudson.util.IOUtils;

import java.io.InputStream;
import java.util.Map;

public class AppInfoExtractor {
    public static AppInfo extract(FilePath appPath) throws Exception {
        FilePath infoPlistPath = appPath.child("Info.plist");
        InputStream inputStream = infoPlistPath.read();
        byte[] bytes = IOUtils.toByteArray(inputStream);
        return new AppInfo(bytes);
    }

    public static class AppInfo {
        private final Map<String, Object> info;

        private AppInfo(byte[] bytes) throws Exception {
            NSDictionary root = (NSDictionary)PropertyListParser.parse(bytes);
            this.info = (Map<String, Object>)root.toJavaObject();
        }

        public String getBundleName() { return (String)info.get("CFBundleName"); }
        public String getBundleDisplayName() { return (String)info.get("CFBundleDisplayName"); }
        public String getBundleExecutable() { return (String)info.get("CFBundleExecutable"); }
        public String getBundleIdentifier() { return (String)info.get("CFBundleIdentifier"); }
        public String getBundleVersion() { return (String)info.get("CFBundleVersion"); }
        public String getBundleShortVersionString() { return (String)info.get("CFBundleShortVersionString"); }
    }
}
