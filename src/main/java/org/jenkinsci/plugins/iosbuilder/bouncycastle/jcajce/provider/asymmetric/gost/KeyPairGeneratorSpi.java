package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.asymmetric.gost;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.generators.GOST3410KeyPairGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.GOST3410KeyGenerationParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.GOST3410Parameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.GOST3410PrivateKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.GOST3410PublicKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.GOST3410ParameterSpec;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.GOST3410PublicKeyParameterSetSpec;

public class KeyPairGeneratorSpi
    extends java.security.KeyPairGenerator
{
    GOST3410KeyGenerationParameters param;
    GOST3410KeyPairGenerator engine = new GOST3410KeyPairGenerator();
    GOST3410ParameterSpec gost3410Params;
    int strength = 1024;
    SecureRandom random = null;
    boolean initialised = false;

    public KeyPairGeneratorSpi()
    {
        super("GOST3410");
    }

    public void initialize(
        int strength,
        SecureRandom random)
    {
        this.strength = strength;
        this.random = random;
    }

    private void init(
        GOST3410ParameterSpec gParams,
        SecureRandom random)
    {
        GOST3410PublicKeyParameterSetSpec spec = gParams.getPublicKeyParameters();

        param = new GOST3410KeyGenerationParameters(random, new GOST3410Parameters(spec.getP(), spec.getQ(), spec.getA()));

        engine.init(param);

        initialised = true;
        gost3410Params = gParams;
    }

    public void initialize(
        AlgorithmParameterSpec params,
        SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        if (!(params instanceof GOST3410ParameterSpec))
        {
            throw new InvalidAlgorithmParameterException("parameter object not a GOST3410ParameterSpec");
        }

        init((GOST3410ParameterSpec)params, random);
    }

    public KeyPair generateKeyPair()
    {
        if (!initialised)
        {
            init(new GOST3410ParameterSpec(CryptoProObjectIdentifiers.gostR3410_94_CryptoPro_A.getId()), new SecureRandom());
        }

        AsymmetricCipherKeyPair pair = engine.generateKeyPair();
        GOST3410PublicKeyParameters pub = (GOST3410PublicKeyParameters)pair.getPublic();
        GOST3410PrivateKeyParameters priv = (GOST3410PrivateKeyParameters)pair.getPrivate();

        return new KeyPair(new BCGOST3410PublicKey(pub, gost3410Params), new BCGOST3410PrivateKey(priv, gost3410Params));
    }
}
