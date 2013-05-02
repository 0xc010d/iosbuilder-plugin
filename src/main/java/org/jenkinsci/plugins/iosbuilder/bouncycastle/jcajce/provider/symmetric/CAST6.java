package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.CipherKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.CAST6Engine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.util.AlgorithmProvider;

public final class CAST6
{
    private CAST6()
    {
    }
    
    public static class ECB
        extends BaseBlockCipher
    {
        public ECB()
        {
            super(new CAST6Engine());
        }
    }

    public static class KeyGen
        extends BaseKeyGenerator
    {
        public KeyGen()
        {
            super("CAST6", 256, new CipherKeyGenerator());
        }
    }

    public static class Mappings
        extends AlgorithmProvider
    {
        private static final String PREFIX = CAST6.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("Cipher.CAST6", PREFIX + "$ECB");
            provider.addAlgorithm("KeyGenerator.CAST6", PREFIX + "$KeyGen");
        }
    }
}
