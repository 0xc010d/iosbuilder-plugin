package org.jenkinsci.plugins.iosbuilder.bouncycastle.util.test;

public interface TestResult
{
    public boolean isSuccessful();
    
    public Throwable getException();
    
    public String toString();
}
