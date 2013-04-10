package org.jenkinsci.plugins.iosbuilder;

import sun.security.x509.X500Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import java.util.logging.Logger;

public class Certificate {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private X509Certificate x509Certificate;
    private String name;
    private Date expirationDate;

    public Certificate(byte[] data) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            this.x509Certificate = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(data));
            this.setProperties();
        }
        catch (Exception e) {}
        LOG.info(this.name);
    }
    public Certificate(X509Certificate certificate) {
        try {
            this.x509Certificate = certificate;
            this.setProperties();
        }
        catch (Exception e) {}
        LOG.info(this.name);
    }
    private void setProperties() throws IOException {
        this.name = new X500Name(this.x509Certificate.getSubjectDN().getName()).getCommonName();
        this.expirationDate = this.x509Certificate.getNotAfter();
    }

    public String getName() { return name; }
    public Date getExpirationDate() { return expirationDate; }
    X509Certificate getX509Certificate() { return x509Certificate; }
}