package org.jenkinsci.plugins.iosbuilder.signing;

import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.UUID;

public class PrivateKey {
    private final java.security.PrivateKey privateKey;
    private final String alias;
    private final byte[] checkData;
    private final byte[] checkSignature;

    public String getAlias() { return alias; }
    java.security.PrivateKey getPrivateKey() { return privateKey; }

    PrivateKey(String alias, java.security.PrivateKey privateKey) throws IOException {
        try {
            this.alias = alias;
            this.privateKey = privateKey;

            Signature signature = Signature.getInstance("SHA1withRSA");
            checkData = UUID.randomUUID().toString().getBytes();
            signature.initSign(privateKey);
            signature.update(checkData);
            checkSignature = signature.sign();
        }
        catch (Exception e) {
            throw new IOException("Can not instantiate private key object");
        }
        if (checkSignature == null || checkSignature.length == 0) {
            throw new IOException("Can not get signature");
        }
    }

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
