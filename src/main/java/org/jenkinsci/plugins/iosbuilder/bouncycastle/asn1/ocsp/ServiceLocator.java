package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ocsp;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1EncodableVector;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Object;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Primitive;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERSequence;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x500.X500Name;

public class ServiceLocator
    extends ASN1Object
{
    X500Name issuer;
    ASN1Primitive locator;

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * ServiceLocator ::= SEQUENCE {
     *     issuer    Name,
     *     locator   AuthorityInfoAccessSyntax OPTIONAL }
     * </pre>
     */
    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector    v = new ASN1EncodableVector();

        v.add(issuer);

        if (locator != null)
        {
            v.add(locator);
        }

        return new DERSequence(v);
    }
}
