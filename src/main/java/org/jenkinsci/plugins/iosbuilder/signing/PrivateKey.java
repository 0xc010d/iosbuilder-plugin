package org.jenkinsci.plugins.iosbuilder.signing;

import java.security.PublicKey;
import java.security.Signature;
import java.util.UUID;

public class PrivateKey {
    private java.security.PrivateKey privateKey;
    private String alias;
    private byte[] checkData;
    private byte[] checkSignature = null;

    private PrivateKey(String alias, java.security.PrivateKey privateKey) throws Exception {
        try {
            this.alias = alias;
            this.privateKey = privateKey;
            Signature signature = Signature.getInstance("SHA1withRSA");
            checkData = UUID.randomUUID().toString().getBytes();
            signature.initSign(this.privateKey);
            signature.update(checkData);
            checkSignature = signature.sign();
        }
        catch (Exception e) {}
        if (checkSignature == null || checkSignature.length == 0) {
            throw new Exception("We were not able to get signature");
        }
    }

    public static PrivateKey getInstance(String alias, java.security.PrivateKey privateKey) {
        try {
            return new PrivateKey(alias, privateKey);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey getInstance(String alias, java.security.KeyStore keyStore, char[] password) {
        try {
            return new PrivateKey(alias, (java.security.PrivateKey)keyStore.getKey(alias, password));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
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
