# 04 — GET /auth/telegram/session/{sid}

Цель
- Позволить фронту опрашивать статус «ожидающей сессии». При готовности вернуть `AuthResponse` (access + refresh).

Контракт
- Ответ pending: `{ status: "pending" }`.
- Ответ ready: `{ status: "ready", auth: AuthResponse }`.
- Ошибки: 404 (не найдено), 410 (истёк), 409 (уже использован).

Требования
- Доставить `AuthResponse` только один раз: после успешной выдачи refresh‑cookie пометить `sid` как `used`.
- При `ready` — установить refresh‑cookie (httpOnly, Secure, SameSite=Lax/Strict, TTL).
- Конфигурируемый режим LongPolling (таймаут ожидания, например 20–30 секунд) — опционально.

Приёмочные критерии
- Pending → возвращает pending без установки cookie.
- Ready → возвращает auth, выставляет cookie, и последующие запросы по `sid` дают 409/used.

Тест‑идеи
- Переход: pending → ready (через предварительное подтверждение из шага 03).
- Проверка установки cookie и однократной выдачи.

