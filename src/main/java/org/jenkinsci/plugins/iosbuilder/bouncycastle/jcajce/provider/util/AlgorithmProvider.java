package org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.util;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.jcajce.provider.config.ConfigurableProvider;

public abstract class AlgorithmProvider
{
    public abstract void configure(ConfigurableProvider provider);
}
