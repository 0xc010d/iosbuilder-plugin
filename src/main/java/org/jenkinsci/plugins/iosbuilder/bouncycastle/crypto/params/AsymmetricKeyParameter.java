package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.CipherParameters;

public class AsymmetricKeyParameter
    implements CipherParameters
{
    boolean privateKey;

    public AsymmetricKeyParameter(
        boolean privateKey)
    {
        this.privateKey = privateKey;
    }

    public boolean isPrivate()
    {
        return privateKey;
    }
}
