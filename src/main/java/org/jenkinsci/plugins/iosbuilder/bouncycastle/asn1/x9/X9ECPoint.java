package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x9;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Object;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1OctetString;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Primitive;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DEROctetString;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECCurve;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.math.ec.ECPoint;

/**
 * class for describing an ECPoint as a DER object.
 */
public class X9ECPoint
    extends ASN1Object
{
    ECPoint p;

    public X9ECPoint(
        ECPoint p)
    {
        this.p = p;
    }

    public X9ECPoint(
        ECCurve          c,
        ASN1OctetString  s)
    {
        this.p = c.decodePoint(s.getOctets());
    }

    public ECPoint getPoint()
    {
        return p;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     *  ECPoint ::= OCTET STRING
     * </pre>
     * <p>
     * Octet string produced using ECPoint.getEncoded().
     */
    public ASN1Primitive toASN1Primitive()
    {
        return new DEROctetString(p.getEncoded());
    }
}
