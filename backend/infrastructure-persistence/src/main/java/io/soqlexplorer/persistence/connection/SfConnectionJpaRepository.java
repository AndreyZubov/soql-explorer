package io.soqlexplorer.persistence.connection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for {@link SfConnectionEntity}. */
public interface SfConnectionJpaRepository extends JpaRepository<SfConnectionEntity, UUID> {

  List<SfConnectionEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

  Optional<SfConnectionEntity> findFirstByOwnerIdAndIsDefaultTrue(UUID ownerId);

  Optional<SfConnectionEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

  @Modifying
  @Query(
      "UPDATE SfConnectionEntity c SET c.isDefault = false, c.updatedAt = :now"
          + " WHERE c.ownerId = :ownerId AND c.isDefault = true AND c.id <> :id")
  void clearOtherDefaults(@Param("ownerId") UUID ownerId, @Param("id") UUID id, @Param("now") java.time.Instant now);
}
