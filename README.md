# Family Tree Monolith (Kotlin + Spring Boot)

Много-модульный монолит:
- `api`: точка входа Spring Boot, REST‑контроллеры.
- `service`: бизнес‑логика, оркестрация доменных операций.
- `data`: JPA‑сущности и репозитории (PostgreSQL).
- `bot-telegram`: слой интеграции Telegram (заготовка, готов к расширению).

## Быстрый старт
- JDK 17+
- Gradle 8+

Сборка:
```
./gradlew build
```

Запуск приложения (API):
```
./gradlew :api:bootRun
```

Конфиг: скопируйте `api/src/main/resources/application-local.example.yml` → `api/src/main/resources/application-local.yml` и заполните секреты.

Эндпоинт проверки:
- `GET /health` → `{ "status": "ok" }`

Структура зависимостей:
- `api` → `service`, `bot-telegram`
- `service` → `data`
- `data` → PostgreSQL/JPA

Примечание: слой Telegram пока содержит заглушку `TelegramBotService`.

## Docker Compose (БД + сервис)
- Требуется Docker и Docker Compose v2.
- Запуск: `docker compose up --build`
- Сервисы:
  - PostgreSQL: порт `5432` (db/family/family)
  - API: порт `8080` → `http://localhost:8080/health`
- Персистентность: том `pgdata` хранит данные БД.

