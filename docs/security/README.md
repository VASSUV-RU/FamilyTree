# Безопасность

- Аутентификация и токены: см. [guides/authorization](../guides/authorization/README.md).
- CORS/CSRF, double-submit и безопасное хранение refresh-токена (передаётся в теле запроса): описаны в авторизации.
- Rate limiting и защита от повторов: Redis (`used_hash:{hash}`), лимиты на `POST /auth/telegram/session` и на обработку webhook (по `telegram_id`/`sid`).
- Хранение секретов: только в ENV/`application-local.yml` (в .gitignore), пример — `application-local.example.yml`.
- Интеграция Telegram‑бота: для webhook использовать секретный заголовок (например, `X-Telegram-Bot-Api-Secret-Token`) на `POST /telegram/webhook`.
