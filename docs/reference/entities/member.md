# Member

Участник семьи и его роль.

| Поле       | Тип    | Описание                                                            |
|------------|--------|---------------------------------------------------------------------|
| `userId`   | string | Пользователь.                                                       |
| `familyId` | string | Семья.                                                              |
| `role`     | string | Роль в семье (`owner`, `admin`, `member`, `guest`). См. [roles](../roles.md). |
| `status`   | string | Статус участия: `active` или `invited`.                             |
| `personId` | string | (опц.) Связанный узел в дереве семьи (`Person`).                    |
