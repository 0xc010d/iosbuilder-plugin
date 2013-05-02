package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.DerivationParameters;

/**
 * parameters for Key derivation functions for IEEE P1363a
 */
public class KDFParameters
    implements DerivationParameters
{
    byte[]  iv;
    byte[]  shared;

    public KDFParameters(
        byte[]  shared,
        byte[]  iv)
    {
        this.shared = shared;
        this.iv = iv;
    }

    public byte[] getSharedSecret()
    {
        return shared;
    }

    public byte[] getIV()
    {
        return iv;
    }
}
