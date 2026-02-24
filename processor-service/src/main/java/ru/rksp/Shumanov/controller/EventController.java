package ru.rksp.Shumanov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rksp.Shumanov.repository.RawEventRepository;
import ru.rksp.Shumanov.service.EventProcessorService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Events API", description = "API для работы с событиями пропусков")
public class EventController {

    private final RawEventRepository rawEventRepository;
    private final EventProcessorService eventProcessorService;

    public EventController(RawEventRepository rawEventRepository, EventProcessorService eventProcessorService) {
        this.rawEventRepository = rawEventRepository;
        this.eventProcessorService = eventProcessorService;
    }

    @PostMapping("/events/count")
    @Operation(summary = "Получить количество событий", description = "Получает количество записей из PostgreSQL и сохраняет в ClickHouse")
    public ResponseEntity<Map<String, Object>> getEventsCount() {
        long count = rawEventRepository.count();
        eventProcessorService.saveCountToClickHouse(count);

        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("message", "Count saved to ClickHouse");
        return ResponseEntity.ok(response);
    }
}
