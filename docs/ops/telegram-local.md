# Локальная интеграция Telegram‑бота (ngrok)

Этот гайд поможет привязать существующего Telegram‑бота к локальному окружению проекта и протестировать вход через deep‑link.

## Предпосылки
- Есть бот в Telegram (username и токен от BotFather).
- Установлен Docker и Docker Compose, либо готовность запускать приложение через Gradle.
- Установлен ngrok (HTTPS‑туннель к локальному `localhost:8080`).

## Переменные окружения
Создайте `.env` из примера и заполните значения:

```bash
cp .env .env
```

Ключевые переменные:
- `BOT_USERNAME` — имя вашего бота без `@`.
- `BOT_TOKEN` — токен бота от BotFather (нужен для исходящих сообщений).
- `TELEGRAM_SESSION_TTL` — TTL «ожидающей сессии» в секундах (например, `300`).
- `TELEGRAM_WEBHOOK_SECRET` — произвольный секрет для заголовка `X-Telegram-Bot-Api-Secret-Token`.
- `REDIS_HOST`, `REDIS_PASSWORD` — настройки Redis (совпадают с docker-compose по умолчанию).

Docker Compose автоматически пробросит их в `AUTH_TELEGRAM_*` свойства Spring. При локальном запуске без Docker используйте переменные окружения Spring: `AUTH_TELEGRAM_BOT_USERNAME`, `AUTH_TELEGRAM_BOT_TOKEN`, `AUTH_TELEGRAM_SESSION_TTL_SECONDS`, `AUTH_TELEGRAM_WEBHOOK_SECRET` (или `application-local.yml`).

## Запуск инфраструктуры
Вариант A — через Docker Compose (БД + Redis + приложение):

```bash
# один раз: подготовить .env
cp .env .env

# запустить сервисы
docker compose up --build
```

Вариант B — локально через Gradle (требуются запущенные Redis и Postgres):

```bash
export SPRING_PROFILES_ACTIVE=local
export AUTH_TELEGRAM_BOT_USERNAME="<your_bot>"
export AUTH_TELEGRAM_SESSION_TTL_SECONDS=300
export AUTH_TELEGRAM_WEBHOOK_SECRET="<secret>"

./gradlew :app:application:bootRun
```

Приложение слушает `http://localhost:8080`.

## Настройка ngrok
1) Установите и залогиньтесь в ngrok:

```bash
# macOS (Homebrew)
brew install ngrok
ngrok config add-authtoken <NGROK_AUTHTOKEN>
```

2) Запустите HTTPS‑туннель к локальному приложению:

```bash
ngrok http 8080
```

Скопируйте публичный HTTPS‑URL вида `https://<subdomain>.ngrok-free.app`.

(Опционально) Чтобы URL не менялся, используйте резервированный поддомен в платном плане и запускайте: `ngrok http --domain=<your-subdomain>.ngrok.app 8080`.

## Привязка webhook бота
Установите webhook вашего бота на URL эндпоинта API. В проекте webhook обрабатывается по пути:

```
POST /auth/telegram/webhook
```

Выполните запрос к Telegram API (замените токен бота, домен и секрет):

```bash
BOT_TOKEN="<telegram_bot_token>"
PUBLIC_URL="https://<subdomain>.ngrok-free.app"
SECRET="<same_secret_as_TELEGRAM_WEBHOOK_SECRET>"

curl -sS "https://api.telegram.org/bot${BOT_TOKEN}/setWebhook" \
  -F "url=${PUBLIC_URL}/auth/telegram/webhook" \
  -F "secret_token=${SECRET}" \
  -F "drop_pending_updates=true" \
  -F "allowed_updates=[\"message\"]"
```

Проверьте ответ `{"ok":true, ...}`. Чтобы снять webhook: `setWebhook` c пустым `url`.

Важно: `secret_token` в Telegram должен в точности совпадать с `TELEGRAM_WEBHOOK_SECRET`/`AUTH_TELEGRAM_WEBHOOK_SECRET` в вашем приложении — иначе сервер вернёт 401.

## Быстрая проверка сценария логина
1) Создайте «ожидающую» сессию:

```bash
curl -sS -X POST http://localhost:8080/auth/telegram/session \
  -H 'Content-Type: application/json' \
  -d '{"invitationId":null}'
```

Ответ включает `sid` и `deeplinkUrl`, например:

```json
{ "sid": "Sabc123", "deeplinkUrl": "https://t.me/<bot>?start=Sabc123", "expiresIn": 300 }
```

2) Нажмите deep‑link или отправьте в чат бота команду `/start Sabc123`.

3) Бот (webhook) отметит сессию подтверждённой. На фронте обычно идёт опрос. Для ручной проверки:

```bash
curl -sS http://localhost:8080/auth/telegram/session/Sabc123 | jq .
```

В статусе READY вернётся `AuthResponse` (access/refresh). Если сессия не найдена/истекла — убедитесь, что `sid` ещё валиден (TTL см. `TELEGRAM_SESSION_TTL`).

## Отладка и частые ошибки
- 401 на `/auth/telegram/webhook`: не совпадает `X-Telegram-Bot-Api-Secret-Token` — проверьте `TELEGRAM_WEBHOOK_SECRET` и `secret_token` в `setWebhook`.
- 404/410/409 при опросе сессии: сессия не найдена/истекла/уже использована.
- Нет входящих запросов на webhook: проверьте, что приложение доступно по `PUBLIC_URL`, `ngrok` запущен, на стороне Telegram установлен верный webhook.
- Бот не отвечает в Telegram‑чате: в текущей версии бот не отправляет сообщения, а только подтверждает `/start <sid>` через webhook. Это ожидаемо.

## Безопасность
- Не коммитьте токен бота и секреты.
- Для продакшена используйте отдельный домен/сертификат и секреты только из переменных окружения или менеджера секретов.
