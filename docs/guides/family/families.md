# Семьи

Документ описывает сущность семьи и операции с ней.

Схема данных: см. [reference/entities/family.md](../../reference/entities/family.md).
Права: см. [reference/permissions.md](../../reference/permissions.md).

## 1. Операции API

### 1.1 `POST /families`
Создать новую семью. Требуется право `family:create`.

Запрос:
```json
{ "name": "Ивановы" }
```

Успешный ответ:
```json
{ "id": "f-1", "name": "Ивановы" }
```

Ошибки: `409 DUPLICATE_NAME`.

### 1.2 `GET /families/{id}`
Просмотр информации о семье. Право `family:read`.

### 1.3 `PATCH /families/{id}`
Обновление атрибутов семьи. Право `family:update`.

### 1.4 `DELETE /families/{id}`
Удаление семьи. Право `family:delete`. Доступно только владельцу.
