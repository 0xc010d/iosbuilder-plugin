package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.KeyParser;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DHParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DHPublicKeyParameters;

public class DHIESPublicKeyParser
    implements KeyParser
{
    private DHParameters dhParams;

    public DHIESPublicKeyParser(DHParameters dhParams)
    {
        this.dhParams = dhParams;
    }

    public AsymmetricKeyParameter readKey(InputStream stream)
        throws IOException
    {
        byte[] V = new byte[(dhParams.getP().bitLength() + 7) / 8];

        stream.read(V, 0, V.length);

        return new DHPublicKeyParameters(new BigInteger(1, V), dhParams);
    }
}
