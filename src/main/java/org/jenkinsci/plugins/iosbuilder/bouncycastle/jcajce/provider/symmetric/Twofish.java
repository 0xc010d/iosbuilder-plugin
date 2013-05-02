package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.CipherKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.TwofishEngine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.IvAlgorithmParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.util.AlgorithmProvider;

public final class Twofish
{
    private Twofish()
    {
    }
    
    public static class ECB
        extends BaseBlockCipher
    {
        public ECB()
        {
            super(new TwofishEngine());
        }
    }

    public static class KeyGen
        extends BaseKeyGenerator
    {
        public KeyGen()
        {
            super("Twofish", 256, new CipherKeyGenerator());
        }
    }

    public static class AlgParams
        extends IvAlgorithmParameters
    {
        protected String engineToString()
        {
            return "Twofish IV";
        }
    }

    public static class Mappings
        extends AlgorithmProvider
    {
        private static final String PREFIX = Twofish.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {

            provider.addAlgorithm("Cipher.Twofish", PREFIX + "$ECB");
            provider.addAlgorithm("KeyGenerator.Twofish", PREFIX + "$KeyGen");
            provider.addAlgorithm("AlgorithmParameters.Twofish", PREFIX + "$AlgParams");

        }
    }
}
