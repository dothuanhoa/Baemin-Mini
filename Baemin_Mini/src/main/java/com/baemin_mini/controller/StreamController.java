package com.baemin_mini.controller;

import com.baemin_mini.service.SseService;
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

    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    @GetMapping(value = "/restaurants/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeRestaurant(@PathVariable Long id) {
        return sseService.subscribeRestaurant(id);
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping(value = "/shippers/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeShipper(@PathVariable Long id) {
        return sseService.subscribeShipper(id);
    }
}
