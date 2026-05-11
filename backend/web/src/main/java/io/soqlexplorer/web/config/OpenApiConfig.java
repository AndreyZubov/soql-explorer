package io.soqlexplorer.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customizes the springdoc-generated OpenAPI document.
 *
 * <p>Declaring the {@code bearerAuth} security scheme here means every controller gets a Swagger
 * UI "Authorize" button without having to annotate each operation. Step 5 wires this document
 * into a frontend code-gen step (Orval) so the SPA's API client is generated, not hand-written.
 */
@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI customOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("SOQL Explorer API")
                .description("Backend API for the SOQL Query Builder & Data Explorer")
                .version("v1")
                .license(new License().name("Apache-2.0")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
