package io.soqlexplorer.application.ports.salesforce;

import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.query.SoqlQuery;
import java.util.List;
import java.util.Map;

/**
 * Outbound port for all Salesforce REST interactions.
 *
 * <p>Methods return plain Java types — no Spring, no Salesforce SDK leaking up. Errors are
 * surfaced via {@link SalesforceGatewayException}.
 *
 * <p>The adapter is responsible for keeping access tokens fresh (typically by delegating to an
 * in-memory token cache + the OAuth refresh-token flow). Callers pass the
 * {@link SalesforceConnection} aggregate and never see raw tokens.
 */
public interface SalesforceGatewayPort {

  /** Lists sObject API names for the supplied connection. */
  List<String> listSObjects(SalesforceConnection connection);

  /** Describes a single sObject — fields, relationships, picklist values. */
  Map<String, Object> describeSObject(SalesforceConnection connection, String sObjectName);

  /** Returns the parent/child relationships of an sObject. */
  Map<String, Object> getRelationships(SalesforceConnection connection, String sObjectName);

  /** Executes a SOQL query and returns the raw page (records + nextRecordsUrl). */
  QueryPage executeQuery(SalesforceConnection connection, SoqlQuery query);

  /** Fetches the next page using the opaque cursor returned by Salesforce. */
  QueryPage fetchNextPage(SalesforceConnection connection, String nextRecordsUrl);
}
