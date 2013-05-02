package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.asymmetric.ec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Encodable;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1OctetString;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Primitive;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERBitString;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERNull;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DEROctetString;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X962Parameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X9ECParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X9ECPoint;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X9IntegerConverter;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.ECDomainParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.interfaces.ECPointEncoder;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECCurve;

public class BCECPublicKey
    implements ECPublicKey, org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.interfaces.ECPublicKey, ECPointEncoder
{
    static final long serialVersionUID = 2422789860422731812L;

    private String    algorithm = "EC";
    private boolean   withCompression;

    private transient org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECPoint q;
    private transient ECParameterSpec         ecSpec;
    private transient ProviderConfiguration configuration;

    public BCECPublicKey(
        String algorithm,
        BCECPublicKey key)
    {
        this.algorithm = algorithm;
        this.q = key.q;
        this.ecSpec = key.ecSpec;
        this.withCompression = key.withCompression;
        this.configuration = key.configuration;
    }
    
    public BCECPublicKey(
        String algorithm,
        ECPublicKeySpec spec,
        ProviderConfiguration configuration)
    {
        this.algorithm = algorithm;
        this.ecSpec = spec.getParams();
        this.q = EC5Util.convertPoint(ecSpec, spec.getW(), false);
        this.configuration = configuration;
    }

    public BCECPublicKey(
        String algorithm,
        org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECPublicKeySpec spec,
        ProviderConfiguration configuration)
    {
        this.algorithm = algorithm;
        this.q = spec.getQ();

        if (spec.getParams() != null) // can be null if implictlyCa
        {
            ECCurve curve = spec.getParams().getCurve();
            EllipticCurve ellipticCurve = EC5Util.convertCurve(curve, spec.getParams().getSeed());

            this.ecSpec = EC5Util.convertSpec(ellipticCurve, spec.getParams());
        }
        else
        {
            if (q.getCurve() == null)
            {
                org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECParameterSpec s = configuration.getEcImplicitlyCa();

                q = s.getCurve().createPoint(q.getX().toBigInteger(), q.getY().toBigInteger(), false);
            }               
            this.ecSpec = null;
        }

        this.configuration = configuration;
    }
    
    public BCECPublicKey(
        String algorithm,
        ECPublicKeyParameters params,
        ECParameterSpec spec,
        ProviderConfiguration configuration)
    {
        ECDomainParameters dp = params.getParameters();

        this.algorithm = algorithm;
        this.q = params.getQ();

        if (spec == null)
        {
            EllipticCurve ellipticCurve = EC5Util.convertCurve(dp.getCurve(), dp.getSeed());

            this.ecSpec = createSpec(ellipticCurve, dp);
        }
        else
        {
            this.ecSpec = spec;
        }

        this.configuration = configuration;
    }

    public BCECPublicKey(
        String algorithm,
        ECPublicKeyParameters params,
        org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECParameterSpec spec,
        ProviderConfiguration configuration)
    {
        ECDomainParameters      dp = params.getParameters();

        this.algorithm = algorithm;
        this.q = params.getQ();

        if (spec == null)
        {
            EllipticCurve ellipticCurve = EC5Util.convertCurve(dp.getCurve(), dp.getSeed());

            this.ecSpec = createSpec(ellipticCurve, dp);
        }
        else
        {
            EllipticCurve ellipticCurve = EC5Util.convertCurve(spec.getCurve(), spec.getSeed());

            this.ecSpec = EC5Util.convertSpec(ellipticCurve, spec);
        }

        this.configuration = configuration;
    }

    /*
     * called for implicitCA
     */
    public BCECPublicKey(
        String algorithm,
        ECPublicKeyParameters params,
        ProviderConfiguration configuration)
    {
        this.algorithm = algorithm;
        this.q = params.getQ();
        this.ecSpec = null;
        this.configuration = configuration;
    }

    public BCECPublicKey(
        ECPublicKey key,
        ProviderConfiguration configuration)
    {
        this.algorithm = key.getAlgorithm();
        this.ecSpec = key.getParams();
        this.q = EC5Util.convertPoint(this.ecSpec, key.getW(), false);
    }

    BCECPublicKey(
        String algorithm,
        SubjectPublicKeyInfo info,
        ProviderConfiguration configuration)
    {
        this.algorithm = algorithm;
        this.configuration = configuration;
        populateFromPubKeyInfo(info);
    }

    private ECParameterSpec createSpec(EllipticCurve ellipticCurve, ECDomainParameters dp)
    {
        return new ECParameterSpec(
                ellipticCurve,
                new ECPoint(
                        dp.getG().getX().toBigInteger(),
                        dp.getG().getY().toBigInteger()),
                        dp.getN(),
                        dp.getH().intValue());
    }

    private void populateFromPubKeyInfo(SubjectPublicKeyInfo info)
    {
        X962Parameters params = new X962Parameters((ASN1Primitive)info.getAlgorithm().getParameters());
        ECCurve                 curve;
        EllipticCurve           ellipticCurve;

        if (params.isNamedCurve())
        {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)params.getParameters();
            X9ECParameters ecP = ECUtil.getNamedCurveByOid(oid);

            curve = ecP.getCurve();
            ellipticCurve = EC5Util.convertCurve(curve, ecP.getSeed());

            ecSpec = new ECNamedCurveSpec(
                    ECUtil.getCurveName(oid),
                    ellipticCurve,
                    new ECPoint(
                            ecP.getG().getX().toBigInteger(),
                            ecP.getG().getY().toBigInteger()),
                    ecP.getN(),
                    ecP.getH());
        }
        else if (params.isImplicitlyCA())
        {
            ecSpec = null;
            curve = configuration.getEcImplicitlyCa().getCurve();
        }
        else
        {
            X9ECParameters          ecP = X9ECParameters.getInstance(params.getParameters());

            curve = ecP.getCurve();
            ellipticCurve = EC5Util.convertCurve(curve, ecP.getSeed());

            this.ecSpec = new ECParameterSpec(
                    ellipticCurve,
                    new ECPoint(
                            ecP.getG().getX().toBigInteger(),
                            ecP.getG().getY().toBigInteger()),
                    ecP.getN(),
                    ecP.getH().intValue());
        }

        DERBitString bits = info.getPublicKeyData();
        byte[]          data = bits.getBytes();
        ASN1OctetString key = new DEROctetString(data);

        //
        // extra octet string - one of our old certs...
        //
        if (data[0] == 0x04 && data[1] == data.length - 2
            && (data[2] == 0x02 || data[2] == 0x03))
        {
            int qLength = new X9IntegerConverter().getByteLength(curve);

            if (qLength >= data.length - 3)
            {
                try
                {
                    key = (ASN1OctetString) ASN1Primitive.fromByteArray(data);
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException("error recovering public key");
                }
            }
        }
        X9ECPoint derQ = new X9ECPoint(curve, key);

        this.q = derQ.getPoint();
    }

    public String getAlgorithm()
    {
        return algorithm;
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getEncoded()
    {
        ASN1Encodable        params;
        SubjectPublicKeyInfo info;

        if (ecSpec instanceof ECNamedCurveSpec)
        {
            ASN1ObjectIdentifier curveOid = ECUtil.getNamedCurveOid(((ECNamedCurveSpec)ecSpec).getName());
            if (curveOid == null)
            {
                curveOid = new ASN1ObjectIdentifier(((ECNamedCurveSpec)ecSpec).getName());
            }
            params = new X962Parameters(curveOid);
        }
        else if (ecSpec == null)
        {
            params = new X962Parameters(DERNull.INSTANCE);
        }
        else
        {
            ECCurve curve = EC5Util.convertCurve(ecSpec.getCurve());

            X9ECParameters ecP = new X9ECParameters(
                curve,
                EC5Util.convertPoint(curve, ecSpec.getGenerator(), withCompression),
                ecSpec.getOrder(),
                BigInteger.valueOf(ecSpec.getCofactor()),
                ecSpec.getCurve().getSeed());

            params = new X962Parameters(ecP);
        }

        ECCurve curve = this.engineGetQ().getCurve();
        ASN1OctetString p = (ASN1OctetString)
            new X9ECPoint(curve.createPoint(this.getQ().getX().toBigInteger(), this.getQ().getY().toBigInteger(), withCompression)).toASN1Primitive();

        info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), p.getOctets());

        return KeyUtil.getEncodedSubjectPublicKeyInfo(info);
    }

    private void extractBytes(byte[] encKey, int offSet, BigInteger bI)
    {
        byte[] val = bI.toByteArray();
        if (val.length < 32)
        {
            byte[] tmp = new byte[32];
            System.arraycopy(val, 0, tmp, tmp.length - val.length, val.length);
            val = tmp;
        }

        for (int i = 0; i != 32; i++)
        {
            encKey[offSet + i] = val[val.length - 1 - i];
        }
    }

    public ECParameterSpec getParams()
    {
        return ecSpec;
    }

    public org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECParameterSpec getParameters()
    {
        if (ecSpec == null)     // implictlyCA
        {
            return null;
        }

        return EC5Util.convertSpec(ecSpec, withCompression);
    }

    public ECPoint getW()
    {
        return new ECPoint(q.getX().toBigInteger(), q.getY().toBigInteger());
    }

    public org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECPoint getQ()
    {
        if (ecSpec == null)
        {
            if (q instanceof org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECPoint.Fp)
            {
                return new org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECPoint.Fp(null, q.getX(), q.getY());
            }
            else
            {
                return new org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECPoint.F2m(null, q.getX(), q.getY());
            }
        }

        return q;
    }

    public org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECPoint engineGetQ()
    {
        return q;
    }

    org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECParameterSpec engineGetSpec()
    {
        if (ecSpec != null)
        {
            return EC5Util.convertSpec(ecSpec, withCompression);
        }

        return configuration.getEcImplicitlyCa();
    }

    public String toString()
    {
        StringBuffer    buf = new StringBuffer();
        String          nl = System.getProperty("line.separator");

        buf.append("EC Public Key").append(nl);
        buf.append("            X: ").append(this.q.getX().toBigInteger().toString(16)).append(nl);
        buf.append("            Y: ").append(this.q.getY().toBigInteger().toString(16)).append(nl);

        return buf.toString();

    }
    
    public void setPointFormat(String style)
    {
       withCompression = !("UNCOMPRESSED".equalsIgnoreCase(style));
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof BCECPublicKey))
        {
            return false;
        }

        BCECPublicKey other = (BCECPublicKey)o;

        return engineGetQ().equals(other.engineGetQ()) && (engineGetSpec().equals(other.engineGetSpec()));
    }

    public int hashCode()
    {
        return engineGetQ().hashCode() ^ engineGetSpec().hashCode();
    }

    private void readObject(
        ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        byte[] enc = (byte[])in.readObject();

        populateFromPubKeyInfo(SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(enc)));

        this.configuration = BouncyCastleProvider.CONFIGURATION;
    }

    private void writeObject(
        ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        out.writeObject(this.getEncoded());
    }
}
