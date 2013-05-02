package org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.interfaces;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.jce.spec.ElGamalParameterSpec;

public interface ElGamalKey
{
    public ElGamalParameterSpec getParameters();
}
