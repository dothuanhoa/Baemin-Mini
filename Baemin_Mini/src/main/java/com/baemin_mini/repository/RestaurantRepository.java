package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.Restaurant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByAreaIdAndIsOpenTrue(Long areaId);
    List<Restaurant> findByOwnerId(Long ownerId);
}
