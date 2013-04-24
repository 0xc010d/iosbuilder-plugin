package org.jenkinsci.plugins.iosbuilder.bouncycastle.util.test;

public interface Test
{
    String getName();

    TestResult perform();
}
