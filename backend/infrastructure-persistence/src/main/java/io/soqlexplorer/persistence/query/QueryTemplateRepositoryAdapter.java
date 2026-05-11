package io.soqlexplorer.persistence.query;

import io.soqlexplorer.application.ports.query.QueryTemplateRepositoryPort;
import io.soqlexplorer.domain.query.QueryTemplate;
import io.soqlexplorer.domain.query.SoqlQuery;
import io.soqlexplorer.domain.query.TemplateId;
import io.soqlexplorer.domain.user.UserId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

@Component
public class QueryTemplateRepositoryAdapter implements QueryTemplateRepositoryPort {

  private static final String TAG_SEPARATOR = ""; // ASCII unit separator — safe for free-text tags.

  private final QueryTemplateJpaRepository jpa;

  public QueryTemplateRepositoryAdapter(QueryTemplateJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public QueryTemplate save(QueryTemplate t) {
    QueryTemplateEntity entity =
        jpa.findById(new QueryTemplateEntity.Key(t.id().value(), t.version()))
            .map(
                e -> {
                  e.setName(t.name());
                  e.setShared(t.isShared());
                  e.setLatest(t.isLatest());
                  e.setTags(joinTags(t.tags()));
                  e.setUpdatedAt(t.updatedAt());
                  return e;
                })
            .orElseGet(
                () ->
                    new QueryTemplateEntity(
                        t.id().value(),
                        t.version(),
                        t.ownerId().value(),
                        t.name(),
                        t.soql().text(),
                        joinTags(t.tags()),
                        t.isShared(),
                        t.isLatest(),
                        t.createdAt(),
                        t.updatedAt()));
    return toDomain(jpa.save(entity));
  }

  @Override
  public Optional<QueryTemplate> findLatestById(TemplateId id) {
    return jpa.findByIdAndIsLatestTrue(id.value())
        .map(QueryTemplateRepositoryAdapter::toDomain);
  }

  @Override
  public List<QueryTemplate> findAllVersions(TemplateId id) {
    return jpa.findAllByIdOrderByVersionDesc(id.value()).stream()
        .map(QueryTemplateRepositoryAdapter::toDomain)
        .toList();
  }

  @Override
  public List<QueryTemplate> findLatestByOwner(UserId ownerId) {
    return jpa.findAllByOwnerIdAndIsLatestTrueOrderByUpdatedAtDesc(ownerId.value()).stream()
        .map(QueryTemplateRepositoryAdapter::toDomain)
        .toList();
  }

  @Override
  public List<QueryTemplate> findLatestShared() {
    return jpa.findAllByIsLatestTrueAndSharedTrueOrderByUpdatedAtDesc().stream()
        .map(QueryTemplateRepositoryAdapter::toDomain)
        .toList();
  }

  @Override
  public void delete(TemplateId id) {
    jpa.findAllByIdOrderByVersionDesc(id.value()).forEach(jpa::delete);
  }

  private static QueryTemplate toDomain(QueryTemplateEntity e) {
    return QueryTemplate.rehydrate(
        TemplateId.of(e.getId()),
        UserId.of(e.getOwnerId()),
        e.getName(),
        SoqlQuery.of(e.getSoql()),
        splitTags(e.getTags()),
        e.isShared(),
        e.getVersion(),
        e.isLatest(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }

  private static String joinTags(Set<String> tags) {
    return String.join(TAG_SEPARATOR, tags);
  }

  private static Set<String> splitTags(String joined) {
    if (joined == null || joined.isEmpty()) {
      return Set.of();
    }
    return new TreeSet<>(Arrays.asList(joined.split(TAG_SEPARATOR)));
  }
}
