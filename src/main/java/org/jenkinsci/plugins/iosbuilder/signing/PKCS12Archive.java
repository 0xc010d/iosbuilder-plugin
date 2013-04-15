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

    private Map<String, Certificate> certificates;

    private PKCS12Archive(byte[] data, char[] password) {
        certificates = new HashMap<String, Certificate>();
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(data), password);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                certificates.put(alias, new Certificate((X509Certificate)keyStore.getCertificate(alias)));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PKCS12Archive getInstance(byte[] data, char[] password) {
        if (data != null && data.length != 0 && password != null && password.length != 0) {
            return new PKCS12Archive(data, password);
        }
        return null;
    }
    public static PKCS12Archive getInstance(String encodedData, String password) {
        try {
            return getInstance(new BASE64Decoder().decodeBuffer(encodedData), password.toCharArray());
        }
        catch (Exception e) {}
        return null;
    }

    public Map<String, Certificate> getCertificates() { return certificates; }

    public Certificate chooseCertificate(Mobileprovision mobileprovision) {
        if (mobileprovision != null && mobileprovision.getCertificates() != null) {
            for (Certificate certificate : mobileprovision.getCertificates()) {
                for (String key : certificates.keySet()) {
                    if (certificate.equals(certificates.get(key))) {
                        return certificate;
                    }
                }
            }
        }
        return null;
    }
}
