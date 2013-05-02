package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.tls;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.DSA;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.signers.DSASigner;

class TlsDSSSigner extends TlsDSASigner
{
    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof DSAPublicKeyParameters;
    }

    protected DSA createDSAImpl()
    {
        return new DSASigner();
    }
}
