package com.rrestate.distribution;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationRepository extends JpaRepository<Allocation, UUID> {
    List<Allocation> findByDistributionId(UUID distributionId);
    Page<Allocation> findByUserId(UUID userId, Pageable pageable);
    List<Allocation> findByUserIdAndStatusOrderByPaidAtDesc(UUID userId, String status);
}
