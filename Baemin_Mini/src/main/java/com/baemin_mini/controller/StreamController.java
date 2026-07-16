package com.baemin_mini.controller;

import com.baemin_mini.common.exception.ForbiddenException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.Restaurant;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.enums.RoleName;
import com.baemin_mini.repository.RestaurantRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.SseService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamController {

    private final SseService sseService;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    @GetMapping(value = "/restaurants/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeRestaurant(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isAdmin = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == RoleName.ADMIN);

        if (!isAdmin) {
            Restaurant restaurant = restaurantRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Restaurant not found"));
            if (!restaurant.getOwner().getId().equals(user.getId())) {
                throw new ForbiddenException("You don't have permission to subscribe to this restaurant's stream");
            }
        }

        return sseService.subscribeRestaurant(id);
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping(value = "/shippers/me", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeShipper(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return sseService.subscribeShipper(user.getId());
    }
}
