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

**В PowerShell** Варианты:

```powershell
# Вариант 1:
curl.exe -X POST http://localhost:8080/api/v1/events -H "Content-Type: application/json" -d '{"идентификатор":"test-001","фио_сотрудника":"Иванов И.И.","номер_пропуска":"P-100","результат_проверки":"пройдено","дата_события":"2026-02-24T12:00:00"}'

# Вариант 2:
$body = '{"идентификатор":"test-001","фио_сотрудника":"Иванов И.И.","номер_пропуска":"P-100","результат_проверки":"пройдено","дата_события":"2026-02-24T12:00:00"}'
Invoke-RestMethod -Uri http://localhost:8080/api/v1/events -Method Post -Body $body -ContentType "application/json; charset=utf-8"
```

2. **Логи processor-service:** сообщения о сохранении в PostgreSQL.

3. **PostgreSQL:** таблица `сырые_события_пропусков` — должна появиться запись.

4. **Сохранить счётчик в ClickHouse:**  
   POST http://localhost:8081/api/v1/events/count — в ответе `count` и сообщение о сохранении в ClickHouse.

5. **ClickHouse:**  
   http://localhost:8123/play — запрос:

```sql
SELECT * FROM `агрегаты_событий_пропусков` ORDER BY `дата_и_время_записи` DESC;
```

## Полный прогон (пошагово)

1. **Поднять инфраструктуру** (один раз):
   ```bash
   cd processor-service
   docker-compose up -d
   ```
   Дождаться запуска: `docker ps` — контейнеры `postgres`, `clickhouse`, `rabbitmq` в состоянии Up.

2. **Запустить ingest-service** (терминал 1):
   ```bash
   cd ingest-service
   .\mvnw.cmd spring-boot:run
   ```
   Дождаться строки `Tomcat started on port 8080`.

3. **Запустить processor-service** (терминал 2):
   ```bash
   cd processor-service
   .\mvnw.cmd spring-boot:run
   ```
   Дождаться строки `Tomcat started on port 8081` и подключения к RabbitMQ.

4. **Отправить тестовое событие** (терминал 3 или Swagger). В PowerShell используйте один из вариантов:

   ```powershell
   # Вариант 1: curl.exe, JSON в одинарных кавычках
   curl.exe -X POST http://localhost:8080/api/v1/events -H "Content-Type: application/json" -d '{"идентификатор":"test-001","фио_сотрудника":"Иванов И.И.","номер_пропуска":"P-100","результат_проверки":"пройдено","дата_события":"2026-02-24T12:00:00"}'

   # Вариант 2: Invoke-RestMethod
   $body = '{"идентификатор":"test-001","фио_сотрудника":"Иванов И.И.","номер_пропуска":"P-100","результат_проверки":"пройдено","дата_события":"2026-02-24T12:00:00"}'
   Invoke-RestMethod -Uri http://localhost:8080/api/v1/events -Method Post -Body $body -ContentType "application/json; charset=utf-8"
   ```
   Ожидается ответ: `Event sent to RabbitMQ`.

5. **Проверить логи processor-service:** в терминале 2 должны появиться строки:
   - `Сохранение в PostgreSQL...`
   - `✓ Событие успешно сохранено в PostgreSQL! ID: (id события)`

6. **Проверить PostgreSQL:**
   ```powershell
   docker exec -it postgres psql -U postgres -d postgres -c 'SELECT * FROM "сырые_события_пропусков";'
   ```
   В PowerShell для `-c` используйте **одинарные** кавычки, чтобы двойные кавычки вокруг имени таблицы дошли до psql. В bash/zsh:
   ```bash
   docker exec -it postgres psql -U postgres -d postgres -c "SELECT * FROM \"сырые_события_пропусков\";"
   ```
   Должна быть одна запись (или больше при повторных отправках).

7. **Сохранить счётчик в ClickHouse:**
   ```powershell
   Invoke-RestMethod -Uri http://localhost:8081/api/v1/events/count -Method Post
   ```
   Или: `curl.exe -X POST http://localhost:8081/api/v1/events/count`  
   В ответе — `count` и сообщение о сохранении в ClickHouse. В логах processor-service — `✓ Количество записей (...) успешно сохранено в ClickHouse`.

8. **Проверить ClickHouse:** в браузере http://localhost:8123/play выполнить:
   ```sql
   SELECT * FROM `агрегаты_событий_пропусков` ORDER BY `дата_и_время_записи` DESC;
   ```
   Если таблица не создана — один раз выполнить содержимое `processor-service/sql/clickhouse_init.sql` в Play **или** запустить `.\scripts\init-clickhouse.ps1` (создаёт таблицу по HTTP).

Готовый сценарий автоматической проверки (после запуска инфраструктуры и обоих сервисов): см. `scripts/full-run.ps1`.

## Структура по Билету №13

- **RabbitMQ:** очередь `events.raw`.
- **PostgreSQL:** таблица `сырые_события_пропусков` — идентификатор, фио_сотрудника, номер_пропуска, результат_проверки, дата_события.
- **ClickHouse:** таблица `агрегаты_событий_пропусков` — дата_и_время_записи, количество_записей.
