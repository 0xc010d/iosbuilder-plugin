package org.jenkinsci.plugins.iosbuilder.signing;

import java.security.KeyStore;

public class PrivateKeyFactory {
    public static PrivateKey newInstance(String alias, java.security.PrivateKey privateKey) {
        try {
            return new PrivateKey(alias, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey newInstance(String alias, KeyStore keyStore, char[] password) {
        try {
            return new PrivateKey(alias, (java.security.PrivateKey) keyStore.getKey(alias, password));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
