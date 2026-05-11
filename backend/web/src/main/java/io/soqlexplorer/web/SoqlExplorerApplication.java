package io.soqlexplorer.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the SOQL Explorer backend.
 *
 * <p>Component scanning is intentionally rooted at {@code io.soqlexplorer} so beans declared in
 * the persistence, salesforce, and cache modules are picked up.
 */
@SpringBootApplication(scanBasePackages = "io.soqlexplorer")
public class SoqlExplorerApplication {

  public static void main(String[] args) {
    SpringApplication.run(SoqlExplorerApplication.class, args);
  }
}
