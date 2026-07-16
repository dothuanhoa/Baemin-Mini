package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.dto.catalog.MenuItemAvailabilityRequest;
import com.baemin_mini.dto.catalog.MenuItemRequest;
import com.baemin_mini.dto.catalog.MenuItemResponse;
import com.baemin_mini.dto.catalog.RestaurantResponse;
import com.baemin_mini.service.CatalogService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/restaurants/nearby")
    public ApiResponse<List<RestaurantResponse>> getNearbyRestaurants(@RequestParam(required = false) Long areaId) {
        return ApiResponse.success("Restaurants fetched successfully", catalogService.getNearbyRestaurants(areaId));
    }

    @GetMapping("/restaurants/{id}")
    public ApiResponse<RestaurantResponse> getRestaurantDetails(@PathVariable Long id) {
        return ApiResponse.success("Restaurant fetched successfully", catalogService.getRestaurantById(id));
    }

    @GetMapping("/restaurants/{id}/menu")
    public ApiResponse<List<MenuItemResponse>> getRestaurantMenu(@PathVariable Long id) {
        return ApiResponse.success("Menu fetched successfully", catalogService.getMenuItemsByRestaurant(id));
    }


    @PostMapping("/restaurants/{restaurantId}/menu")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ApiResponse<MenuItemResponse> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            Principal principal) {
        return ApiResponse.success("Menu item created successfully", catalogService.createMenuItem(restaurantId, request, principal.getName()));
    }

    @PutMapping("/menu-items/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ApiResponse<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request,
            Principal principal) {
        return ApiResponse.success("Menu item updated successfully", catalogService.updateMenuItem(id, request, principal.getName()));
    }

    @PatchMapping("/menu-items/{id}/availability")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ApiResponse<Void> toggleMenuItemAvailability(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemAvailabilityRequest request,
            Principal principal) {
        catalogService.toggleMenuItemAvailability(id, request.isAvailable(), principal.getName());
        return ApiResponse.successMessage("Menu item availability updated successfully");
    }
}
