package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.smime;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

public interface SMIMEAttributes
{
    public static final ASN1ObjectIdentifier smimeCapabilities = PKCSObjectIdentifiers.pkcs_9_at_smimeCapabilities;
    public static final ASN1ObjectIdentifier  encrypKeyPref = PKCSObjectIdentifiers.id_aa_encrypKeyPref;
}
