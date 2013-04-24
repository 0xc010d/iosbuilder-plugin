package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Primitive;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Object;


/**
 * CertPolicyId, used in the CertificatePolicies and PolicyMappings
 * X509V3 Extensions.
 *
 * <pre>
 *     CertPolicyId ::= OBJECT IDENTIFIER
 * </pre>
 */
/**
 * CertPolicyId, used in the CertificatePolicies and PolicyMappings
 * X509V3 Extensions.
 *
 * <pre>
 *     CertPolicyId ::= OBJECT IDENTIFIER
 * </pre>
 */
public class CertPolicyId
    extends ASN1Object
{
    private ASN1ObjectIdentifier id;

    private CertPolicyId(ASN1ObjectIdentifier id)
    {
        this.id = id;
    }

    public static CertPolicyId getInstance(Object o)
    {
        if (o instanceof CertPolicyId)
        {
            return (CertPolicyId)o;
        }
        else if (o != null)
        {
            return new CertPolicyId(ASN1ObjectIdentifier.getInstance(o));
        }

        return null;
    }

    public String getId()
    {
        return id.getId();
    }

    public ASN1Primitive toASN1Primitive()
    {
        return id;
    }
}
