# Пермишены (v0.1)

## Аутентификация
- `auth:login` — вход через Telegram / refresh (по сути, системный, редко нужен в проверках).

## Семьи
- `family:create` — создать семью.
- `family:read` — просматривать информацию о семье.
- `family:update` — редактировать настройки семьи.
- `family:delete` — удалить семью.

## Участники
- `member:invite:create` — создавать приглашения.
- `member:invite:accept` — принимать приглашение.
- `member:role:update` — менять роли участникам.
- `member:remove` — исключать участника.
- `member:leave` — покинуть семью.

## Разрешения
- `role:permission:update` — менять роли участникам.

## Источники (media sources)
- `source:read` — просматривать список источников.
- `source:create` — подключать новый источник.
- `source:update` — редактировать/переподключать.
- `source:delete` — отключать источник.

## Медиа
- `media:list` — листинг и просмотр метаданных медиа.
- `media:download` — выдача короткоживущей ссылки на скачивание.
- `media:comment:create` — добавлять комментарии.
- `media:comment:delete` — удалять свои комментарии.
- `media:comment:moderate` — удалять/модерировать чужие.
- `media:tag:create` — ставить теги персон на фото/видео.
- `media:tag:delete` — снимать теги персон.

## Генеалогия
- `genealogy:person:create` — добавить Person.
- `genealogy:person:update` — редактировать Person.
- `genealogy:person:delete` — удалить Person.
- `genealogy:relation:create` — добавить Relation.
- `genealogy:relation:update` — редактировать Relation.
- `genealogy:relation:delete` — удалить Relation.
- `genealogy:tree:read` — получить дерево семьи.

## Синхронизация
- `sync:trigger` — запуск ручного синка источника.
- `sync:observe` — просмотр логов/состояния синка.

## Безопасность / аудит
- `security:audit:view` — просматривать аудит-логи.
