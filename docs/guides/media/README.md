# Медиа и источники

Документ описывает базовые понятия, права и минимальные эндпоинты для работы с медиаконтентом и внешними источниками.

## Термины
- Источник (Source) — подключение к внешнему хранилищу (например, Telegram, Яндекс.Диск).
- Объект медиа (MediaItem) — фото/видео/документ, индексированный из источника (без копирования файла).

## Права (см. [reference/permissions.md](../../reference/permissions.md))
- source:read / source:create / source:update / source:delete
- media:list / media:download
- media:comment:create / media:comment:delete / media:comment:moderate
- media:tag:create / media:tag:delete

## Сущности (минимум)

### Source
- id: string
- familyId: string
- type: string (telegram | yandex_disk | ...)
- config: object (секреты/токены не возвращаются в API)
- createdAt: int

### MediaItem
- id: string
- familyId: string
- sourceId: string
- type: string (photo | video | doc)
- createdAt: int
- metadata: object (размеры, длительность, превью и т. п.)

### Comment
- id: string
- mediaId: string
- authorId: string
- text: string
- createdAt: int

## Эндпоинты (минимальная версия)

### Источники
- GET /families/{id}/sources — [source:read] список источников семьи.
- POST /families/{id}/sources — [source:create] подключить источник.
- PATCH /families/{id}/sources/{sourceId} — [source:update] изменить настройки.
- DELETE /families/{id}/sources/{sourceId} — [source:delete] отключить.

### Медиа
- GET /families/{id}/media — [media:list] листинг медиа (фильтры по sourceId, типу, дате).
- GET /media/{mediaId} — [media:list] метаданные конкретного медиа.
- POST /media/{mediaId}/download-link — [media:download] короткоживущая ссылка или прокси‑токен для внешнего сервиса.

### Комментарии
- GET /media/{mediaId}/comments — чтение доступно при [media:list];
  права на создание/удаление см. ниже.
- POST /media/{mediaId}/comments — [media:comment:create] добавить комментарий.
- DELETE /media/{mediaId}/comments/{commentId} — [media:comment:delete] удалить свой; [media:comment:moderate] — модерация чужих.

### Теги персон (опционально в MVP)
- POST /media/{mediaId}/tags — [media:tag:create] указать персону на медиа.
- DELETE /media/{mediaId}/tags/{tagId} — [media:tag:delete] удалить тег.

## Заметки по интеграциям
- Файлы не копируются в сервис; индексируются только метаданные и выдается короткоживущая ссылка/токен.
- Синхронизация инициируется по расписанию/событию или вручную [sync:trigger]; состояние доступно по [sync:observe].
