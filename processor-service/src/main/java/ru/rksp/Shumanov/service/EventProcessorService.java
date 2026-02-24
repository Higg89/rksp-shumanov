package ru.rksp.Shumanov.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.rksp.Shumanov.dto.EventDto;
import ru.rksp.Shumanov.entity.RawEvent;
import ru.rksp.Shumanov.repository.RawEventRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EventProcessorService {

    private final RawEventRepository rawEventRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter CLICKHOUSE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${clickhouse.datasource.url:http://localhost:8123}")
    private String clickHouseBaseUrl;

    public EventProcessorService(RawEventRepository rawEventRepository) {
        this.rawEventRepository = rawEventRepository;
    }

    @RabbitListener(queues = "events.raw")
    @Transactional
    public void processEvent(EventDto eventDto) {
        try {
            System.out.println("=== Начало обработки события ===");
            System.out.println("Получено событие из RabbitMQ:");
            System.out.println(" - ФИО сотрудника: " + eventDto.getФиоСотрудника());
            System.out.println(" - Номер пропуска: " + eventDto.getНомерПропуска());
            System.out.println(" - Результат проверки: " + eventDto.getРезультатПроверки());
            System.out.println(" - Дата: " + eventDto.getДатаСобытия());

            RawEvent rawEvent = new RawEvent();
            rawEvent.setФиоСотрудника(eventDto.getФиоСотрудника());
            rawEvent.setНомерПропуска(eventDto.getНомерПропуска());
            rawEvent.setРезультатПроверки(eventDto.getРезультатПроверки());
            rawEvent.setДатаСобытия(eventDto.getДатаСобытия());

            System.out.println("Сохранение в PostgreSQL...");
            RawEvent saved = rawEventRepository.save(rawEvent);
            rawEventRepository.flush();

            System.out.println("✓ Событие успешно сохранено в PostgreSQL! ID: " + saved.getИдентификатор());
            System.out.println("=== Конец обработки события ===");
        } catch (Exception e) {
            System.err.println("✗ ОШИБКА при обработке события: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void saveCountToClickHouse(long count) {
        String sql = null;
        try {
            String now = LocalDateTime.now().format(CLICKHOUSE_FORMATTER);
            sql = String.format(
                    "INSERT INTO `агрегаты_событий_пропусков` (`дата_и_время_записи`, `количество_записей`) VALUES ('%s', %d)",
                    now, count);

            String baseUrl = clickHouseBaseUrl.replaceAll("/$", "");
            String encodedQuery = URLEncoder.encode(sql, StandardCharsets.UTF_8);
            String fullUrl = baseUrl + "/?query=" + encodedQuery;

            System.out.println("=== Сохранение в ClickHouse ===");
            System.out.println("Количество записей: " + count);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "text/plain; charset=utf-8");
            HttpEntity<String> request = new HttpEntity<>(sql, headers);

            var response = restTemplate.postForEntity(baseUrl + "/", request, String.class);
            String responseBody = response.getBody();

            if (response.getStatusCode().is2xxSuccessful()) {
                if (responseBody == null || responseBody.isEmpty() || responseBody.trim().equals("")) {
                    System.out.println("✓ Количество записей (" + count + ") успешно сохранено в ClickHouse");
                } else if (responseBody.contains("Exception") || responseBody.contains("Error") || responseBody.contains("Code:")) {
                    System.err.println("✗ ClickHouse вернул ошибку: " + responseBody);
                } else {
                    System.out.println("✓ Количество записей (" + count + ") успешно сохранено в ClickHouse");
                }
            } else {
                System.err.println("✗ Неожиданный статус от ClickHouse: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("✗ ОШИБКА при сохранении в ClickHouse: " + e.getMessage());
            if (sql != null) {
                System.err.println("SQL: " + sql);
            }
            e.printStackTrace();
        }
    }
}
