# Экзаменационная работа — Шуманов (Билет №13)

Проект из двух микросервисов на Spring Boot (Java 17):

- **ingest-service** (порт 8080): принимает события пропусков по REST и отправляет их в RabbitMQ (очередь `events.raw`).
- **processor-service** (порт 8081): читает из `events.raw`, пишет в PostgreSQL, по запросу считает записи и пишет агрегат в ClickHouse.

## Запуск

### 1. Инфраструктура

```bash
cd processor-service
docker-compose up -d
```

Проверка: `docker ps` — должны быть контейнеры PostgreSQL (5432), ClickHouse (8123, 9000), RabbitMQ (5672, 15672).

### 2. Сервисы

**Ingest Service (терминал 1):**

```bash
cd ingest-service
mvn spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui/index.html

**Processor Service (терминал 2):**

```bash
cd processor-service
mvn spring-boot:run
```

Swagger: http://localhost:8081/swagger-ui/index.html

## Проверка

1. **Отправить событие:**  
   POST http://localhost:8080/api/v1/events (или через Swagger) с телом, например:

```json
{
  "идентификатор": "test-001",
  "фио_сотрудника": "Иванов И.И.",
  "номер_пропуска": "P-100",
  "результат_проверки": "пройдено",
  "дата_события": "2026-02-24T12:00:00"
}
```

Ожидается ответ: `Event sent to RabbitMQ`.

2. **Логи processor-service:** сообщения о сохранении в PostgreSQL.

3. **PostgreSQL:** таблица `сырые_события_пропусков` — должна появиться запись.

4. **Сохранить счётчик в ClickHouse:**  
   POST http://localhost:8081/api/v1/events/count — в ответе `count` и сообщение о сохранении в ClickHouse.

5. **ClickHouse:**  
   http://localhost:8123/play — запрос:

```sql
SELECT * FROM `агрегаты_событий_пропусков` ORDER BY `дата_и_время_записи` DESC;
```

Имена колонок с кириллицей — в обратных кавычках (\`).

## Структура по Билету №13

- **RabbitMQ:** очередь `events.raw`.
- **PostgreSQL:** таблица `сырые_события_пропусков` — идентификатор, фио_сотрудника, номер_пропуска, результат_проверки, дата_события.
- **ClickHouse:** таблица `агрегаты_событий_пропусков` — дата_и_время_записи, количество_записей.
