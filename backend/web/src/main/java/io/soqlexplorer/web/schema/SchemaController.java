package io.soqlexplorer.web.schema;

import io.soqlexplorer.application.schema.DescribeSObjectUseCase;
import io.soqlexplorer.application.schema.GetRelationshipsUseCase;
import io.soqlexplorer.application.schema.ListSObjectsUseCase;
import io.soqlexplorer.domain.connection.ConnectionId;
import io.soqlexplorer.domain.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only Salesforce schema endpoints, keyed by connection.
 *
 * <p>Every endpoint goes through the {@link io.soqlexplorer.application.ports.schema.SchemaCachePort}
 * Caffeine adapter, so repeated calls within the configured TTLs do not consume Salesforce API
 * limits.
 */
@RestController
@RequestMapping("/schema/{connectionId}")
@Tag(name = "schema", description = "Salesforce schema browsing")
public class SchemaController {

  private final ListSObjectsUseCase listSObjects;
  private final DescribeSObjectUseCase describeSObject;
  private final GetRelationshipsUseCase getRelationships;

  public SchemaController(
      ListSObjectsUseCase listSObjects,
      DescribeSObjectUseCase describeSObject,
      GetRelationshipsUseCase getRelationships) {
    this.listSObjects = listSObjects;
    this.describeSObject = describeSObject;
    this.getRelationships = getRelationships;
  }

  @GetMapping("/sobjects")
  @Operation(summary = "List sObject API names available on the connection")
  public List<String> listSObjects(
      @AuthenticationPrincipal UserId principal, @PathVariable("connectionId") UUID connectionId) {
    return listSObjects.list(principal, ConnectionId.of(connectionId));
  }

  @GetMapping("/sobjects/{name}/describe")
  @Operation(summary = "Describe a single sObject")
  public Map<String, Object> describeSObject(
      @AuthenticationPrincipal UserId principal,
      @PathVariable("connectionId") UUID connectionId,
      @PathVariable("name") String name) {
    return describeSObject.describe(principal, ConnectionId.of(connectionId), name);
  }

  @GetMapping("/sobjects/{name}/relationships")
  @Operation(summary = "List parent + child relationships for an sObject")
  public Map<String, Object> relationships(
      @AuthenticationPrincipal UserId principal,
      @PathVariable("connectionId") UUID connectionId,
      @PathVariable("name") String name) {
    return getRelationships.get(principal, ConnectionId.of(connectionId), name);
  }
}
