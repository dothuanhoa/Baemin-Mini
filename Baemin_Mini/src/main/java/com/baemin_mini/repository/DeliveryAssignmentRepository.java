package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.DeliveryAssignment;
import com.baemin_mini.domain.enums.DeliveryAssignmentStatus;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    boolean existsByOrder_IdAndShipper_IdAndStatusIn(
            Long orderId,
            Long shipperId,
            Collection<DeliveryAssignmentStatus> statuses);

    Optional<DeliveryAssignment> findFirstByOrder_IdAndShipper_IdAndStatusOrderByCreatedAtDesc(
            Long orderId,
            Long shipperId,
            DeliveryAssignmentStatus status);
}