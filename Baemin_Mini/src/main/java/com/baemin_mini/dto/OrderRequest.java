package com.baemin_mini.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Address ID is required")
    private Long addressId;

    private String voucherCode;

    private String restaurantNote;

    private String shipperNote;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;
}
