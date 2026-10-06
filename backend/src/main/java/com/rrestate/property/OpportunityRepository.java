package com.rrestate.property;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {
    Optional<Opportunity> findByPropertyId(UUID propertyId);
    List<Opportunity> findByPropertyIdIn(Collection<UUID> propertyIds);

    /** Atomic capacity reservation: succeeds only while the target would not be exceeded. Returns rows changed (0 or 1). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Opportunity o set o.raisedAmount = o.raisedAmount + :amount where o.id = :id and o.raisedAmount + :amount <= o.targetRaise")
    int reserve(UUID id, BigDecimal amount);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Opportunity o set o.raisedAmount = o.raisedAmount - :amount where o.id = :id and o.raisedAmount - :amount >= 0")
    int release(UUID id, BigDecimal amount);
}
