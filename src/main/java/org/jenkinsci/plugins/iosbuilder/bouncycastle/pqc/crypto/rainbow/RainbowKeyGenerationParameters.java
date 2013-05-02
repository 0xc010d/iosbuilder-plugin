package org.jenkinsci.plugins.iosbuilder.bouncycastle.pqc.crypto.rainbow;

import java.security.SecureRandom;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.KeyGenerationParameters;

public class RainbowKeyGenerationParameters
    extends KeyGenerationParameters
{
    private RainbowParameters params;

    public RainbowKeyGenerationParameters(
        SecureRandom random,
        RainbowParameters params)
    {
        // TODO: key size?
        super(random, params.getVi()[params.getVi().length - 1] - params.getVi()[0]);
        this.params = params;
    }

    public RainbowParameters getParameters()
    {
        return params;
    }
}

