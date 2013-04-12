package org.jenkinsci.plugins.iosbuilder;

import hudson.remoting.Base64;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;

import java.util.logging.Logger;

public class Mobileprovision {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());
    private final static String nameXPath = "//dict/key[text()='Name']/following-sibling::string[1]/text()";
    private final static String uuidXPath = "//dict/key[text()='UUID']/following-sibling::string[1]/text()";
    private final static String applicationIdentifierXPath = "//dict/key[text()='Entitlements']/following-sibling::dict[1]/key[text()='application-identifier']/following-sibling::string[1]/text()";
    private final static String certificatesXPath = "//dict/key[text()='DeveloperCertificates']/following-sibling::array[1]/data/text()";

    private String name;
    private String UUID;
    private String applicationIdentifier;
    private Certificate[] certificates;

    private Mobileprovision(byte[] data) {
        try {
            if (data != null && data.length != 0) {
                ContentInfo contentInfo = ContentInfo.getInstance(new ASN1InputStream(data).readObject());
                SignedData signedData = SignedData.getInstance(contentInfo.getContent());
                byte[] plist = ((ASN1OctetString)(signedData.getEncapContentInfo().getContent())).getOctets();

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(new ByteArrayInputStream(plist));
                XPath xPath = XPathFactory.newInstance().newXPath();

                this.name = (String)xPath.compile(nameXPath).evaluate(document, XPathConstants.STRING);
                this.UUID = (String)xPath.compile(uuidXPath).evaluate(document, XPathConstants.STRING);
                this.applicationIdentifier = (String)xPath.compile(applicationIdentifierXPath).evaluate(document, XPathConstants.STRING);

                NodeList certificateNodes = (NodeList)xPath.compile(certificatesXPath).evaluate(document, XPathConstants.NODESET);
                this.certificates = new Certificate[certificateNodes.getLength()];
                for (int index = 0; index < certificateNodes.getLength(); index++) {
                    String encodedCertificate = certificateNodes.item(index).getTextContent();
                    this.certificates[index] = Certificate.getInstance(Base64.decode(encodedCertificate));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Mobileprovision getInstance(byte[] data) {
        if (data != null && data.length != 0) {
            return new Mobileprovision(data);
        }
        return null;
    }

    public String getName() { return name; }
    public String getUUID() { return UUID; }
    public String getApplicationIdentifier() { return applicationIdentifier; }
    public Certificate[] getCertificates() { return certificates; }

    public boolean checkCertificate(Certificate certificate) {
        if (certificate != null) {
            for (Certificate developerCertificate : certificates) {
                if (certificate.equals(developerCertificate)) {
                    return true;
                }
            }
        }
        return false;
    }
}