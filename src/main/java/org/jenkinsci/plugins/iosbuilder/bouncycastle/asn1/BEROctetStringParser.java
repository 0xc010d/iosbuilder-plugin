package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1;

import java.io.IOException;
import java.io.InputStream;

public class BEROctetStringParser
    implements ASN1OctetStringParser
{
    private ASN1StreamParser _parser;

    BEROctetStringParser(
        ASN1StreamParser parser)
    {
        _parser = parser;
    }

    public InputStream getOctetStream()
    {
        return new ConstructedOctetStream(_parser);
    }

    public ASN1Primitive getLoadedObject()
        throws IOException
    {
        return new BEROctetString(org.jenkinsci.plugins.iosbuilder.bouncycastle.util.io.Streams.readAll(getOctetStream()));
    }

    public ASN1Primitive toASN1Primitive()
    {
        try
        {
            return getLoadedObject();
        }
        catch (IOException e)
        {
            throw new ASN1ParsingException("IOException converting stream to byte array: " + e.getMessage(), e);
        }
    }
}
