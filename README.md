# Family-Tree

Сервис семейного медиаконтента и генеалогического дерева. Проект собирает и предоставляет медиаконтент семьи, описывает связи между членами семьи и управляет доступом по ролям.

## Возможности
- Регистрация семей и пользователей
- Авторизация пользователей и управление ролями
- Приглашение и добавление пользователей в семью
- Создание связей между членами семьи для отображения генеалогического дерева
- Интеграция с внешними хранилищами медиаконтента (Яндекс.Диск, Telegram, S3 и др.)
- Асинхронная синхронизация метаданных из внешних хранилищ без копирования самих файлов
- Передача фронтенду данных для запроса медиаматериалов во внешних сервисах (включая токены авторизации)

## Технологический стек
- Kotlin
- Spring Boot
- PostgreSQL
- Docker
- Интеграции: Яндекс.Диск, Telegram
- Redis (сессии, блок‑лист JWT, антиреплей)

## Архитектура и проектирование
Проект реализуется как монолит с модульной структурой, чтобы при необходимости части можно было выделять в отдельные микросервисы. Планируется разделение на слои (представление, бизнес-логика, данные) и на независимые модули:

- **auth** – регистрация, авторизация, управление пользователями и ролями
- **family** – управление семьями и их участниками, приглашения
- **media** – работа с медиаконтентом и интеграциями с внешними хранилищами
- **genealogy** – построение и хранение родственных связей
- **integration** – обертки над внешними сервисами (Яндекс.Диск, Telegram и др.)

## План дальнейшей работы
1. Детализировать архитектуру и доменную модель
2. Настроить базовую инфраструктуру проекта (Gradle, Docker, CI/CD)
3. Реализовать модуль авторизации и управления пользователями
4. Подключить внешние хранилища и реализовать синхронизацию метаданных
5. Разработать механизмы построения генеалогического дерева

---

## Документация
- Guides (доменные сценарии):
  - Авторизация: `docs/guides/authorization/README.md`
  - Семья: `docs/guides/family/families.md`, `docs/guides/family/members.md`, `docs/guides/family/invitations.md`
  - Медиа: `docs/guides/media/README.md`
  - Генеалогия: `docs/guides/genealogy/README.md`
- Reference (справочники):
  - Permissions/Roles: `docs/reference/permissions.md`, `docs/reference/roles.md`
  - Entities: `docs/reference/entities/user.md`, `docs/reference/entities/identity.md`, `docs/reference/entities/family.md`, `docs/reference/entities/member.md`, `docs/reference/entities/person.md`, `docs/reference/entities/invitation.md`, `docs/reference/entities/relation.md`
  - Errors: `docs/reference/errors.md`
  - API (OpenAPI): `docs/reference/api/openapi.yaml`
- Flows: `docs/flows/login-telegram.md`, `docs/flows/invite-accept.md`, `docs/flows/switch-family.md`, `docs/flows/refresh-token.md`, `docs/flows/sync.md`
- Требования и срез API: `docs/REQUIREMENTS.md`

Документация будет дополняться по мере развития проекта.
