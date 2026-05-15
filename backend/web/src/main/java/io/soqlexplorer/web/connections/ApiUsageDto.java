package io.soqlexplorer.web.connections;

import io.soqlexplorer.application.ports.salesforce.ApiUsageInfo;

/** Outbound projection of the most recent {@link ApiUsageInfo} for a connection. */
public record ApiUsageDto(int used, int limit, int percentUsed) {

  public static ApiUsageDto from(ApiUsageInfo info) {
    return new ApiUsageDto(info.used(), info.limit(), info.percentUsed());
  }
}
