package com.baemin_mini.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private String voucherCode;

    private String restaurantNote;

    private String shipperNote;

    @NotEmpty(message = "Order items cannot be empty")
    private List<@Valid OrderItemRequest> items;
}
