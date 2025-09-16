# Family Tree Monolith (Kotlin + Spring Boot)

Много-модульный монолит:
- `api`: точка входа Spring Boot, REST‑контроллеры.
- `service`: бизнес‑логика, оркестрация доменных операций.
- `data`: JPA‑сущности и репозитории (PostgreSQL).
- `bot-telegram`: слой интеграции Telegram (заготовка, готов к расширению).

## Быстрый старт
- JDK 24+
- Gradle 9+
- PostgreSQL 16+ (локально или через docker compose)
- Redis 7+ (локально или через docker compose)

Сборка:
```
./gradlew build
```

Запуск приложения:
```
./gradlew :app:application:bootRun
```

Локальные переменные окружения:
- Скопируйте `.env.example` → `.env`, заполните значения (например, `TELEGRAM_WEBHOOK_SECRET`).
- При запуске через Docker Compose файл `.env` автоматически подхватится; при запуске через Gradle экспортируйте переменные окружения вручную.

Локальные конфиги Spring:
- Для локальной разработки используются настройки профиля `local`. Оставляем как есть сейчас; перед выкладкой в прод обязательно пересмотреть подключение профилей и секретов (см. AGENTS.md → Развёртывание/Профили).

Эндпоинты проверки и аутентификации:
- `GET /health` → `{ "status": "ok" }`
- `POST /auth/telegram/session` — создать ожидающую сессию
- `GET /auth/telegram/session/{sid}` — опрос статуса сессии
- `POST /auth/telegram/webhook` — webhook от Telegram (заголовок `X-Telegram-Bot-Api-Secret-Token` обязателен)

Гайды:
- Локальная интеграция бота и ngrok: `docs/ops/telegram-local.md`

Структура зависимостей:
- `application` → `api`, `bot-telegram`, `libs`
- `api` → `service`, `bot-telegram`, `data`, `libs`
- `service` → `data`, `libs`
- `data` → PostgreSQL/JPA/Redis

Чтобы локально проверять TelegramBot - запустить команду `ngrok http 8080`

Примечание: слой Telegram пока содержит заглушку `TelegramBotService`.

## Docker Compose (БД + Redis + сервис)
- Требуется Docker и Docker Compose v2.
- Запуск: `cp .env.example .env && docker compose up --build`
- Сервисы:
  - PostgreSQL: порт `5432` (db/family/family)
  - Redis: порт `6379` (по умолчанию пароль `yourpassword`)
  - API: порт `8080` → `http://localhost:8080/health`
- Персистентность: том `pgdata` хранит данные БД.

Примечание:
- Для локальной разработки webhook‑секрет берётся из `.env` (`TELEGRAM_WEBHOOK_SECRET`). Перед продом убедитесь, что секреты и профили не хранятся в репозитории и подставляются через секрет‑менеджер/переменные окружения.
