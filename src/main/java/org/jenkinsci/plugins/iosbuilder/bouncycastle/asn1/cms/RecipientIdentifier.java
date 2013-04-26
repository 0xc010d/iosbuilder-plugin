package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.cms;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Choice;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1Primitive;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.ASN1TaggedObject;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.DERTaggedObject;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.*;

public class RecipientIdentifier
    extends ASN1Object
    implements ASN1Choice
{
    private ASN1Encodable id;
    
    public RecipientIdentifier(
        IssuerAndSerialNumber id)
    {
        this.id = id;
    }
    
    public RecipientIdentifier(
        ASN1OctetString id)
    {
        this.id = new DERTaggedObject(false, 0, id);
    }
    
    public RecipientIdentifier(
        ASN1Primitive id)
    {
        this.id = id;
    }
    
    /**
     * return a RecipientIdentifier object from the given object.
     *
     * @param o the object we want converted.
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static RecipientIdentifier getInstance(
        Object o)
    {
        if (o == null || o instanceof RecipientIdentifier)
        {
            return (RecipientIdentifier)o;
        }
        
        if (o instanceof IssuerAndSerialNumber)
        {
            return new RecipientIdentifier((IssuerAndSerialNumber)o);
        }
        
        if (o instanceof ASN1OctetString)
        {
            return new RecipientIdentifier((ASN1OctetString)o);
        }
        
        if (o instanceof ASN1Primitive)
        {
            return new RecipientIdentifier((ASN1Primitive)o);
        }
        
        throw new IllegalArgumentException(
          "Illegal object in RecipientIdentifier: " + o.getClass().getName());
    } 

    public boolean isTagged()
    {
        return (id instanceof ASN1TaggedObject);
    }

    public ASN1Encodable getId()
    {
        if (id instanceof ASN1TaggedObject)
        {
            return ASN1OctetString.getInstance((ASN1TaggedObject) id, false);
        }

        return IssuerAndSerialNumber.getInstance(id);
    }

    /** 
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * RecipientIdentifier ::= CHOICE {
     *     issuerAndSerialNumber IssuerAndSerialNumber,
     *     subjectKeyIdentifier [0] SubjectKeyIdentifier 
     * }
     *
     * SubjectKeyIdentifier ::= OCTET STRING
     * </pre>
     */
    public ASN1Primitive toASN1Primitive()
    {
        return id.toASN1Primitive();
    }
}
