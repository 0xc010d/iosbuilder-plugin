package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.IvParameterSpec;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.CipherKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.CamelliaEngine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.CamelliaWrapEngine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.RFC3211WrapEngine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.modes.CBCBlockCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseAlgorithmParameterGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.IvAlgorithmParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.provider.BouncyCastleProvider;

public final class Camellia
{
    private Camellia()
    {
    }
    
    public static class ECB
        extends BaseBlockCipher
    {
        public ECB()
        {
            super(new CamelliaEngine());
        }
    }

    public static class CBC
       extends BaseBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new CamelliaEngine()), 128);
        }
    }

    public static class Wrap
        extends BaseWrapCipher
    {
        public Wrap()
        {
            super(new CamelliaWrapEngine());
        }
    }

    public static class RFC3211Wrap
        extends BaseWrapCipher
    {
        public RFC3211Wrap()
        {
            super(new RFC3211WrapEngine(new CamelliaEngine()), 16);
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
            super("Camellia", keySize, new CipherKeyGenerator());
        }
    }

    public static class KeyGen128
        extends KeyGen
    {
        public KeyGen128()
        {
            super(128);
        }
    }

    public static class KeyGen192
        extends KeyGen
    {
        public KeyGen192()
        {
            super(192);
        }
    }

    public static class KeyGen256
        extends KeyGen
    {
        public KeyGen256()
        {
            super(256);
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
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for Camellia parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            byte[] iv = new byte[16];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(iv);

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("Camellia", BouncyCastleProvider.PROVIDER_NAME);
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
            return "Camellia IV";
        }
    }

    public static class Mappings
        extends AlgorithmProvider
    {
        private static final String PREFIX = Camellia.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {

            provider.addAlgorithm("AlgorithmParameters.CAMELLIA", PREFIX + "$AlgParams");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameters." + NTTObjectIdentifiers.id_camellia128_cbc, "CAMELLIA");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameters." + NTTObjectIdentifiers.id_camellia192_cbc, "CAMELLIA");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameters." + NTTObjectIdentifiers.id_camellia256_cbc, "CAMELLIA");

            provider.addAlgorithm("AlgorithmParameterGenerator.CAMELLIA", PREFIX + "$AlgParamGen");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameterGenerator." + NTTObjectIdentifiers.id_camellia128_cbc, "CAMELLIA");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameterGenerator." + NTTObjectIdentifiers.id_camellia192_cbc, "CAMELLIA");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameterGenerator." + NTTObjectIdentifiers.id_camellia256_cbc, "CAMELLIA");

            provider.addAlgorithm("Cipher.CAMELLIA", PREFIX + "$ECB");
            provider.addAlgorithm("Cipher." + NTTObjectIdentifiers.id_camellia128_cbc, PREFIX + "$CBC");
            provider.addAlgorithm("Cipher." + NTTObjectIdentifiers.id_camellia192_cbc, PREFIX + "$CBC");
            provider.addAlgorithm("Cipher." + NTTObjectIdentifiers.id_camellia256_cbc, PREFIX + "$CBC");

            provider.addAlgorithm("Cipher.CAMELLIARFC3211WRAP", PREFIX + "$RFC3211Wrap");
            provider.addAlgorithm("Cipher.CAMELLIAWRAP", PREFIX + "$Wrap");
            provider.addAlgorithm("Alg.Alias.Cipher." + NTTObjectIdentifiers.id_camellia128_wrap, "CAMELLIAWRAP");
            provider.addAlgorithm("Alg.Alias.Cipher." + NTTObjectIdentifiers.id_camellia192_wrap, "CAMELLIAWRAP");
            provider.addAlgorithm("Alg.Alias.Cipher." + NTTObjectIdentifiers.id_camellia256_wrap, "CAMELLIAWRAP");

            provider.addAlgorithm("KeyGenerator.CAMELLIA", PREFIX + "$KeyGen");
            provider.addAlgorithm("KeyGenerator." + NTTObjectIdentifiers.id_camellia128_wrap, PREFIX + "$KeyGen128");
            provider.addAlgorithm("KeyGenerator." + NTTObjectIdentifiers.id_camellia192_wrap, PREFIX + "$KeyGen192");
            provider.addAlgorithm("KeyGenerator." + NTTObjectIdentifiers.id_camellia256_wrap, PREFIX + "$KeyGen256");
            provider.addAlgorithm("KeyGenerator." + NTTObjectIdentifiers.id_camellia128_cbc, PREFIX + "$KeyGen128");
            provider.addAlgorithm("KeyGenerator." + NTTObjectIdentifiers.id_camellia192_cbc, PREFIX + "$KeyGen192");
            provider.addAlgorithm("KeyGenerator." + NTTObjectIdentifiers.id_camellia256_cbc, PREFIX + "$KeyGen256");

        }
    }
}
