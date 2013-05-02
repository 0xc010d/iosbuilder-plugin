package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.generators;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.EphemeralKeyPair;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.KeyEncoder;

public class EphemeralKeyPairGenerator
{
    private AsymmetricCipherKeyPairGenerator gen;
    private KeyEncoder keyEncoder;

    public EphemeralKeyPairGenerator(AsymmetricCipherKeyPairGenerator gen, KeyEncoder keyEncoder)
    {
        this.gen = gen;
        this.keyEncoder = keyEncoder;
    }

    public EphemeralKeyPair generate()
    {
        AsymmetricCipherKeyPair eph = gen.generateKeyPair();

        // Encode the ephemeral public key
     	return new EphemeralKeyPair(eph, keyEncoder);
    }
}
