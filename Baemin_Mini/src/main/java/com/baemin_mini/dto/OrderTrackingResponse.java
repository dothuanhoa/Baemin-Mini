package com.baemin_mini.dto;

import com.baemin_mini.domain.enums.OrderStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderTrackingResponse {
    private OrderStatus status;
    private String note;
    private String actorRole;
    private LocalDateTime createdAt;
}
