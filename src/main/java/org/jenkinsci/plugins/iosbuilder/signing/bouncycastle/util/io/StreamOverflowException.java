package org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.util.io;

import java.io.IOException;

public class StreamOverflowException
    extends IOException
{
    public StreamOverflowException(String msg)
    {
        super(msg);
    }
}
