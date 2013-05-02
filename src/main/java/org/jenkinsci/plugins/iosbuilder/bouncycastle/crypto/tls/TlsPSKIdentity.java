package org.jenkinsci.plugins.iosbuilder.bouncycastle.crypto.tls;

public interface TlsPSKIdentity
{
	void skipIdentityHint();

	void notifyIdentityHint(byte[] psk_identity_hint);

	byte[] getPSKIdentity();

	byte[] getPSK();
}
