package com.baemin_mini.dto.order;

import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.domain.enums.PaymentMethod;
import com.baemin_mini.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long restaurantId;
    private Long customerId;
    private Long shipperId;
    private Long voucherId;
    private OrderStatus status;
    private BigDecimal itemsTotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal finalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String deliveryAddress;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}