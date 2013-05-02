package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.agreement.kdf;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.DerivationParameters;

public class DHKDFParameters
    implements DerivationParameters
{
    private ASN1ObjectIdentifier algorithm;
    private int keySize;
    private byte[] z;
    private byte[] extraInfo;

    public DHKDFParameters(
        DERObjectIdentifier algorithm,
        int keySize,
        byte[] z)
    {
        this(algorithm, keySize, z, null);
    }

    public DHKDFParameters(
        DERObjectIdentifier algorithm,
        int keySize,
        byte[] z,
        byte[] extraInfo)
    {
        this.algorithm = new ASN1ObjectIdentifier(algorithm.getId());
        this.keySize = keySize;
        this.z = z;
        this.extraInfo = extraInfo;
    }

    public ASN1ObjectIdentifier getAlgorithm()
    {
        return algorithm;
    }

    public int getKeySize()
    {
        return keySize;
    }

    public byte[] getZ()
    {
        return z;
    }

    public byte[] getExtraInfo()
    {
        return extraInfo;
    }
}
