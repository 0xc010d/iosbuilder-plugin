package org.jenkinsci.plugins.iosbuilder.signing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PKCS12Archive {
    private final static Logger LOG = Logger.getLogger(org.jenkinsci.plugins.iosbuilder.PluginImpl.class.getName());

    private Map<PrivateKey, Certificate> content;

    public Map<PrivateKey, Certificate> getContent() { return content; }

    PKCS12Archive(byte[] data, char[] password) throws IOException {
        try {
            content = new HashMap<PrivateKey, Certificate>();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(data), password);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                try {
                    String alias = aliases.nextElement();
                    PrivateKey privateKey = PrivateKeyFactory.newInstance(alias, keyStore, password);
                    Certificate certificate = CertificateFactory.newInstance((X509Certificate) keyStore.getCertificate(alias));
                    content.put(privateKey, certificate);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Can not instantiate PKCS#12 archive object");
        }
        if (content.size() == 0) {
            throw new IOException("Can not instantiate PKCS#12 archive object: private keys were not found");
        }
    }

    public Identity chooseIdentity(Certificate[] certificates) {
        try {
            for (Certificate certificate : certificates) {
                for (PrivateKey privateKey : content.keySet()) {
                    if (privateKey.checkPublicKey(certificate.getPublicKey())) {
                        return new Identity(privateKey, certificate);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
