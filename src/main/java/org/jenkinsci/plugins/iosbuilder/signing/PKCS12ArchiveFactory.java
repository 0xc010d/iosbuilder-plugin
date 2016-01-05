package org.jenkinsci.plugins.iosbuilder.signing;

import hudson.util.Secret;
import sun.misc.BASE64Decoder;

import java.io.IOException;

public class PKCS12ArchiveFactory {
    public static PKCS12Archive newInstance(byte[] data, char[] password) throws IOException {
        return new PKCS12Archive(data, password);
    }

    public static PKCS12Archive newInstance(String encodedData, Secret password) throws IOException {
        return newInstance(new BASE64Decoder().decodeBuffer(encodedData), Secret.toString(password).toCharArray());
    }

    public static PKCS12Archive newSafeInstance(String encodedData, Secret password) throws IOException {
        try {
            return newInstance(encodedData, password);
        } catch (IOException e) {
            return null;
        }
    }
}
