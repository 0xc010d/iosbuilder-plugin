package org.jenkinsci.plugins.iosbuilder.signing;

import hudson.util.Secret;
import sun.misc.BASE64Decoder;

public class PKCS12ArchiveFactory {
    public static PKCS12Archive newInstance(byte[] data, char[] password) {
        try {
            return new PKCS12Archive(data, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PKCS12Archive newInstance(String encodedData, Secret password) {
        try {
            return newInstance(new BASE64Decoder().decodeBuffer(encodedData), Secret.toString(password).toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
