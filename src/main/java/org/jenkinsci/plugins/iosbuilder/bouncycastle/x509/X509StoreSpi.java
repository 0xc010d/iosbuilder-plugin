package org.jenkinsci.plugins.iosbuilder.bouncycastle.x509;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.util.Selector;

import java.util.Collection;

public abstract class X509StoreSpi
{
    public abstract void engineInit(X509StoreParameters parameters);

    public abstract Collection engineGetMatches(Selector selector);
}
