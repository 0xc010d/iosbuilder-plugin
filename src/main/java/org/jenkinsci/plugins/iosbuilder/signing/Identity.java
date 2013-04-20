package org.jenkinsci.plugins.iosbuilder.signing;

import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;

public class Identity {
    private final PrivateKey privateKey;
    private final Certificate certificate;

    public String getCommonName() { return certificate.getCommonName(); }
    public Date getExpirationDate() { return certificate.getExpirationDate(); }

    Identity(PrivateKey privateKey, Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public void saveToFile(String path, char[] password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        X509Certificate[] certificateChain = {certificate.getX509Certificate()};
        keyStore.setKeyEntry(privateKey.getAlias(), privateKey.getPrivateKey(), password, certificateChain);
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        keyStore.store(fileOutputStream, password);
        fileOutputStream.close();
    }
}
