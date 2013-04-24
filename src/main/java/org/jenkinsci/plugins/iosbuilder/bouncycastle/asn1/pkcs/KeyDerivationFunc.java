package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.pkcs;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Encodable;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Sequence;

public class KeyDerivationFunc
    extends AlgorithmIdentifier
{
    KeyDerivationFunc(
        ASN1Sequence seq)
    {
        super(seq);
    }
    
    public KeyDerivationFunc(
        ASN1ObjectIdentifier id,
        ASN1Encodable params)
    {
        super(id, params);
    }
}
