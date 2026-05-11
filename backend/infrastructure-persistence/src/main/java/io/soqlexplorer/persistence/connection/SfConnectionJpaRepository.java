package io.soqlexplorer.persistence.connection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for {@link SfConnectionEntity}. */
public interface SfConnectionJpaRepository extends JpaRepository<SfConnectionEntity, UUID> {

  List<SfConnectionEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

  Optional<SfConnectionEntity> findFirstByOwnerIdAndIsDefaultTrue(UUID ownerId);
}
