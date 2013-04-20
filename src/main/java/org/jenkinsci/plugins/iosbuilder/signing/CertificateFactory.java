package org.jenkinsci.plugins.iosbuilder.signing;

import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;

public class CertificateFactory {
    public static Certificate newInstance(byte[] data) {
        try {
            java.security.cert.CertificateFactory certificateFactory = java.security.cert.CertificateFactory.getInstance("X.509");
            return newInstance((X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(data)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Certificate newInstance(X509Certificate x509Certificate) {
        try {
            return new Certificate(x509Certificate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Certificate newInstance(String encodedData) {
        try {
            return newInstance(new BASE64Decoder().decodeBuffer(encodedData));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
