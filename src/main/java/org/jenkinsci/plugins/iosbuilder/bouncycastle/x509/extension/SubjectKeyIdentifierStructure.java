package org.jenkinsci.plugins.iosbuilder.bouncycastle.x509.extension;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1OctetString;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 * A high level subject key identifier.
 * @deprecated use JcaX509ExtensionUtils andSubjectKeyIdentifier.getInstance()
 */
public class SubjectKeyIdentifierStructure
    extends SubjectKeyIdentifier
{
    /**
     * Constructor which will take the byte[] returned from getExtensionValue()
     * 
     * @param encodedValue a DER octet encoded string with the extension structure in it.
     * @throws IOException on parsing errors.
     */
    public SubjectKeyIdentifierStructure(
        byte[]  encodedValue)
        throws IOException
    {
        super((ASN1OctetString)X509ExtensionUtil.fromExtensionValue(encodedValue));
    }
    
    private static ASN1OctetString fromPublicKey(
        PublicKey pubKey)
        throws InvalidKeyException
    {
        try
        {
            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pubKey.getEncoded());

            return (ASN1OctetString)(new SubjectKeyIdentifier(info).toASN1Object());
        }
        catch (Exception e)
        {
            throw new InvalidKeyException("Exception extracting key details: " + e.toString());
        }
    }
    
    public SubjectKeyIdentifierStructure(
        PublicKey pubKey)
        throws InvalidKeyException
    {
        super(fromPublicKey(pubKey));
    }
}
