# Приглашения

## 1. Схема данных

| Поле        | Тип    | Описание                         |
|-------------|--------|----------------------------------|
| `id`        | string | Идентификатор приглашения.      |
| `familyId`  | string | Семья, в которую приглашают.    |
| `inviterId` | string | Кто пригласил.                  |
| `email`     | string | Почта или идентификатор.       |
| `role`      | string | Роль после принятия.           |
| `expiresAt` | int    | Срок действия.                 |
| `status`    | string | `pending`, `accepted`, `revoked`.

## 2. Создание приглашения

```mermaid
sequenceDiagram
    participant A as Администратор
    participant S as Сервис
    participant M as Приглашённый
    A->>S: POST /families/{id}/invites {email, role}
    S->>M: Отправка письма со ссылкой
```

## 3. Принятие приглашения

```mermaid
sequenceDiagram
    participant M as Приглашённый
    participant S as Сервис
    participant F as Семья
    M->>S: POST /invites/{token}/accept
    S->>F: Добавление участника
    S->>M: Подтверждение и выдача прав
```
