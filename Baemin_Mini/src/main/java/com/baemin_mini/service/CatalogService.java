package com.baemin_mini.service;

import com.baemin_mini.dto.catalog.MenuItemRequest;
import com.baemin_mini.dto.catalog.MenuItemResponse;
import com.baemin_mini.dto.catalog.RestaurantResponse;
import java.util.List;

public interface CatalogService {
    List<RestaurantResponse> getNearbyRestaurants(Long areaId);
    RestaurantResponse getRestaurantById(Long id);
    List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId);

    // Restaurant Owner APIs
    MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request, String username);
    MenuItemResponse updateMenuItem(Long menuItemId, MenuItemRequest request, String username);
    void toggleMenuItemAvailability(Long menuItemId, boolean isAvailable, String username);
}
