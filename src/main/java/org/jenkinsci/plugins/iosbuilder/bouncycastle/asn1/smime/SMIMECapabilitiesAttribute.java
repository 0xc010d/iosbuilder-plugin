package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.smime;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.cms.Attribute;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERSequence;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERSet;

public class SMIMECapabilitiesAttribute
    extends Attribute
{
    public SMIMECapabilitiesAttribute(
        SMIMECapabilityVector capabilities)
    {
        super(SMIMEAttributes.smimeCapabilities,
                new DERSet(new DERSequence(capabilities.toASN1EncodableVector())));
    }
}
