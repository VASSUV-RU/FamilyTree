# Безопасность

- Аутентификация и токены: см. `../guides/authorization/README.md`.
- CORS/CSRF, cookie-флаги, double-submit: описаны в авторизации.
- Rate limiting и защита от повторов: Redis (`used_hash:{hash}`), лимиты на `/auth/telegram/verify`.
- Хранение секретов: только в ENV/`application-local.yml` (в .gitignore), пример — `application-local.example.yml`.

