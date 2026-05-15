package io.soqlexplorer.salesforce;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Wires the Salesforce module so the {@code web} module only needs to depend on this jar. */
@Configuration
@ComponentScan
@EnableConfigurationProperties(SalesforceProperties.class)
public class SalesforceModuleConfiguration {}
