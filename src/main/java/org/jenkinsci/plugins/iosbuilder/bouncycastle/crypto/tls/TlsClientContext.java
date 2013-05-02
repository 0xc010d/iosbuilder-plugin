package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.tls;

import java.security.SecureRandom;

public interface TlsClientContext
{
    SecureRandom getSecureRandom();

    SecurityParameters getSecurityParameters();

    ProtocolVersion getClientVersion();

    ProtocolVersion getServerVersion();

    Object getUserObject();

    void setUserObject(Object userObject);
}
