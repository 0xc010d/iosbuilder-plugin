package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.asymmetric.ec;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.nist.NISTNamedCurves;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.sec.SECNamedCurves;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X962NamedCurves;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X9ECParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.ECDomainParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.interfaces.ECPrivateKey;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.interfaces.ECPublicKey;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECParameterSpec;

/**
 * utility class for converting jce/jca ECDSA, ECDH, and ECDHC
 * objects into their org.bouncycastle.crypto counterparts.
 */
public class ECUtil
{
    /**
     * Returns a sorted array of middle terms of the reduction polynomial.
     * @param k The unsorted array of middle terms of the reduction polynomial
     * of length 1 or 3.
     * @return the sorted array of middle terms of the reduction polynomial.
     * This array always has length 3.
     */
    static int[] convertMidTerms(
        int[] k)
    {
        int[] res = new int[3];
        
        if (k.length == 1)
        {
            res[0] = k[0];
        }
        else
        {
            if (k.length != 3)
            {
                throw new IllegalArgumentException("Only Trinomials and pentanomials supported");
            }

            if (k[0] < k[1] && k[0] < k[2])
            {
                res[0] = k[0];
                if (k[1] < k[2])
                {
                    res[1] = k[1];
                    res[2] = k[2];
                }
                else
                {
                    res[1] = k[2];
                    res[2] = k[1];
                }
            }
            else if (k[1] < k[2])
            {
                res[0] = k[1];
                if (k[0] < k[2])
                {
                    res[1] = k[0];
                    res[2] = k[2];
                }
                else
                {
                    res[1] = k[2];
                    res[2] = k[0];
                }
            }
            else
            {
                res[0] = k[2];
                if (k[0] < k[1])
                {
                    res[1] = k[0];
                    res[2] = k[1];
                }
                else
                {
                    res[1] = k[1];
                    res[2] = k[0];
                }
            }
        }

        return res;
    }

    public static AsymmetricKeyParameter generatePublicKeyParameter(
        PublicKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ECPublicKey)
        {
            ECPublicKey    k = (ECPublicKey)key;
            ECParameterSpec s = k.getParameters();

            if (s == null)
            {
                s = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();

                return new ECPublicKeyParameters(
                            ((BCECPublicKey)k).engineGetQ(),
                            new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
            }
            else
            {
                return new ECPublicKeyParameters(
                            k.getQ(),
                            new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
            }
        }
        else if (key instanceof java.security.interfaces.ECPublicKey)
        {
            java.security.interfaces.ECPublicKey pubKey = (java.security.interfaces.ECPublicKey)key;
            ECParameterSpec s = EC5Util.convertSpec(pubKey.getParams(), false);
            return new ECPublicKeyParameters(
                EC5Util.convertPoint(pubKey.getParams(), pubKey.getW(), false),
                            new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        }
        else
        {
            // see if we can build a key from key.getEncoded()
            try
            {
                byte[] bytes = key.getEncoded();

                if (bytes == null)
                {
                    throw new InvalidKeyException("no encoding for EC public key");
                }

                PublicKey publicKey = BouncyCastleProvider.getPublicKey(SubjectPublicKeyInfo.getInstance(bytes));

                if (publicKey instanceof java.security.interfaces.ECPublicKey)
                {
                    return ECUtil.generatePublicKeyParameter(publicKey);
                }
            }
            catch (Exception e)
            {
                throw new InvalidKeyException("cannot identify EC public key: " + e.toString());
            }
        }

        throw new InvalidKeyException("cannot identify EC public key.");
    }

    public static AsymmetricKeyParameter generatePrivateKeyParameter(
        PrivateKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ECPrivateKey)
        {
            ECPrivateKey  k = (ECPrivateKey)key;
            ECParameterSpec s = k.getParameters();

            if (s == null)
            {
                s = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
            }

            return new ECPrivateKeyParameters(
                            k.getD(),
                            new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        }
        else if (key instanceof java.security.interfaces.ECPrivateKey)
        {
            java.security.interfaces.ECPrivateKey privKey = (java.security.interfaces.ECPrivateKey)key;
            ECParameterSpec s = EC5Util.convertSpec(privKey.getParams(), false);
            return new ECPrivateKeyParameters(
                            privKey.getS(),
                            new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        }
        else
        {
            // see if we can build a key from key.getEncoded()
            try
            {
                byte[] bytes = key.getEncoded();

                if (bytes == null)
                {
                    throw new InvalidKeyException("no encoding for EC private key");
                }

                PrivateKey privateKey = BouncyCastleProvider.getPrivateKey(PrivateKeyInfo.getInstance(bytes));

                if (privateKey instanceof java.security.interfaces.ECPrivateKey)
                {
                    return ECUtil.generatePrivateKeyParameter(privateKey);
                }
            }
            catch (Exception e)
            {
                throw new InvalidKeyException("cannot identify EC private key: " + e.toString());
            }
        }

        throw new InvalidKeyException("can't identify EC private key.");
    }

    public static ASN1ObjectIdentifier getNamedCurveOid(
        String name)
    {
        ASN1ObjectIdentifier oid = X962NamedCurves.getOID(name);
        
        if (oid == null)
        {
            oid = SECNamedCurves.getOID(name);
            if (oid == null)
            {
                oid = NISTNamedCurves.getOID(name);
            }
            if (oid == null)
            {
                oid = TeleTrusTNamedCurves.getOID(name);
            }
            if (oid == null)
            {
                oid = ECGOST3410NamedCurves.getOID(name);
            }
        }

        return oid;
    }
    
    public static X9ECParameters getNamedCurveByOid(
        ASN1ObjectIdentifier oid)
    {
        X9ECParameters params = X962NamedCurves.getByOID(oid);
        
        if (params == null)
        {
            params = SECNamedCurves.getByOID(oid);
            if (params == null)
            {
                params = NISTNamedCurves.getByOID(oid);
            }
            if (params == null)
            {
                params = TeleTrusTNamedCurves.getByOID(oid);
            }
        }

        return params;
    }

    public static String getCurveName(
        ASN1ObjectIdentifier oid)
    {
        String name = X962NamedCurves.getName(oid);
        
        if (name == null)
        {
            name = SECNamedCurves.getName(oid);
            if (name == null)
            {
                name = NISTNamedCurves.getName(oid);
            }
            if (name == null)
            {
                name = TeleTrusTNamedCurves.getName(oid);
            }
            if (name == null)
            {
                name = ECGOST3410NamedCurves.getName(oid);
            }
        }

        return name;
    }
}
