package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.util;

import java.io.IOException;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Integer;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERNull;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.RSAKeyParameters;

/**
 * Factory to create ASN.1 subject public key info objects from lightweight public keys.
 */
public class SubjectPublicKeyInfoFactory
{
    /**
     * Create a SubjectPublicKeyInfo public key.
     *
     * @param publicKey the SubjectPublicKeyInfo encoding
     * @return the appropriate key parameter
     * @throws java.io.IOException on an error encoding the key
     */
    public static SubjectPublicKeyInfo createSubjectPublicKeyInfo(AsymmetricKeyParameter publicKey) throws IOException
    {
        if (publicKey instanceof RSAKeyParameters)
        {
            RSAKeyParameters pub = (RSAKeyParameters)publicKey;

            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), new RSAPublicKey(pub.getModulus(), pub.getExponent()));
        }
        else if (publicKey instanceof DSAPublicKeyParameters)
        {
            DSAPublicKeyParameters pub = (DSAPublicKeyParameters)publicKey;

            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa), new ASN1Integer(pub.getY()));
        }
        else
        {
            throw new IOException("key parameters not recognised.");
        }
    }
}
