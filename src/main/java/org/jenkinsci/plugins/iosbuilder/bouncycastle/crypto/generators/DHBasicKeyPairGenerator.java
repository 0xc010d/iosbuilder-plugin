package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.generators;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.KeyGenerationParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DHParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DHPublicKeyParameters;

import java.math.BigInteger;

/**
 * a basic Diffie-Hellman key pair generator.
 *
 * This generates keys consistent for use with the basic algorithm for
 * Diffie-Hellman.
 */
public class DHBasicKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator
{
    private DHKeyGenerationParameters param;

    public void init(
        KeyGenerationParameters param)
    {
        this.param = (DHKeyGenerationParameters)param;
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        DHKeyGeneratorHelper helper = DHKeyGeneratorHelper.INSTANCE;
        DHParameters dhp = param.getParameters();

        BigInteger x = helper.calculatePrivate(dhp, param.getRandom()); 
        BigInteger y = helper.calculatePublic(dhp, x);

        return new AsymmetricCipherKeyPair(
            new DHPublicKeyParameters(y, dhp),
            new DHPrivateKeyParameters(x, dhp));
    }
}
