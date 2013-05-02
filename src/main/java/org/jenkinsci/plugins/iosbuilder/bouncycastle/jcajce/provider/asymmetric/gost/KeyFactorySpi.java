package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.asymmetric.gost;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.asymmetric.util.BaseKeyFactorySpi;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.interfaces.GOST3410PrivateKey;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.interfaces.GOST3410PublicKey;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.GOST3410PrivateKeySpec;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.GOST3410PublicKeyParameterSetSpec;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.GOST3410PublicKeySpec;

public class KeyFactorySpi
    extends BaseKeyFactorySpi
{
    public KeyFactorySpi()
    {
    }

    protected KeySpec engineGetKeySpec(
        Key key,
        Class spec)
        throws InvalidKeySpecException
    {
        if (spec.isAssignableFrom(GOST3410PublicKeySpec.class) && key instanceof GOST3410PublicKey)
        {
            GOST3410PublicKey k = (GOST3410PublicKey)key;
            GOST3410PublicKeyParameterSetSpec parameters = k.getParameters().getPublicKeyParameters();

            return new GOST3410PublicKeySpec(k.getY(), parameters.getP(), parameters.getQ(), parameters.getA());
        }
        else if (spec.isAssignableFrom(GOST3410PrivateKeySpec.class) && key instanceof GOST3410PrivateKey)
        {
            GOST3410PrivateKey k = (GOST3410PrivateKey)key;
            GOST3410PublicKeyParameterSetSpec parameters = k.getParameters().getPublicKeyParameters();

            return new GOST3410PrivateKeySpec(k.getX(), parameters.getP(), parameters.getQ(), parameters.getA());
        }

        return super.engineGetKeySpec(key, spec);
    }

    protected Key engineTranslateKey(
        Key key)
        throws InvalidKeyException
    {
        if (key instanceof GOST3410PublicKey)
        {
            return new BCGOST3410PublicKey((GOST3410PublicKey)key);
        }
        else if (key instanceof GOST3410PrivateKey)
        {
            return new BCGOST3410PrivateKey((GOST3410PrivateKey)key);
        }

        throw new InvalidKeyException("key type unknown");
    }

    protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
    throws InvalidKeySpecException
    {
        if (keySpec instanceof GOST3410PrivateKeySpec)
        {
            return new BCGOST3410PrivateKey((GOST3410PrivateKeySpec)keySpec);
        }

        return super.engineGeneratePrivate(keySpec);
    }

    protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
    throws InvalidKeySpecException
    {
        if (keySpec instanceof GOST3410PublicKeySpec)
        {
            return new BCGOST3410PublicKey((GOST3410PublicKeySpec)keySpec);
        }

        return super.engineGeneratePublic(keySpec);
    }

    public PrivateKey generatePrivate(PrivateKeyInfo keyInfo)
        throws IOException
    {
        ASN1ObjectIdentifier algOid = keyInfo.getPrivateKeyAlgorithm().getAlgorithm();

        if (algOid.equals(CryptoProObjectIdentifiers.gostR3410_94))
        {
            return new BCGOST3410PrivateKey(keyInfo);
        }
        else
        {
            throw new IOException("algorithm identifier " + algOid + " in key not recognised");
        }
    }

    public PublicKey generatePublic(SubjectPublicKeyInfo keyInfo)
        throws IOException
    {
        ASN1ObjectIdentifier algOid = keyInfo.getAlgorithm().getAlgorithm();

        if (algOid.equals(CryptoProObjectIdentifiers.gostR3410_94))
        {
            return new BCGOST3410PublicKey(keyInfo);
        }
        else
        {
            throw new IOException("algorithm identifier " + algOid + " in key not recognised");
        }
    }
}
