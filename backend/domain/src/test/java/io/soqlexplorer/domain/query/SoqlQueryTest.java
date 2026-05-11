package io.soqlexplorer.domain.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SoqlQueryTest {

  @Test
  void accepts_select_query() {
    SoqlQuery q = SoqlQuery.of("SELECT Id, Name FROM Account LIMIT 10");
    assertThat(q.text()).startsWith("SELECT");
  }

  @Test
  void rejects_blank_text() {
    assertThatThrownBy(() -> SoqlQuery.of("   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejects_dml_statements() {
    assertThatThrownBy(() -> SoqlQuery.of("DELETE FROM Account WHERE Id = '1'"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SoqlQuery.of("UPDATE Account SET Name = 'x'"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejects_oversized_input() {
    String big = "SELECT Id FROM Account WHERE Name = '" + "x".repeat(20_001) + "'";
    assertThatThrownBy(() -> SoqlQuery.of(big)).isInstanceOf(IllegalArgumentException.class);
  }
}
