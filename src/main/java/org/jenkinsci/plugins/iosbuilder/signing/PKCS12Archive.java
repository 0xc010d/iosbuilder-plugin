package org.jenkinsci.plugins.iosbuilder.signing;

import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PKCS12Archive {
    private final static Logger LOG = Logger.getLogger(org.jenkinsci.plugins.iosbuilder.PluginImpl.class.getName());

    private Map<PrivateKey, Certificate> content;

    private PKCS12Archive(byte[] data, char[] password) {
        content = new HashMap<PrivateKey, Certificate>();
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(data), password);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                try {
                    String alias = aliases.nextElement();
                    PrivateKey privateKey = PrivateKey.getInstance(alias, keyStore, password);
                    Certificate certificate = Certificate.getInstance((X509Certificate)keyStore.getCertificate(alias));
                    content.put(privateKey, certificate);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PKCS12Archive getInstance(byte[] data, char[] password) {
        try {
            return new PKCS12Archive(data, password);
        }
        catch (Exception e) {}
        return null;
    }

    public static PKCS12Archive getInstance(String encodedData, String password) {
        try {
            return getInstance(new BASE64Decoder().decodeBuffer(encodedData), password.toCharArray());
        }
        catch (Exception e) {}
        return null;
    }

    public Map<PrivateKey, Certificate> getContent() { return content; }

    public Certificate chooseCertificate(Mobileprovision mobileprovision) {
        return chooseCertificate(mobileprovision, true);
    }
    public Certificate chooseCertificate(Mobileprovision mobileprovision, boolean checkKeys) {
        if (mobileprovision != null && mobileprovision.getCertificates() != null) {
            for (Certificate certificate : mobileprovision.getCertificates()) {
                for (Map.Entry<PrivateKey, Certificate> entry : content.entrySet()) {
                    if (checkKeys) {
                        PrivateKey privateKey = entry.getKey();
                        if (privateKey != null && privateKey.checkPublicKey(certificate.getPublicKey())) {
                            return certificate;
                        }
                    }
                    else if (certificate.equals(entry.getValue())) {
                        return certificate;
                    }
                }
            }
        }
        return null;
    }
}
