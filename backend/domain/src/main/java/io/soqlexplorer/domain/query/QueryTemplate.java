package io.soqlexplorer.domain.query;

import io.soqlexplorer.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A reusable, named SOQL query owned by a user.
 *
 * <p>Templates support versioning: editing a template creates a new version and flips
 * {@code isLatest} on the previous one to {@code false}. v1 does not bind parameters (deferred to
 * v2 — see SPEC §1.2), so a template is just a name + SOQL + tags + sharing flag.
 */
public final class QueryTemplate {

  private final TemplateId id;
  private final UserId ownerId;
  private String name;
  private SoqlQuery soql;
  private Set<String> tags;
  private boolean shared;
  private int version;
  private boolean isLatest;
  private final Instant createdAt;
  private Instant updatedAt;

  private QueryTemplate(
      TemplateId id,
      UserId ownerId,
      String name,
      SoqlQuery soql,
      Set<String> tags,
      boolean shared,
      int version,
      boolean isLatest,
      Instant createdAt,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.ownerId = Objects.requireNonNull(ownerId, "ownerId");
    this.name = requireNonBlank(name, "name");
    this.soql = Objects.requireNonNull(soql, "soql");
    this.tags = normalizeTags(tags);
    this.shared = shared;
    if (version < 1) {
      throw new IllegalArgumentException("version must be >= 1");
    }
    this.version = version;
    this.isLatest = isLatest;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }

  public static QueryTemplate create(
      UserId ownerId,
      String name,
      SoqlQuery soql,
      List<String> tags,
      boolean shared,
      Instant now) {
    return new QueryTemplate(
        TemplateId.newId(),
        ownerId,
        name,
        soql,
        tags == null ? Set.of() : new TreeSet<>(tags),
        shared,
        1,
        true,
        now,
        now);
  }

  public static QueryTemplate rehydrate(
      TemplateId id,
      UserId ownerId,
      String name,
      SoqlQuery soql,
      Set<String> tags,
      boolean shared,
      int version,
      boolean isLatest,
      Instant createdAt,
      Instant updatedAt) {
    return new QueryTemplate(
        id, ownerId, name, soql, tags, shared, version, isLatest, createdAt, updatedAt);
  }

  /**
   * Marks this version as superseded. Callers should subsequently create a new {@code
   * QueryTemplate} record with {@code version = previous + 1} and {@code isLatest = true}.
   */
  public void supersede(Instant now) {
    this.isLatest = false;
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void rename(String newName, Instant now) {
    this.name = requireNonBlank(newName, "name");
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void retag(List<String> newTags, Instant now) {
    this.tags = normalizeTags(newTags == null ? Set.of() : new TreeSet<>(newTags));
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void share(Instant now) {
    this.shared = true;
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public void unshare(Instant now) {
    this.shared = false;
    this.updatedAt = Objects.requireNonNull(now, "now");
  }

  public TemplateId id() {
    return id;
  }

  public UserId ownerId() {
    return ownerId;
  }

  public String name() {
    return name;
  }

  public SoqlQuery soql() {
    return soql;
  }

  public Set<String> tags() {
    return Set.copyOf(tags);
  }

  public boolean isShared() {
    return shared;
  }

  public int version() {
    return version;
  }

  public boolean isLatest() {
    return isLatest;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  private static String requireNonBlank(String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }

  private static Set<String> normalizeTags(Set<String> tags) {
    TreeSet<String> sorted = new TreeSet<>();
    for (String tag : tags) {
      if (tag != null && !tag.isBlank()) {
        sorted.add(tag.trim().toLowerCase());
      }
    }
    return sorted;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof QueryTemplate other)) return false;
    return id.equals(other.id) && version == other.version;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version);
  }
}
