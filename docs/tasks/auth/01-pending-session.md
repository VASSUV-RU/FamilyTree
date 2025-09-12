# 01 — Ожидающая сессия (`sid`) и хранилище

Цель
- Ввести сущность «ожидающая сессия» с `sid` (opaque, ≤64 байт), статусами `pending|ready|expired|used`, TTL ~5 минут.

Требования
- Генерация `sid`: криптографически стойкая, короткая (например, base64url 16–24 байта).
- Хранение в Redis: `tg:pending:{sid}` → JSON { status, createdAt, invitationId? , userId? , auth? } с TTL.
- Одноразовость: после перевода в `ready/used` повторное подтверждение отклоняется.
- Идемпотентность: повторная обработка одного и того же `sid` не создаёт дубликатов пользователей/сессий.

Интерфейс (псевдо)
- `PendingSessionRepository.put(sid, data, ttl)`
- `PendingSessionRepository.get(sid)`
- `PendingSessionRepository.markReady(sid, authPayload)`
- `PendingSessionRepository.markUsed(sid)` / `expire(sid)`

Приёмочные критерии
- Можно создать запись с TTL и прочитать её до истечения.
- После истечения запись недоступна и помечается как `expired` при попытке чтения.
- Двойное подтверждение одного `sid` возвращает конфликт.

Тест‑идеи
- Создание → чтение → истечение TTL.
- Два параллельных markReady: один успех, второй — конфликт.
- Сохранение и извлечение `invitationId` для дальнейшего шага.

