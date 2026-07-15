package com.baemin_mini.service.impl;

import com.baemin_mini.service.SseService;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseServiceImpl implements SseService {

    private static final Long DEFAULT_TIMEOUT = 3600000L; // 1 hour

    // store emitters by ID
    private final Map<Long, SseEmitter> restaurantEmitters = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> shipperEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribeRestaurant(Long restaurantId) {
        return createEmitter(restaurantId, restaurantEmitters, "Restaurant");
    }

    @Override
    public SseEmitter subscribeShipper(Long shipperId) {
        return createEmitter(shipperId, shipperEmitters, "Shipper");
    }

    @Override
    public void notifyRestaurant(Long restaurantId, String eventName, Object data) {
        SseEmitter emitter = restaurantEmitters.get(restaurantId);
        if (emitter != null) {
            sendEvent(emitter, restaurantId, restaurantEmitters, eventName, data, "Restaurant");
        }
    }

    @Override
    public void notifyShipper(Long shipperId, String eventName, Object data) {
        SseEmitter emitter = shipperEmitters.get(shipperId);
        if (emitter != null) {
            sendEvent(emitter, shipperId, shipperEmitters, eventName, data, "Shipper");
        }
    }

    private SseEmitter createEmitter(Long id, Map<Long, SseEmitter> emittersMap, String type) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitter.onCompletion(() -> {
            log.info("{} emitter completed for ID: {}", type, id);
            emittersMap.remove(id);
        });

        emitter.onTimeout(() -> {
            log.info("{} emitter timeout for ID: {}", type, id);
            emittersMap.remove(id);
            emitter.complete();
        });

        emitter.onError(e -> {
            log.error("{} emitter error for ID: {}: {}", type, id, e.getMessage());
            emittersMap.remove(id);
        });

        emittersMap.put(id, emitter);

        // Send a dummy event to establish connection successfully
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected successfully"));
        } catch (IOException e) {
            log.error("Error sending init event for {} ID {}: {}", type, id, e.getMessage());
            emittersMap.remove(id);
        }

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, Long id, Map<Long, SseEmitter> emittersMap, String eventName, Object data, String type) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.error("Error sending event {} to {} ID {}: {}", eventName, type, id, e.getMessage());
            emittersMap.remove(id);
            emitter.completeWithError(e);
        }
    }
}
