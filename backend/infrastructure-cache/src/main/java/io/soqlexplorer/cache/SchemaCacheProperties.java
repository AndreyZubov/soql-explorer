package io.soqlexplorer.cache;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunable parameters for the schema cache.
 *
 * <p>Defaults match the implementation plan: sObject list TTL 1 hour, describe TTL 6 hours,
 * 500 describe entries per connection (we approximate by capping the total describe cache size
 * proportional to a typical small deployment).
 */
@ConfigurationProperties(prefix = "soqlexplorer.cache.schema")
public class SchemaCacheProperties {

  /** TTL applied to the sObject-list cache. */
  private Duration sobjectsTtl = Duration.ofHours(1);

  /** TTL applied to the describe cache. */
  private Duration describeTtl = Duration.ofHours(6);

  /** Maximum number of describe entries the cache will hold globally. */
  private long describeMaxSize = 5_000;

  /** Maximum number of sObject-list entries the cache will hold globally. */
  private long sobjectsMaxSize = 1_000;

  public Duration getSobjectsTtl() {
    return sobjectsTtl;
  }

  public void setSobjectsTtl(Duration sobjectsTtl) {
    this.sobjectsTtl = sobjectsTtl;
  }

  public Duration getDescribeTtl() {
    return describeTtl;
  }

  public void setDescribeTtl(Duration describeTtl) {
    this.describeTtl = describeTtl;
  }

  public long getDescribeMaxSize() {
    return describeMaxSize;
  }

  public void setDescribeMaxSize(long describeMaxSize) {
    this.describeMaxSize = describeMaxSize;
  }

  public long getSobjectsMaxSize() {
    return sobjectsMaxSize;
  }

  public void setSobjectsMaxSize(long sobjectsMaxSize) {
    this.sobjectsMaxSize = sobjectsMaxSize;
  }
}
