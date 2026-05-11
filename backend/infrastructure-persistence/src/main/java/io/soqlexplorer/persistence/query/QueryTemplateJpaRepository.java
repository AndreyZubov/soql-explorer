package io.soqlexplorer.persistence.query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueryTemplateJpaRepository
    extends JpaRepository<QueryTemplateEntity, QueryTemplateEntity.Key> {

  Optional<QueryTemplateEntity> findByIdAndIsLatestTrue(UUID id);

  List<QueryTemplateEntity> findAllByIdOrderByVersionDesc(UUID id);

  List<QueryTemplateEntity> findAllByOwnerIdAndIsLatestTrueOrderByUpdatedAtDesc(UUID ownerId);

  List<QueryTemplateEntity> findAllByIsLatestTrueAndSharedTrueOrderByUpdatedAtDesc();
}
