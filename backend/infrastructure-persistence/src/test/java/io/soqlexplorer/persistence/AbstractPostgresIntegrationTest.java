package io.soqlexplorer.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared base class for repository-adapter tests.
 *
 * <p>Spins up a single Postgres 16 container per test run (reused across subclasses thanks to
 * Testcontainers' container reuse). Each subclass inherits the Spring context configured with
 * the live JDBC URL.
 */
@Testcontainers
@SpringBootTest(classes = PersistenceTestApplication.class)
public abstract class AbstractPostgresIntegrationTest {

  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
          .withDatabaseName("soql_explorer_test")
          .withUsername("test")
          .withPassword("test")
          .withReuse(true);

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
    registry.add("spring.flyway.user", POSTGRES::getUsername);
    registry.add("spring.flyway.password", POSTGRES::getPassword);
  }
}
