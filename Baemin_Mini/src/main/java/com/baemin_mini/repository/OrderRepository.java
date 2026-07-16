package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.Order;
import com.baemin_mini.domain.enums.OrderStatus;
import java.math.BigDecimal;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<Order> findByRestaurantIdAndStatusInOrderByCreatedAtDesc(
            Long restaurantId,
            Collection<OrderStatus> statuses);

    List<Order> findByStatusAndShipperIdIsNullOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findByStatusInAndShipperIdIsNullOrderByCreatedAtAsc(Collection<OrderStatus> statuses);

    List<Order> findByShipperIdAndStatusInOrderByCreatedAtDesc(
            Long shipperId,
            Collection<OrderStatus> statuses);

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shipperId IS NULL AND o.status IN :statuses")
    long countByShipperIdIsNullAndStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.platformFee), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumPlatformFeeByStatus(@Param("status") OrderStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);
}