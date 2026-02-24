# Создаёт таблицу агрегаты_событий_пропусков в ClickHouse (один раз после docker-compose up).
# Использование: .\scripts\init-clickhouse.ps1   или из корня репозитория после запуска clickhouse.

$clickhouseUrl = "http://localhost:8123"
$sql = @"
CREATE TABLE IF NOT EXISTS ``агрегаты_событий_пропусков`` (
    ``дата_и_время_записи`` DateTime,
    ``количество_записей`` UInt64
) ENGINE = MergeTree()
ORDER BY ``дата_и_время_записи``
"@

try {
    $resp = Invoke-WebRequest -Uri $clickhouseUrl -Method Post -Body $sql -ContentType "text/plain; charset=utf-8" -UseBasicParsing -TimeoutSec 5
    if ($resp.StatusCode -eq 200) {
        Write-Host "Таблица агрегаты_событий_пропусков создана (или уже существует)." -ForegroundColor Green
    } else {
        Write-Host "Ответ ClickHouse: $($resp.StatusCode) $($resp.Content)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Ошибка: $_" -ForegroundColor Red
    Write-Host "Убедитесь, что ClickHouse запущен: docker ps | findstr clickhouse" -ForegroundColor Gray
    throw
}
