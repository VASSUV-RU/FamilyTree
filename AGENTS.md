# Repository Guidelines

Это руководство помогает участникам работать согласованно с кодом и документацией Family‑Tree (Kotlin + Spring Boot, PostgreSQL, Docker). Держите изменения небольшими, сфокусированными и документируйте их.

## Структура проекта и модули
- Документация: каталог `docs/` (например, `docs/authorization/README.md`, `docs/family/members.md`). При добавлении страниц обновляйте перекрёстные ссылки.
- Корневой обзор: `README.md` — коротко о проекте с ссылками в `docs/`.
- Сервис разделен на 6 слоев - application, api, service, data, bot-telegram, libs
- Ошибки нужно возвращать с помощью исключений по аналогии с UnauthorizeException из app/libs/config
- Тесты пишутся в каждом слое для необходимых классов и методов в аналогичном пакете в разделе с тестами
- Тесты для сервисов и контроллеров писать в отдельных файлах для каждого внешнего метода
- Тесты проверяем с помощью Mockito-kotlin
- Если что-то еще не реализовано, и нужно использовать какую-то функцию - оставлять todo пояснение, что тут в будущем нужно что то вызвать

## Сборка, тесты и локальный запуск
- Сборка: `./gradlew build`
- Тесты: `./gradlew test`
- Запуск приложения: `./gradlew :app:application:bootRun`
- Docker Compose (локально): `cp .env.example .env && docker compose up --build`
- Профили: локально активируем `local` (через `SPRING_PROFILES_ACTIVE=local` или .env)

Заметки по миграциям БД
- Миграции Liquibase хранятся в модуле `app/data` в `src/main/resources/db/changelog`.
- Главный файл: `db/changelog/db.changelog-master.xml` (classpath), подключается из `application.yml` через `spring.liquibase.change-log`.
- Сами миграции группируются в папках по годам. И имеют названия например - [2025-09-08-001-create-persons.xml](app/data/src/main/resources/db/changelog/2025/2025-09-08-001-create-persons.xml)
- Модуль `application` не содержит миграций — добавляйте/правьте их в `app/data`.

Переменные окружения (локально через `.env`):
- `BOT_USERNAME`, `TELEGRAM_SESSION_TTL`, `TELEGRAM_WEBHOOK_SECRET`
- БД: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- Redis: `REDIS_HOST`, `REDIS_PASSWORD`

Заметка: сейчас для удобства разработки включены «локальные» настройки. Перед выкладкой в прод — пересмотреть активацию профилей и секретов (секреты только через переменные окружения/секрет‑менеджер).

## Стиль кода и соглашения об именовании
- Kotlin/JDK: Kotlin 2.2, JDK 24 (см. `jvmToolchain(24)`).
- Логи: SLF4J, без `println` в продуктивном коде.
- Репозитории/сущности: именовать методы по домену (пример: `countPersons()` вместо `countUsers()` для Person).

## Руководство по тестированию
- Моки: Mockito-Kotlin.
- Контроллеры: `@WebMvcTest`, тесты группируем по внешним операциям (пример: для Telegram — отдельные файлы под `/session` и `/webhook`).
- Сервисы: отдельный файл на публичный метод сервиса.
- Data-интеграция: при работе с реальным Redis/PostgreSQL — Testcontainers.
- Именование: `ClassName_Method_BehaviorExpected`.

Ошибки/исключения:
- Для возврата ошибок используйте исключения из `app/libs/config` (`NotFoundException`, `ConflictException`, `GoneException`, `UnauthorizeException`) и хелперы `notFoundError()`, `conflictError()`, `goneError()`, `unauthorizeError()`.
- Глобальная обработка ошибок находится в `app/libs/exceptionhandler`.

## Коммиты и Pull Request’ы
- Коммиты: Conventional Commits (пример: `feat(auth): add email login`). Сообщение — повелительное наклонение и чёткая область.
- PR: понятное описание, связанные issue, скриншоты/логи для UX/API‑изменений, заметки по тестированию. Небольшие, однотематические PR предпочтительны.
- Чек‑лист: сборка зелёная, тесты добавлены/обновлены, документация обновлена (`README.md`, `docs/*`).

## Безопасность и конфигурация
- Не коммитьте секреты. Используйте переменные окружения или `application-local.yml` (в .gitignore) и образец `application-local.example.yml`.
- Минимальные права для пользователей БД, регулярная ротация токенов, валидация и санитизация внешних входных данных.
