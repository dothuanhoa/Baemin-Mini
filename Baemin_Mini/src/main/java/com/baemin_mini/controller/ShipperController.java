package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.domain.enums.ShipperStatus;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.shipper.NearbyOrderResponse;
import com.baemin_mini.dto.shipper.ShipperCancelOrderRequest;
import com.baemin_mini.dto.shipper.ShipperLocationRequest;
import com.baemin_mini.dto.shipper.ShipperProfileResponse;
import com.baemin_mini.service.DeliveryAssignmentService;
import com.baemin_mini.service.ShipperProfileService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipper")
@RequiredArgsConstructor
public class ShipperController {
    private final ShipperProfileService shipperProfileService;
    private final DeliveryAssignmentService deliveryAssignmentService;

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping("/profile")
    public ApiResponse<ShipperProfileResponse> getProfile(Principal principal) {
        return ApiResponse.success("Shipper profile fetched", shipperProfileService.getProfile(principal.getName()));
    }


    @PreAuthorize("hasRole('SHIPPER')")
    @PutMapping("/location")
    public ApiResponse<ShipperProfileResponse> updateLocation(
            Principal principal,
            @Valid @RequestBody ShipperLocationRequest request) {
        return ApiResponse.success("Shipper location updated", shipperProfileService.updateLocation(principal.getName(), request));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @PutMapping("/status")
    public ApiResponse<ShipperProfileResponse> updateStatus(
            Principal principal,
            @RequestParam ShipperStatus status) {
        return ApiResponse.success("Shipper status updated", shipperProfileService.updateStatus(principal.getName(), status));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping("/orders/nearby")
    public ApiResponse<List<NearbyOrderResponse>> getNearbyOrders(Principal principal) {
        return ApiResponse.success("Nearby orders fetched", deliveryAssignmentService.getNearbyOrders(principal.getName()));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @PostMapping("/orders/{id}/accept")
    public ApiResponse<OrderResponse> acceptOrder(
            Principal principal,
            @PathVariable Long id) {
        return ApiResponse.success("Order accepted", deliveryAssignmentService.acceptOrder(principal.getName(), id));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody ShipperCancelOrderRequest request) {
        return ApiResponse.success("Order cancelled by shipper", deliveryAssignmentService.cancelOrder(principal.getName(), id, request));
    }
    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping("/orders/my-orders")
    public ApiResponse<List<OrderResponse>> getMyActiveOrders(Principal principal) {
        return ApiResponse.success("Shipper orders fetched", shipperProfileService.getMyActiveOrders(principal.getName()));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping("/orders/history")
    public ApiResponse<List<OrderResponse>> getMyOrderHistory(Principal principal) {
        return ApiResponse.success("Shipper order history fetched", shipperProfileService.getMyOrderHistory(principal.getName()));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @PutMapping("/orders/{id}/start-delivery")
    public ApiResponse<OrderResponse> startDelivery(
            Principal principal,
            @PathVariable Long id) {
        return ApiResponse.success("Delivery started", shipperProfileService.startDelivery(principal.getName(), id));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @PutMapping("/orders/{id}/complete")
    public ApiResponse<OrderResponse> completeDelivery(
            Principal principal,
            @PathVariable Long id) {
        return ApiResponse.success("Delivery completed", shipperProfileService.completeDelivery(principal.getName(), id));
    }
}
