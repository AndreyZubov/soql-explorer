package io.soqlexplorer.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Wires the persistence module so the {@code web} module only needs to pull this jar onto the
 * classpath. {@link EntityScan} and {@link EnableJpaRepositories} are scoped to this package so
 * we don't accidentally double-scan when the Spring Boot application root sits elsewhere.
 */
@Configuration
@EntityScan(basePackages = "io.soqlexplorer.persistence")
@EnableJpaRepositories(basePackages = "io.soqlexplorer.persistence")
@EnableTransactionManagement
public class PersistenceModuleConfiguration {}
