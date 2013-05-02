package org.jenkinsci.plugins.iosbuilder.bouncycastle.pqc.crypto.gmss;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.Digest;

public interface GMSSDigestProvider
{
    Digest get();
}
