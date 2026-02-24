CREATE TABLE IF NOT EXISTS агрегаты_событий_пропусков (
    дата_и_время_записи DateTime,
    количество_записей UInt64
) ENGINE = MergeTree()
ORDER BY дата_и_время_записи;
