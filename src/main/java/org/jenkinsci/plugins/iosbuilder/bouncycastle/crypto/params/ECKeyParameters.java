package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params;

public class ECKeyParameters
    extends AsymmetricKeyParameter
{
    ECDomainParameters params;

    protected ECKeyParameters(
        boolean             isPrivate,
        ECDomainParameters  params)
    {
        super(isPrivate);

        this.params = params;
    }

    public ECDomainParameters getParameters()
    {
        return params;
    }
}
