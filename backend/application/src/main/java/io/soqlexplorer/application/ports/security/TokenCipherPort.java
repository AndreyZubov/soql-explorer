package io.soqlexplorer.application.ports.security;

import io.soqlexplorer.domain.connection.EncryptedToken;

/**
 * Outbound port for symmetric encryption of Salesforce refresh tokens.
 *
 * <p>The concrete adapter (Step 2) uses AES-GCM with a key sourced from configuration / Vault.
 * Keeping it behind a port lets us unit-test use cases with an in-memory implementation that
 * never touches the JCE.
 */
public interface TokenCipherPort {

  EncryptedToken encrypt(String plaintext);

  String decrypt(EncryptedToken token);
}
