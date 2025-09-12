# Auth Tasks Roadmap (Telegram bot deep-link)

Цель: реализовать авторизацию через Telegram‑бота с deep‑link, «ожидающими сессиями», webhook‑подтверждением и выпуском access/refresh токенов. Каждый шаг маленький, покрываемый тестами.

Состав задач:
- 01 — Модель «ожидающей сессии» (`sid`) и хранилище
- 02 — `POST /auth/telegram/session`
- 03 — Webhook Telegram: подтверждение `/start <sid>` внутри приложения
- 04 — `GET /auth/telegram/session/{sid}` (poll/SSE)
- 05 — JWT/refresh и серверные сессии (`sess:{jti}`, `blk:{jti}`)
- 06 — `POST /auth/refresh`
- 07 — `POST /auth/logout`
- 08 — `POST /me/active-family`
- 09 — Привязка инвайта к `sid`
- 10 — Безопасность и лимиты (secret header, TTL, rate‑limit, идемпотентность)
- 11 — Аудит и метрики
- 12 — E2E сценарии и интеграционные тесты

См. детальные файлы рядом (01-*.md, 02-*.md, ...).

