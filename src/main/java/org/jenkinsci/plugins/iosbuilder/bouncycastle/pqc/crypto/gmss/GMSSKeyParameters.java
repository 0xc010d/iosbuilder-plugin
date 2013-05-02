package org.jenkinsci.plugins.iosbuilder.bouncycastle.pqc.crypto.gmss;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class GMSSKeyParameters
    extends AsymmetricKeyParameter
{
    private GMSSParameters params;

    public GMSSKeyParameters(
        boolean isPrivate,
        GMSSParameters params)
    {
        super(isPrivate);
        this.params = params;
    }

    public GMSSParameters getParameters()
    {
        return params;
    }
}