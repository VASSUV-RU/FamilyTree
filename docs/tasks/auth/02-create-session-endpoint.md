# 02 — POST /auth/telegram/session

Цель
- Создать «ожидающую сессию», вернуть `sid` и deep‑link `https://t.me/{bot}?start={sid}`.

Контракт
- Запрос: `{ invitationId?: string }` (опц.).
- Ответ: `{ sid: string, deeplinkUrl: string, expiresIn: number }`.
- Ошибки: 429 (rate limit).

Требования
- Генерация `sid` через сервис из шага 01, сохранение с TTL.
- Привязка `invitationId` к записи, если передан.
- Формирование `deeplinkUrl` из конфигурации `BOT_USERNAME`.
- Rate limit (на IP или session cookie).

Приёмочные критерии
- Ответ содержит валидный `sid`, `deeplinkUrl`, корректный `expiresIn`.
- Поведение при отсутствии `BOT_USERNAME` — 503 или 500 с понятной ошибкой.

Тест‑идеи
- Успешное создание, валидация формата `deeplinkUrl`.
- С `invitationId` — проверка, что он сохранён вместе с `sid`.
- Лимиты — несколько быстрых запросов → 429.

