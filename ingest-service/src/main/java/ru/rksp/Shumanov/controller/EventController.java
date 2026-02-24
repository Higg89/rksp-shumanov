package ru.rksp.Shumanov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rksp.Shumanov.dto.EventDto;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Events API", description = "API для работы с событиями пропусков")
public class EventController {

    private final RabbitTemplate rabbitTemplate;

    public EventController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/events")
    @Operation(summary = "Создать событие", description = "Отправляет событие пропуска в RabbitMQ очередь events.raw")
    public ResponseEntity<String> createEvent(@RequestBody EventDto event) {
        try {
            if (event.getНомерПропуска() == null || event.getНомерПропуска().isEmpty()) {
                return ResponseEntity.badRequest().body("Поле 'номер_пропуска' обязательно");
            }
            rabbitTemplate.convertAndSend("", "events.raw", event);
            return ResponseEntity.ok("Event sent to RabbitMQ");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending event to RabbitMQ: " + e.getMessage());
        }
    }
}
