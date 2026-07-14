package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.OrderTracking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {
    List<OrderTracking> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
