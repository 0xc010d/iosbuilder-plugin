package org.jenkinsci.plugins.iosbuilder;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;

import java.util.logging.Logger;

public class Mobileprovision {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    public Mobileprovision(byte[] data) {
        try {
            ContentInfo contentInfo = ContentInfo.getInstance(new ASN1InputStream(data).readObject());
            SignedData signedData = SignedData.getInstance(contentInfo.getContent());
            byte[] result = ((ASN1OctetString)(signedData.getEncapContentInfo().getContent())).getOctets();

            LOG.info(new String(result, "UTF-8"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}