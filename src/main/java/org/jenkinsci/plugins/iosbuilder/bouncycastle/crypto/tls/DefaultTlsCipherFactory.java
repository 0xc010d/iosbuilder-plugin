package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.tls;

import java.io.IOException;

import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.BlockCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.Digest;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.StreamCipher;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.digests.MD5Digest;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.digests.SHA1Digest;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.digests.SHA256Digest;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.digests.SHA384Digest;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.AESFastEngine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.DESedeEngine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.engines.RC4Engine;
import org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.modes.CBCBlockCipher;

public class DefaultTlsCipherFactory implements TlsCipherFactory
{
    public TlsCipher createCipher(TlsClientContext context, int encryptionAlgorithm, int digestAlgorithm) throws IOException
    {
        switch (encryptionAlgorithm)
        {
            case EncryptionAlgorithm._3DES_EDE_CBC:
                return createDESedeCipher(context, 24, digestAlgorithm);
            case EncryptionAlgorithm.AES_128_CBC:
                return createAESCipher(context, 16, digestAlgorithm);
            case EncryptionAlgorithm.AES_256_CBC:
                return createAESCipher(context, 32, digestAlgorithm);
            case EncryptionAlgorithm.RC4_128:
                return createRC4Cipher(context, 16, digestAlgorithm);
            default:
                throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }

    protected TlsCipher createAESCipher(TlsClientContext context, int cipherKeySize, int digestAlgorithm) throws IOException
    {
        return new TlsBlockCipher(context, createAESBlockCipher(),
            createAESBlockCipher(), createDigest(digestAlgorithm), createDigest(digestAlgorithm), cipherKeySize);
    }

    protected TlsCipher createRC4Cipher(TlsClientContext context, int cipherKeySize, int digestAlgorithm) throws IOException
    {
        return new TlsStreamCipher(context, createRC4StreamCipher(), createRC4StreamCipher(),
            createDigest(digestAlgorithm), createDigest(digestAlgorithm), cipherKeySize);
    }

    protected TlsCipher createDESedeCipher(TlsClientContext context, int cipherKeySize, int digestAlgorithm) throws IOException
    {
        return new TlsBlockCipher(context, createDESedeBlockCipher(),
            createDESedeBlockCipher(), createDigest(digestAlgorithm), createDigest(digestAlgorithm), cipherKeySize);
    }

    protected StreamCipher createRC4StreamCipher()
    {
        return new RC4Engine();
    }

    protected BlockCipher createAESBlockCipher()
    {
        return new CBCBlockCipher(new AESFastEngine());
    }

    protected BlockCipher createDESedeBlockCipher()
    {
        return new CBCBlockCipher(new DESedeEngine());
    }

    protected Digest createDigest(int digestAlgorithm) throws IOException
    {
        switch (digestAlgorithm)
        {
            case DigestAlgorithm.MD5:
                return new MD5Digest();
            case DigestAlgorithm.SHA:
                return new SHA1Digest();
            case DigestAlgorithm.SHA256:
                return new SHA256Digest();
            case DigestAlgorithm.SHA384:
                return new SHA384Digest();
            default:
                throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }
}
