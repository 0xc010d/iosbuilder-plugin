package org.jenkinsci.plugins.iosbuilder.bouncycastle.pqc.jcajce.spec;

import java.security.spec.KeySpec;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.pqc.crypto.gmss.GMSSParameters;

public class GMSSKeySpec
    implements KeySpec
{
    /**
     * The GMSSParameterSet
     */
    private GMSSParameters gmssParameterSet;

    protected GMSSKeySpec(GMSSParameters gmssParameterSet)
    {
        this.gmssParameterSet = gmssParameterSet;
    }

    /**
     * Returns the GMSS parameter set
     *
     * @return The GMSS parameter set
     */
    public GMSSParameters getParameters()
    {
        return gmssParameterSet;
    }
}
