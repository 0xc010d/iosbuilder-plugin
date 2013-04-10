package org.jenkinsci.plugins.iosbuilder;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import java.util.logging.Logger;

public class PKCS12Archive {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private PrivateKey[] privateKeys;

    public PKCS12Archive(byte[] data, char[] password) {
        List<PrivateKey> privateKeys = new ArrayList();

        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(data), password);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    try {
                        privateKeys.add(new PrivateKey(alias, (java.security.PrivateKey)keyStore.getKey(alias, password)));
                    }
                    catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    this.privateKeys = privateKeys.toArray(new PrivateKey[privateKeys.size()]);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    PrivateKey[] getPrivateKeys() { return privateKeys; }

    public boolean checkCertificate(Certificate certificate) {
        for (int index = 0; index < this.privateKeys.length; index ++) {
            PrivateKey privateKey = this.privateKeys[index];
            if (privateKey.checkPublicKey(certificate.getX509Certificate().getPublicKey())) {
                return true;
            }
        }
        return false;
    }
}
