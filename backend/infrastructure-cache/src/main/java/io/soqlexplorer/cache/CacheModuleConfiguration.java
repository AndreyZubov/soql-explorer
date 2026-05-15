package io.soqlexplorer.cache;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the cache module so the {@code web} module only needs to pull this jar onto the
 * classpath.
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(SchemaCacheProperties.class)
public class CacheModuleConfiguration {}
