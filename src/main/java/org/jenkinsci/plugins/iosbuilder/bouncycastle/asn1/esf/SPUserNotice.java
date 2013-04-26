package org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.esf;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.*;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.DisplayText;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.asn1.x509.NoticeReference;

import java.util.Enumeration;

public class SPUserNotice
    extends ASN1Object
{
    private NoticeReference noticeRef;
    private DisplayText explicitText;

    public static SPUserNotice getInstance(
        Object obj)
    {
        if (obj instanceof SPUserNotice)
        {
            return (SPUserNotice)obj;
        }
        else if (obj != null)
        {
            return new SPUserNotice(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    private SPUserNotice(
        ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements())
        {
            ASN1Encodable object = (ASN1Encodable)e.nextElement();
            if (object instanceof DisplayText || object instanceof ASN1String)
            {
                explicitText = DisplayText.getInstance(object);
            }
            else if (object instanceof NoticeReference || object instanceof ASN1Sequence)
            {
                noticeRef = NoticeReference.getInstance(object);
            }
            else
            {
                throw new IllegalArgumentException("Invalid element in 'SPUserNotice': " + object.getClass().getName());
            }
        }
    }

    public SPUserNotice(
        NoticeReference noticeRef,
        DisplayText explicitText)
    {
        this.noticeRef = noticeRef;
        this.explicitText = explicitText;
    }

    public NoticeReference getNoticeRef()
    {
        return noticeRef;
    }

    public DisplayText getExplicitText()
    {
        return explicitText;
    }

    /**
     * <pre>
     * SPUserNotice ::= SEQUENCE {
     *     noticeRef NoticeReference OPTIONAL,
     *     explicitText DisplayText OPTIONAL }
     * </pre>
     */
    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        if (noticeRef != null)
        {
            v.add(noticeRef);
        }

        if (explicitText != null)
        {
            v.add(explicitText);
        }

        return new DERSequence(v);
    }
}
