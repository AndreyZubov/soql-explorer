package io.soqlexplorer.application.ports.query;

import io.soqlexplorer.domain.query.QueryTemplate;
import io.soqlexplorer.domain.query.TemplateId;
import io.soqlexplorer.domain.user.UserId;
import java.util.List;
import java.util.Optional;

/** Outbound port for {@link QueryTemplate} persistence. */
public interface QueryTemplateRepositoryPort {

  QueryTemplate save(QueryTemplate template);

  Optional<QueryTemplate> findLatestById(TemplateId id);

  List<QueryTemplate> findAllVersions(TemplateId id);

  List<QueryTemplate> findLatestByOwner(UserId ownerId);

  List<QueryTemplate> findLatestShared();

  void delete(TemplateId id);
}
