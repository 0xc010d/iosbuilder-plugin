package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.misc;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERIA5String;

public class NetscapeRevocationURL
    extends DERIA5String
{
    public NetscapeRevocationURL(
        DERIA5String str)
    {
        super(str.getString());
    }

    public String toString()
    {
        return "NetscapeRevocationURL: " + this.getString();
    }
}
