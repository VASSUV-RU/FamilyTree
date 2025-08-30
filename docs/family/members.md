# Участники семьи

## 1. Схема данных

| Поле      | Тип    | Описание                         |
|-----------|--------|----------------------------------|
| `userId`  | string | Пользователь.                   |
| `familyId`| string | Семья.                          |
| `role`    | string | Роль в семье (`owner`, `admin`, `member`, `guest`). |
| `status`  | string | `active` или `invited`.         |

## 2. Управление ролями

- Владелец может назначать роли другим участникам.
- Нельзя понизить или исключить владельца.
- Роль `guest` даёт только права чтения.

## 3. Сценарий смены роли

```mermaid
sequenceDiagram
    participant A as Администратор
    participant S as Сервис
    participant B as Участник
    A->>S: PATCH /families/{id}/members/{userId} {role: "admin"}
    S->>B: Уведомление о смене роли
```

## 4. Эндпоинты

### 4.1 `GET /families/{id}/members`
Список участников семьи. Требуется право `member:list`.

Успешный ответ:

```json
[
  { "userId": "u-1", "role": "admin", "status": "active" }
]
```

Ошибки: `403 FORBIDDEN`, `404 FAMILY_NOT_FOUND`.

### 4.2 `POST /families/{id}/members`
Добавить участника. Право `member:add`.

Запрос:

```json
{ "userId": "u-2", "role": "member" }
```

Успешный ответ:

```json
{ "userId": "u-2", "role": "member", "status": "invited" }
```

Ошибки: `404 USER_NOT_FOUND`, `409 ALREADY_MEMBER`.

### 4.3 `PATCH /families/{id}/members/{userId}`
Изменить роль или статус участника. Право `member:update`.

Запрос:

```json
{ "role": "admin" }
```

Успешный ответ:

```json
{ "userId": "u-2", "role": "admin", "status": "active" }
```

Ошибки: `404 MEMBER_NOT_FOUND`, `409 CANNOT_DOWNGRADE_OWNER`.

### 4.4 `DELETE /families/{id}/members/{userId}`
Исключить участника. Право `member:remove`.

Ответ `204 No Content`.

Ошибки: `404 MEMBER_NOT_FOUND`, `400 CANNOT_REMOVE_OWNER`.
