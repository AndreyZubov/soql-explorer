package io.soqlexplorer.web.config;

import io.soqlexplorer.application.auth.AuthenticateUserService;
import io.soqlexplorer.application.auth.AuthenticateUserUseCase;
import io.soqlexplorer.application.connection.DeleteConnectionService;
import io.soqlexplorer.application.connection.DeleteConnectionUseCase;
import io.soqlexplorer.application.connection.HandleOAuthCallbackService;
import io.soqlexplorer.application.connection.HandleOAuthCallbackUseCase;
import io.soqlexplorer.application.connection.ListConnectionsService;
import io.soqlexplorer.application.connection.ListConnectionsUseCase;
import io.soqlexplorer.application.connection.SetDefaultConnectionService;
import io.soqlexplorer.application.connection.SetDefaultConnectionUseCase;
import io.soqlexplorer.application.connection.StartOAuthFlowService;
import io.soqlexplorer.application.connection.StartOAuthFlowUseCase;
import io.soqlexplorer.application.ports.clock.ClockPort;
import io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort;
import io.soqlexplorer.application.ports.salesforce.SalesforceGatewayPort;
import io.soqlexplorer.application.ports.salesforce.SalesforceOAuthPort;
import io.soqlexplorer.application.ports.schema.SchemaCachePort;
import io.soqlexplorer.application.ports.security.PasswordHasherPort;
import io.soqlexplorer.application.ports.security.TokenCipherPort;
import io.soqlexplorer.application.ports.user.UserRepositoryPort;
import io.soqlexplorer.application.schema.DescribeSObjectService;
import io.soqlexplorer.application.schema.DescribeSObjectUseCase;
import io.soqlexplorer.application.schema.GetRelationshipsService;
import io.soqlexplorer.application.schema.GetRelationshipsUseCase;
import io.soqlexplorer.application.schema.InvalidateSchemaCacheService;
import io.soqlexplorer.application.schema.InvalidateSchemaCacheUseCase;
import io.soqlexplorer.application.schema.ListSObjectsService;
import io.soqlexplorer.application.schema.ListSObjectsUseCase;
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

  // -------------------------------------------------------------------------
  // Auth
  // -------------------------------------------------------------------------

  @Bean
  AuthenticateUserUseCase authenticateUserUseCase(
      UserRepositoryPort users, PasswordHasherPort hasher) {
    return new AuthenticateUserService(users, hasher);
  }

  // -------------------------------------------------------------------------
  // Connections
  // -------------------------------------------------------------------------

  @Bean
  StartOAuthFlowUseCase startOAuthFlowUseCase(SalesforceOAuthPort oauth) {
    return new StartOAuthFlowService(oauth);
  }

  @Bean
  HandleOAuthCallbackUseCase handleOAuthCallbackUseCase(
      SalesforceOAuthPort oauth,
      ConnectionRepositoryPort connections,
      TokenCipherPort cipher,
      ClockPort clock) {
    return new HandleOAuthCallbackService(oauth, connections, cipher, clock);
  }

  @Bean
  ListConnectionsUseCase listConnectionsUseCase(ConnectionRepositoryPort connections) {
    return new ListConnectionsService(connections);
  }

  @Bean
  SetDefaultConnectionUseCase setDefaultConnectionUseCase(
      ConnectionRepositoryPort connections, ClockPort clock) {
    return new SetDefaultConnectionService(connections, clock);
  }

  @Bean
  DeleteConnectionUseCase deleteConnectionUseCase(
      ConnectionRepositoryPort connections, SchemaCachePort cache) {
    return new DeleteConnectionService(connections, cache);
  }

  // -------------------------------------------------------------------------
  // Schema
  // -------------------------------------------------------------------------

  @Bean
  ListSObjectsUseCase listSObjectsUseCase(
      ConnectionRepositoryPort connections,
      SalesforceGatewayPort gateway,
      SchemaCachePort cache) {
    return new ListSObjectsService(connections, gateway, cache);
  }

  @Bean
  DescribeSObjectUseCase describeSObjectUseCase(
      ConnectionRepositoryPort connections,
      SalesforceGatewayPort gateway,
      SchemaCachePort cache) {
    return new DescribeSObjectService(connections, gateway, cache);
  }

  @Bean
  GetRelationshipsUseCase getRelationshipsUseCase(
      ConnectionRepositoryPort connections, SalesforceGatewayPort gateway) {
    return new GetRelationshipsService(connections, gateway);
  }

  @Bean
  InvalidateSchemaCacheUseCase invalidateSchemaCacheUseCase(SchemaCachePort cache) {
    return new InvalidateSchemaCacheService(cache);
  }
}
