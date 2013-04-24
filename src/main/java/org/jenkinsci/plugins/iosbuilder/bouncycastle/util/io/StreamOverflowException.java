package org.jenkinsci.plugins.iosbuilder.bouncycastle.util.io;

import java.io.IOException;

public class StreamOverflowException
    extends IOException
{
    public StreamOverflowException(String msg)
    {
        super(msg);
    }
}
