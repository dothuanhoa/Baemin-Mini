package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.ForbiddenException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.Area;
import com.baemin_mini.domain.entity.Category;
import com.baemin_mini.domain.entity.MenuItem;
import com.baemin_mini.domain.entity.Restaurant;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.enums.RoleName;
import com.baemin_mini.dto.catalog.AreaResponse;
import com.baemin_mini.dto.catalog.CategoryResponse;
import com.baemin_mini.dto.catalog.MenuItemRequest;
import com.baemin_mini.dto.catalog.MenuItemResponse;
import com.baemin_mini.dto.catalog.RestaurantResponse;
import com.baemin_mini.repository.AreaRepository;
import com.baemin_mini.repository.CategoryRepository;
import com.baemin_mini.repository.MenuItemRepository;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.CatalogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getNearbyRestaurants(Long areaId) {
        List<Restaurant> restaurants;
        if (areaId != null) {
            restaurants = restaurantRepository.findByAreaIdAndIsOpenTrue(areaId);
        } else {
            restaurants = restaurantRepository.findAll().stream().filter(Restaurant::getIsOpen).toList();
        }
        return restaurants.stream().map(this::toRestaurantResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        return toRestaurantResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant not found");
        }
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId).stream()
                .map(this::toMenuItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request, String username) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        
        checkOwnership(restaurant, username);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setCategory(category);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setImageUrl(request.imageUrl());
        if (request.isAvailable() != null) {
            item.setIsAvailable(request.isAvailable());
        }

        menuItemRepository.save(item);
        return toMenuItemResponse(item);
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(Long menuItemId, MenuItemRequest request, String username) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

        checkOwnership(item.getRestaurant(), username);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        item.setCategory(category);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setImageUrl(request.imageUrl());
        if (request.isAvailable() != null) {
            item.setIsAvailable(request.isAvailable());
        }

        menuItemRepository.save(item);
        return toMenuItemResponse(item);
    }

    @Override
    @Transactional
    public void toggleMenuItemAvailability(Long menuItemId, boolean isAvailable, String username) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

        checkOwnership(item.getRestaurant(), username);

        item.setIsAvailable(isAvailable);
        menuItemRepository.save(item);
    }

    private void checkOwnership(Restaurant restaurant, String username) {
        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (hasRole(actor, RoleName.ADMIN)) {
            return;
        }
        if (!restaurant.getOwner().getId().equals(actor.getId())) {
            throw new ForbiddenException("You do not have permission to manage this restaurant");
        }
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName() == roleName);
    }

    private RestaurantResponse toRestaurantResponse(Restaurant r) {
        AreaResponse area = null;
        if (r.getArea() != null) {
            area = new AreaResponse(r.getArea().getId(), r.getArea().getName());
        }
        return new RestaurantResponse(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getPhoneContact(),
                r.getLatitude(),
                r.getLongitude(),
                r.getIsOpen(),
                area
        );
    }

    private MenuItemResponse toMenuItemResponse(MenuItem m) {
        CategoryResponse cat = null;
        if (m.getCategory() != null) {
            cat = new CategoryResponse(m.getCategory().getId(), m.getCategory().getName(), m.getCategory().getDescription());
        }
        return new MenuItemResponse(
                m.getId(),
                m.getName(),
                m.getDescription(),
                m.getPrice(),
                m.getImageUrl(),
                m.getIsAvailable(),
                cat
        );
    }
}
