# Invitation

Приглашение в семью.

| Поле        | Тип    | Описание                         |
|-------------|--------|----------------------------------|
| `id`        | string | Идентификатор приглашения.      |
| `familyId`  | string | Семья, в которую приглашают.    |
| `inviterId` | string | Кто пригласил.                  |
| `contact`   | string | Никнейм или телефон в Telegram. |
| `role`      | string | Роль после принятия.            |
| `expiresAt` | int    | Срок действия.                  |
| `status`    | string | Статус: `pending`, `accepted`, `revoked`. |

