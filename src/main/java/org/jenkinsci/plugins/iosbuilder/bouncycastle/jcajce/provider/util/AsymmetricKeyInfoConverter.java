package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.util;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public interface AsymmetricKeyInfoConverter
{
    PrivateKey generatePrivate(PrivateKeyInfo keyInfo)
        throws IOException;

    PublicKey generatePublic(SubjectPublicKeyInfo keyInfo)
        throws IOException;
}
