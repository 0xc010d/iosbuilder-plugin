package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.tls;

import java.security.SecureRandom;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.CryptoException;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.Signer;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.params.AsymmetricKeyParameter;

interface TlsSigner
{
    byte[] calculateRawSignature(SecureRandom random, AsymmetricKeyParameter privateKey, byte[] md5andsha1)
        throws CryptoException;

    Signer createVerifyer(AsymmetricKeyParameter publicKey);

    boolean isValidPublicKey(AsymmetricKeyParameter publicKey);
}
