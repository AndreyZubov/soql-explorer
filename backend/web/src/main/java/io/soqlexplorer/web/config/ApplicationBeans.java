package io.soqlexplorer.web.config;

import io.soqlexplorer.application.auth.AuthenticateUserService;
import io.soqlexplorer.application.auth.AuthenticateUserUseCase;
import io.soqlexplorer.application.ports.security.PasswordHasherPort;
import io.soqlexplorer.application.ports.user.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires application-layer services (which are pure POJOs) into the Spring context.
 *
 * <p>Keeping all application-layer beans here means new use cases are added in one obvious
 * place and the application module itself stays free of {@code @Component} annotations.
 */
@Configuration
public class ApplicationBeans {

  @Bean
  AuthenticateUserUseCase authenticateUserUseCase(
      UserRepositoryPort users, PasswordHasherPort hasher) {
    return new AuthenticateUserService(users, hasher);
  }
}
