package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.IvParameterSpec;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.CipherKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.GOST28147Engine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.macs.GOST28147Mac;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.modes.CBCBlockCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseAlgorithmParameterGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.IvAlgorithmParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.provider.BouncyCastleProvider;

public final class GOST28147
{
    private GOST28147()
    {
    }
    
    public static class ECB
        extends BaseBlockCipher
    {
        public ECB()
        {
            super(new GOST28147Engine());
        }
    }

    public static class CBC
       extends BaseBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new GOST28147Engine()), 64);
        }
    }

    /**
     * GOST28147
     */
    public static class Mac
        extends BaseMac
    {
        public Mac()
        {
            super(new GOST28147Mac());
        }
    }

    public static class KeyGen
        extends BaseKeyGenerator
    {
        public KeyGen()
        {
            this(256);
        }

        public KeyGen(int keySize)
        {
            super("GOST28147", keySize, new CipherKeyGenerator());
        }
    }

    public static class AlgParamGen
        extends BaseAlgorithmParameterGenerator
    {
        protected void engineInit(
            AlgorithmParameterSpec genParamSpec,
            SecureRandom random)
            throws InvalidAlgorithmParameterException
        {
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for AES parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            byte[]  iv = new byte[16];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(iv);

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("GOST28147", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new IvParameterSpec(iv));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class AlgParams
        extends IvAlgorithmParameters
    {
        protected String engineToString()
        {
            return "GOST IV";
        }
    }

    public static class Mappings
        extends AlgorithmProvider
    {
        private static final String PREFIX = GOST28147.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("Cipher.GOST28147", PREFIX + "$ECB");
            provider.addAlgorithm("Alg.Alias.Cipher.GOST", "GOST28147");
            provider.addAlgorithm("Alg.Alias.Cipher.GOST-28147", "GOST28147");
            provider.addAlgorithm("Cipher." + CryptoProObjectIdentifiers.gostR28147_cbc, PREFIX + "$CBC");

            provider.addAlgorithm("KeyGenerator.GOST28147", PREFIX + "$KeyGen");
            provider.addAlgorithm("Alg.Alias.KeyGenerator.GOST", "GOST28147");
            provider.addAlgorithm("Alg.Alias.KeyGenerator.GOST-28147", "GOST28147");
            provider.addAlgorithm("Alg.Alias.KeyGenerator." + CryptoProObjectIdentifiers.gostR28147_cbc, "GOST28147");

            provider.addAlgorithm("Mac.GOST28147MAC", PREFIX + "$Mac");
            provider.addAlgorithm("Alg.Alias.Mac.GOST28147", "GOST28147MAC");
        }
    }
}
