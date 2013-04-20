package org.jenkinsci.plugins.iosbuilder.signing;

import sun.security.x509.X500Name;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;

public class Certificate {
    private X509Certificate x509Certificate;
    private String commonName;
    private Date expirationDate;

    X509Certificate getX509Certificate() { return x509Certificate; }
    public String getCommonName() { return commonName; }
    public Date getExpirationDate() { return expirationDate; }
    public PublicKey getPublicKey() { return x509Certificate.getPublicKey(); }

    Certificate(X509Certificate certificate) throws IOException {
        if (certificate != null) {
            this.x509Certificate = certificate;
            this.commonName = new X500Name(certificate.getSubjectDN().getName()).getCommonName();
            this.expirationDate = certificate.getNotAfter();
        }
        else {
            throw new IOException("Can not instantiate certificate object");
        }
    }
}
