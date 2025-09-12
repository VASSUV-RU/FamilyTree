# Flow: Вход через Telegram‑бот (deep‑link)

Сценарий входа описан в гайде: [guides/authorization](../guides/authorization/README.md).
- Шаги: фронт создаёт «ожидающую сессию» (`POST /auth/telegram/session`) → получает `sid` и deep‑link → пользователь нажимает Start в боте → Telegram шлёт webhook на наш сервер, бот‑хендлер видит `/start <sid>` и внутри приложения помечает `sid` подтверждённым → фронт опрашивает `GET /auth/telegram/session/{sid}` и получает `AuthResponse` (access + refresh).
- Связанные сущности: `User`, `Member` (активная семья), см. `../reference/entities/`.
