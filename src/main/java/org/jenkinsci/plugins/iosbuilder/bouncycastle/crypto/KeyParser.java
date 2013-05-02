package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto;

import java.io.IOException;
import java.io.InputStream;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface KeyParser
{
    AsymmetricKeyParameter readKey(InputStream stream)
        throws IOException;
}
