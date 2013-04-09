package org.jenkinsci.plugins.iosbuilder;

import java.util.Date;
import sun.security.x509.X500Name;

import java.util.logging.Logger;

public class Certificate {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    private String name;
    private Date expirationDate;

    public Certificate(byte[] data) {
        try {
            javax.security.cert.X509Certificate certificate = javax.security.cert.X509Certificate.getInstance(data);
            this.name = new X500Name(certificate.getSubjectDN().getName()).getCommonName();
            this.expirationDate = certificate.getNotAfter();
        }
        catch (Exception e) {}
        LOG.info(this.name);
        // LOG.info(this.expirationDate.toString());
    }
    public Certificate(java.security.cert.X509Certificate certificate) {
        try {
            this.name = new X500Name(certificate.getSubjectX500Principal().getName()).getCommonName();
            this.expirationDate = certificate.getNotAfter();
        }
        catch (Exception e) {}
        LOG.info(this.name);
        // LOG.info(this.expirationDate.toString());
    }

    public String getName() { return name; }
    public Date getExpirationDate() { return expirationDate; }
}