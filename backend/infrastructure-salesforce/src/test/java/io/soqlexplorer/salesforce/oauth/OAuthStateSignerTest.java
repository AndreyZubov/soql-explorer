package io.soqlexplorer.salesforce.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayException;
import io.soqlexplorer.domain.connection.Environment;
import io.soqlexplorer.domain.user.UserId;
import io.soqlexplorer.salesforce.SalesforceProperties;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OAuthStateSignerTest {

  private static OAuthStateSigner signer(String key) {
    SalesforceProperties props = new SalesforceProperties();
    props.setRefreshTokenEncryptionKey(key);
    return new OAuthStateSigner(props, new ObjectMapper());
  }

  @Test
  void sign_and_verify_round_trip() {
    OAuthStateSigner s = signer("local-dev-key-with-enough-entropy-please-change-me");
    UserId user = UserId.of(UUID.randomUUID());
    OAuthState payload =
        OAuthState.newFor(user, Environment.SANDBOX, "verifier", Instant.now().plusSeconds(900));

    String state = s.sign(payload);
    OAuthState verified = s.verify(state);

    assertThat(verified.userId()).isEqualTo(user.value());
    assertThat(verified.env()).isEqualTo(Environment.SANDBOX);
    assertThat(verified.pkceVerifier()).isEqualTo("verifier");
  }

  @Test
  void tampered_state_is_rejected() {
    OAuthStateSigner s = signer("local-dev-key-with-enough-entropy-please-change-me");
    UserId user = UserId.of(UUID.randomUUID());
    String state =
        s.sign(OAuthState.newFor(user, Environment.PRODUCTION, "v", Instant.now().plusSeconds(900)));
    String tampered = state.substring(0, state.length() - 4) + "XXXX";

    assertThatThrownBy(() -> s.verify(tampered))
        .isInstanceOf(SalesforceGatewayException.class)
        .matches(ex -> ((SalesforceGatewayException) ex).kind() == SalesforceGatewayException.Kind.AUTH);
  }

  @Test
  void different_key_rejects_state_signed_elsewhere() {
    OAuthStateSigner a = signer("key-one-with-enough-entropy-please-change-me");
    OAuthStateSigner b = signer("key-two-with-enough-entropy-please-change-me");
    String state =
        a.sign(
            OAuthState.newFor(
                UserId.of(UUID.randomUUID()),
                Environment.SANDBOX,
                "v",
                Instant.now().plusSeconds(900)));

    assertThatThrownBy(() -> b.verify(state))
        .isInstanceOf(SalesforceGatewayException.class);
  }
}
