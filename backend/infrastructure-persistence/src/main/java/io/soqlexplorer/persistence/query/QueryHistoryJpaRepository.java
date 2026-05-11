package io.soqlexplorer.persistence.query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueryHistoryJpaRepository extends JpaRepository<QueryHistoryEntity, UUID> {

  @Query(
      "select h from QueryHistoryEntity h "
          + "where h.userId = :userId and h.deletedAt is null "
          + "order by h.executedAt desc")
  List<QueryHistoryEntity> findRecentForUser(@Param("userId") UUID userId, Pageable pageable);

  @Modifying
  @Query(
      "update QueryHistoryEntity h set h.deletedAt = :now "
          + "where h.deletedAt is null and h.executedAt < :cutoff")
  int softDeleteOlderThan(@Param("cutoff") Instant cutoff, @Param("now") Instant now);

  @Modifying
  @Query(
      "update QueryHistoryEntity h set h.deletedAt = :now "
          + "where h.id = :id and h.userId = :userId and h.deletedAt is null")
  int softDeleteForUser(
      @Param("id") UUID id, @Param("userId") UUID userId, @Param("now") Instant now);
}
