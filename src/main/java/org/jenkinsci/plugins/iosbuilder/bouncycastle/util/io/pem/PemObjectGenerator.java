package org.jenkinsci.plugins.iosbuilder.bouncycastle.util.io.pem;

public interface PemObjectGenerator
{
    PemObject generate()
        throws PemGenerationException;
}
