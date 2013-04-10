package org.jenkinsci.plugins.iosbuilder;

import java.security.PublicKey;
import java.security.Signature;
import java.util.UUID;

public class PrivateKey {
    private java.security.PrivateKey privateKey;
    private String alias;
    private byte[] checkData;
    private byte[] checkSignature;

    public PrivateKey(String alias, java.security.PrivateKey privateKey) {
        this.alias = alias;
        this.privateKey = privateKey;
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            checkData = UUID.randomUUID().toString().getBytes();
            signature.initSign(this.privateKey);
            signature.update(checkData);
            checkSignature = signature.sign();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAlias() { return alias; }

    public boolean checkPublicKey(PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(checkData);
            return signature.verify(checkSignature);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
