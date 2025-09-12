# Приглашения

Схема данных: см. [reference/entities/invitation.md](../../reference/entities/invitation.md).

Только участники с пермишеном `member:invite:create` (обычно owner и admin) могут создавать приглашения.

## 1. Создание приглашения через Telegram

```mermaid
sequenceDiagram
    participant A as Приглашающий
    participant B as Telegram-бот
    participant S as Сервис
    participant M as Приглашённый
    A->>B: Команда "Добавить участника"
    B->>A: Предложение выбрать контакт
    A->>B: Контакт и роль
    B->>S: POST /families/{id}/invites {contact, role}
    S-->>B: invitationId
    B->>M: Ссылка на фронт с invitationId
```

## 2. Принятие приглашения

```mermaid
sequenceDiagram
    participant M as Приглашённый
    participant F as Фронт
    participant B as Telegram-бот
    participant S as Сервис
    M->>F: Переход по ссылке с invitationId
    F->>M: Запрос ника или телефона
    M->>F: Ввод данных
    F->>M: Показ ссылки на бота
    M->>B: Подтверждение в боте
    B->>S: POST /invites/{token}/accept
    S->>M: Привязка к семье и выдача прав
    S->>B: (опц.) Добавление в семейный чат
```

Если приглашённый уже имеет аккаунт в сервисе, шаги ввода никнейма и создания учётной записи пропускаются: бот сразу вызывает `POST /invites/{token}/accept`, пользователь получает новые права и актуальный `accessToken`.

## 3. Эндпоинты

### 3.1 `POST /families/{id}/invites`
Создать новое приглашение. Обычно вызывается Telegram-ботом после выбора контакта.

Параметры пути:
- `id` — идентификатор семьи.

Тело запроса:

```json
{ "contact": "@username", "role": "member", "personId": "p-123" }
```

Успешный ответ:

```json
{
  "id": "inv-1",
  "familyId": "f-1",
  "inviterId": "u-1",
  "contact": "@username",
  "role": "member",
  "personId": "p-123",
  "expiresAt": 0,
  "status": "pending"
}
```

Ошибки: `403 FORBIDDEN`, `404 FAMILY_NOT_FOUND`, `409 ALREADY_INVITED`.

### 3.2 `GET /families/{id}/invites`
Список приглашений семьи.

Параметры пути:
- `id` — идентификатор семьи.

Успешный ответ:

```json
[
  {
    "id": "inv-1",
    "contact": "@username",
    "role": "member",
    "status": "pending"
  }
]
```

Ошибки: `403 FORBIDDEN`, `404 FAMILY_NOT_FOUND`.

### 3.3 `POST /invites/{token}/accept`
Принять приглашение по токену. Вызывается ботом после подтверждения пользователя.

Параметры пути:
- `token` — уникальный токен приглашения.

Тело запроса отсутствует.

Успешный ответ:

```json
{
  "id": "inv-1",
  "familyId": "f-1",
  "role": "member",
  "personId": "p-123",
  "status": "accepted"
}
```

Ошибки: `400 INVITE_EXPIRED`, `404 INVITE_NOT_FOUND`, `409 ALREADY_ACCEPTED`.

### 3.4 `DELETE /families/{id}/invites/{inviteId}`
Отозвать приглашение.

Параметры пути:
- `id` — идентификатор семьи.
- `inviteId` — идентификатор приглашения.

Ответ `204 No Content`.

Ошибки: `403 FORBIDDEN`, `404 INVITE_NOT_FOUND`.

## 4. Уведомления через Telegram

### 4.1 Личные уведомления
Бот отправляет сообщения конкретным пользователям о событиях в семье.

### 4.2 Уведомления в семейный чат
- Владелец семьи вручную приглашает бота в чат.
- После подключения бот может публиковать сервисные сообщения и приглашать новых участников в чат (если Telegram позволяет).

## 5. Привязка к существующему Person через приглашение
- Если при создании приглашения указан `personId`, то после принятия инвайта создаётся членство `Member(active)` с полем `personId`, указывающим на существующую персону в дереве.
- Если `personId` не указан, привязка может быть выполнена вручную owner/admin после онбординга или пользователем с последующим подтверждением.
