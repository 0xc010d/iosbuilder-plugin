package org.jenkinsci.plugins.iosbuilder.signing;

import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.ASN1InputStream;
import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.ASN1OctetString;
import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.cms.ContentInfo;
import org.jenkinsci.plugins.iosbuilder.signing.bouncycastle.asn1.cms.SignedData;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Mobileprovision {

    private final static String nameXPath = "//dict/key[text()='Name']/following-sibling::string[1]/text()";
    private final static String uuidXPath = "//dict/key[text()='UUID']/following-sibling::string[1]/text()";
    private final static String applicationIdentifierXPath = "//dict/key[text()='Entitlements']/following-sibling::dict[1]/key[text()='application-identifier']/following-sibling::string[1]/text()";
    private final static String certificatesXPath = "//dict/key[text()='DeveloperCertificates']/following-sibling::array[1]/data/text()";

    private byte[] bytes;
    private String name;
    private String UUID;
    private String applicationIdentifier;
    private Certificate[] certificates;

    public byte[] getBytes() { return bytes; }
    public String getName() { return name; }
    public String getUUID() { return UUID; }
    public String getApplicationIdentifier() { return applicationIdentifier; }
    public Certificate[] getCertificates() { return certificates; }

    Mobileprovision(byte[] bytes) throws Exception {
        ContentInfo contentInfo = ContentInfo.getInstance(new ASN1InputStream(bytes).readObject());
        SignedData signedData = SignedData.getInstance(contentInfo.getContent());
        byte[] plist = ((ASN1OctetString)(signedData.getEncapContentInfo().getContent())).getOctets();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        //Ignore DTD
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
            }
        });
        Document document = builder.parse(new ByteArrayInputStream(plist));

        XPath xPath = XPathFactory.newInstance().newXPath();

        this.bytes = bytes;
        this.name = xPath.evaluate(nameXPath, document);
        this.UUID = xPath.evaluate(uuidXPath, document);
        this.applicationIdentifier = xPath.evaluate(applicationIdentifierXPath, document);

        NodeList certificateNodes = (NodeList)xPath.evaluate(certificatesXPath, document, XPathConstants.NODESET);
        this.certificates = new Certificate[certificateNodes.getLength()];
        for (int index = 0; index < certificateNodes.getLength(); index++) {
            String encodedCertificate = certificateNodes.item(index).getTextContent().replaceAll("\\s+", "");
            this.certificates[index] = CertificateFactory.newInstance(encodedCertificate);
        }
    }
}
