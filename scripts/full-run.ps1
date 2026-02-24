# Полный прогон: отправка события -> PostgreSQL -> счётчик в ClickHouse
# Требуется: инфраструктура (docker-compose up -d), ingest-service и processor-service запущены.

$ErrorActionPreference = "Stop"
$baseIngest = "http://localhost:8080"
$baseProcessor = "http://localhost:8081"

function Test-Endpoint {
    param($url, $name)
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
        return $true
    } catch {
        Write-Host "  [FAIL] $name не доступен: $url" -ForegroundColor Red
        return $false
    }
}

Write-Host "`n=== Проверка доступности сервисов ===" -ForegroundColor Cyan
if (-not (Test-Endpoint "$baseIngest/swagger-ui/index.html" "ingest-service")) { exit 1 }
if (-not (Test-Endpoint "$baseProcessor/swagger-ui/index.html" "processor-service")) { exit 1 }
Write-Host "  [OK] Оба сервиса доступны`n" -ForegroundColor Green

Write-Host "=== Инициализация таблицы ClickHouse (если ещё не создана) ===" -ForegroundColor Cyan
try {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    & "$scriptDir\init-clickhouse.ps1"
} catch {
    Write-Host "  (пропущено или ClickHouse недоступен)" -ForegroundColor Gray
}

$body = @{
    идентификатор = "test-001"
    фио_сотрудника = "Иванов И.И."
    номер_пропуска = "P-100"
    результат_проверки = "пройдено"
    дата_события = "2026-02-24T12:00:00"
} | ConvertTo-Json

Write-Host "=== 1. Отправка события в ingest-service ===" -ForegroundColor Cyan
try {
    $resp = Invoke-RestMethod -Uri "$baseIngest/api/v1/events" -Method Post -Body $body -ContentType "application/json; charset=utf-8"
    Write-Host "  Ответ: $resp" -ForegroundColor Green
} catch {
    Write-Host "  Ошибка: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n  Ждём обработки processor-service (3 сек)..." -ForegroundColor Gray
Start-Sleep -Seconds 3

Write-Host "`n=== 2. Запрос счётчика и сохранение в ClickHouse ===" -ForegroundColor Cyan
try {
    $countResp = Invoke-RestMethod -Uri "$baseProcessor/api/v1/events/count" -Method Post
    Write-Host "  Ответ: count = $($countResp.count), message = $($countResp.message)" -ForegroundColor Green
} catch {
    Write-Host "  Ошибка (возможно ClickHouse не запущен): $_" -ForegroundColor Yellow
}

Write-Host "`n=== 3. Проверка PostgreSQL (через docker) ===" -ForegroundColor Cyan
$pgContainer = $null
try {
    if (Get-Command docker -ErrorAction SilentlyContinue) {
        $pgContainer = docker ps --format "{{.Names}}" 2>$null | Where-Object { $_ -match "postgres" } | Select-Object -First 1
    }
    if ($pgContainer) {
        $sql = 'SELECT COUNT(*) FROM "сырые_события_пропусков";'
        $pgOut = & docker exec $pgContainer psql -U postgres -d postgres -t -c $sql 2>&1
        if ($LASTEXITCODE -eq 0) {
            $pgOut = ($pgOut -join " ").Trim()
            Write-Host "  Контейнер: $pgContainer. Записей в сырые_события_пропусков: $pgOut" -ForegroundColor Green
        } else {
            Write-Host "  Запрос к БД не выполнен. Вывод: $($pgOut -join ' ')" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  Контейнер PostgreSQL не найден (docker ps). Если БД локальная — проверьте вручную:" -ForegroundColor Yellow
        Write-Host "  psql -U postgres -d postgres -c \"SELECT * FROM \\\"сырые_события_пропусков\\\";\"" -ForegroundColor Gray
    }
} catch {
    Write-Host "  Проверка PostgreSQL пропущена: $_" -ForegroundColor Yellow
}

Write-Host "`n=== Полный прогон завершён ===" -ForegroundColor Cyan
Write-Host "  Логи processor-service: должны быть строки про сохранение в PostgreSQL и (при запущенном ClickHouse) в ClickHouse." -ForegroundColor Gray
Write-Host "  ClickHouse Play: http://localhost:8123/play" -ForegroundColor Gray
