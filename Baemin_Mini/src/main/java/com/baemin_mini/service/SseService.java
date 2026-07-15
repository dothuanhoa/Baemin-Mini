package com.baemin_mini.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter subscribeRestaurant(Long restaurantId);
    SseEmitter subscribeShipper(Long shipperId);
    void notifyRestaurant(Long restaurantId, String eventName, Object data);
    void notifyShipper(Long shipperId, String eventName, Object data);
}
