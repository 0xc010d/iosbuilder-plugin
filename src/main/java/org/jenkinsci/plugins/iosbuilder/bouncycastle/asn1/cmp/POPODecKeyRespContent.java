package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.cmp;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Integer;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Object;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Primitive;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Sequence;

public class POPODecKeyRespContent
    extends ASN1Object
{
    private ASN1Sequence content;

    private POPODecKeyRespContent(ASN1Sequence seq)
    {
        content = seq;
    }

    public static POPODecKeyRespContent getInstance(Object o)
    {
        if (o instanceof POPODecKeyRespContent)
        {
            return (POPODecKeyRespContent)o;
        }

        if (o != null)
        {
            return new POPODecKeyRespContent(ASN1Sequence.getInstance(o));
        }

        return null;
    }

    public ASN1Integer[] toASN1IntegerArray()
    {
        ASN1Integer[] result = new ASN1Integer[content.size()];

        for (int i = 0; i != result.length; i++)
        {
            result[i] = ASN1Integer.getInstance(content.getObjectAt(i));
        }

        return result;
    }

    /**
     * <pre>
     * POPODecKeyRespContent ::= SEQUENCE OF INTEGER
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public ASN1Primitive toASN1Primitive()
    {
        return content;
    }
}
