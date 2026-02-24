CREATE TABLE IF NOT EXISTS сырые_события_пропусков (
    идентификатор BIGSERIAL PRIMARY KEY,
    фио_сотрудника VARCHAR(255),
    номер_пропуска VARCHAR(255) NOT NULL,
    результат_проверки VARCHAR(255),
    дата_события TIMESTAMP
);
