package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
    List<MenuItem> findByRestaurantId(Long restaurantId);
}
