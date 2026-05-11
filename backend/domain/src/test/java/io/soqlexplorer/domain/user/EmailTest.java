package io.soqlexplorer.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  void normalizes_to_lowercase() {
    assertThat(Email.of("Foo@Example.COM").value()).isEqualTo("foo@example.com");
  }

  @Test
  void rejects_malformed_input() {
    assertThatThrownBy(() -> Email.of("not-an-email"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejects_oversized_input() {
    String local = "a".repeat(250);
    assertThatThrownBy(() -> Email.of(local + "@a.io"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
