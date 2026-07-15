package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.enums.OrderStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<Order> findByRestaurantIdAndStatusInOrderByCreatedAtDesc(
            Long restaurantId,
            Collection<OrderStatus> statuses);

    List<Order> findByStatusAndShipperIdIsNullOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findByShipperIdAndStatusInOrderByCreatedAtDesc(
            Long shipperId,
            Collection<OrderStatus> statuses);
}