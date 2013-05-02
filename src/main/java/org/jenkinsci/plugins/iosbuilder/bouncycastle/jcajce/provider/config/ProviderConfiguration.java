package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.config;

import javax.crypto.spec.DHParameterSpec;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ECParameterSpec;

public interface ProviderConfiguration
{
    ECParameterSpec getEcImplicitlyCa();

    DHParameterSpec getDHDefaultParameters(int keySize);
}
