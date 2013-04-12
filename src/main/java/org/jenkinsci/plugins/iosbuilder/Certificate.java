package org.jenkinsci.plugins.iosbuilder;

import sun.security.x509.X500Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import java.util.logging.Logger;

public class Certificate {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private X509Certificate x509Certificate;
    private String commonName;
    private Date expirationDate;

    public Certificate(X509Certificate certificate) {
        try {
            if (certificate != null) {
                this.x509Certificate = certificate;
                this.commonName = new X500Name(this.x509Certificate.getSubjectDN().getName()).getCommonName();
                this.expirationDate = this.x509Certificate.getNotAfter();
            }
        }
        catch (Exception e) {}
    }
    public static Certificate getInstance(byte[] data) {
        if (data != null && data.length != 0) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                return getInstance((X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(data)));
            }
            catch (CertificateException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static Certificate getInstance(X509Certificate x509Certificate) {
        if (x509Certificate != null) {
            return new Certificate(x509Certificate);
        }
        return null;
    }

    public boolean equals(Certificate certificate) {
        if (certificate instanceof Certificate) {
            return this.x509Certificate.equals(certificate.getX509Certificate());
        }
        else {
            return false;
        }
    }

    private X509Certificate getX509Certificate() { return x509Certificate; }
    public String getCommonName() { return commonName; }
    public Date getExpirationDate() { return expirationDate; }
}