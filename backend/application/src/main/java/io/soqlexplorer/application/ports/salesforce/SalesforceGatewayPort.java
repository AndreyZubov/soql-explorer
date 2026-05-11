package io.soqlexplorer.application.ports.salesforce;

import io.soqlexplorer.domain.connection.SalesforceConnection;
import io.soqlexplorer.domain.query.SoqlQuery;
import java.util.List;
import java.util.Map;

/**
 * Outbound port for all Salesforce REST interactions.
 *
 * <p>The Step 1 boot does not call Salesforce; this interface is declared up front so the
 * application layer can compile against it and the Step 2 adapter can fill in the implementation
 * without touching higher layers.
 *
 * <p>Methods return plain Java types — no Spring, no Salesforce SDK leaking up. Errors are
 * surfaced via {@link SalesforceGatewayException}.
 */
public interface SalesforceGatewayPort {

  /** Lists sObject API names for the supplied connection. */
  List<String> listSObjects(SalesforceConnection connection);

  /** Describes a single sObject — fields, relationships, picklist values. */
  Map<String, Object> describeSObject(SalesforceConnection connection, String sObjectName);

  /** Executes a SOQL query and returns the raw page (records + nextRecordsUrl). */
  QueryPage executeQuery(SalesforceConnection connection, SoqlQuery query);

  /** Fetches the next page using the opaque cursor returned by Salesforce. */
  QueryPage fetchNextPage(SalesforceConnection connection, String nextRecordsUrl);
}
