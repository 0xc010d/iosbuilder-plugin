package org.jenkinsci.plugins.iosbuilder.signing;

import com.dd.plist.*;
import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.ASN1InputStream;
import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.ASN1OctetString;
import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.cms.ContentInfo;
import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.cms.SignedData;

public class Mobileprovision {
    private final byte[] bytes;
    private final String name;
    private final String UUID;
    private final String applicationIdentifier;
    private final Certificate[] certificates;

    public byte[] getBytes() { return bytes; }
    public String getName() { return name; }
    public String getUUID() { return UUID; }
    public String getApplicationIdentifier() { return applicationIdentifier; }
    public Certificate[] getCertificates() { return certificates; }

    Mobileprovision(byte[] bytes) throws Exception {
        this.bytes = bytes;

        ContentInfo contentInfo = ContentInfo.getInstance(new ASN1InputStream(bytes).readObject());
        SignedData signedData = SignedData.getInstance(contentInfo.getContent());
        byte[] plist = ((ASN1OctetString)(signedData.getEncapContentInfo().getContent())).getOctets();

        NSDictionary root = (NSDictionary)PropertyListParser.parse(plist);
        name = root.objectForKey("Name").toString();
        UUID = root.objectForKey("UUID").toString();
        NSDictionary entitlements = (NSDictionary)root.objectForKey("Entitlements");
        applicationIdentifier = entitlements.objectForKey("application-identifier").toString();
        NSObject[] developerCertificates = ((NSArray)root.objectForKey("DeveloperCertificates")).getArray();
        certificates = new Certificate[developerCertificates.length];
        for (int index = 0; index < developerCertificates.length; index++) {
            byte[] certificateData = ((NSData)developerCertificates[index]).bytes();
            this.certificates[index] = CertificateFactory.newInstance(certificateData);
        }
    }
}
