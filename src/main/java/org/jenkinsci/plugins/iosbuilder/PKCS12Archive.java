package org.jenkinsci.plugins.iosbuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import sun.security.x509.X500Name;

import java.util.logging.Logger;

public class PKCS12Archive {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private Map<String, Certificate> keys;

    public PKCS12Archive(byte[] data, char[] password) {
        this.keys = new HashMap();

        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream inputStream = new ByteArrayInputStream(data);
            keyStore.load(inputStream, password);
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    LOG.info(alias);
                    Certificate certificate = null;
                    try {
                        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, new KeyStore.PasswordProtection(password));
                        certificate = new Certificate((X509Certificate)entry.getCertificate());
                    }
                    catch (NullPointerException e) {}
                    this.keys.put(alias, certificate);
                }
            }
            LOG.info(this.keys.toString());
        }
        catch (Exception e) {
            LOG.info(e.toString());
        }
    }

    public Map<String, Certificate> getKeys() { return keys; }
}
