package com.rrestate.portfolio;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PositionRepository extends JpaRepository<Position, UUID> {
    List<Position> findByUserId(UUID userId);
    List<Position> findByPropertyId(UUID propertyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Position p where p.userId = :userId and p.propertyId = :propertyId")
    Optional<Position> lockByUserAndProperty(UUID userId, UUID propertyId);
}
