# 04 — Пересборка капабилити и preferredFamily после онбординга

Цель
- После создания пользователя/семьи/членства выдать валидные права и активную семью в `AuthResponse`.

Требования
- Реализовать `CapabilityService.rebuildFor(userId, familyId)`:
  - загрузить роли/пермишены для `Member`;
  - сформировать `scopes`, `capVersion`, `perm:{userId}:{familyId}` в Redis;
  - вернуть структуру для конструирования JWT payload.
- При онбординге владельца или принятии инвайта обновлять `preferredFamilyId` и собирать `AuthResponse` (access, refresh).
- Данные должны попадать в `sess:{jti}`; при отсутствии семьи возвращать `unauthorizeError()`.
- Логика вынесена в `app/service`, вызывается из `AuthService` перед финальным ответом.

Приёмочные критерии
- После подтверждения Telegram `GET /auth/telegram/session/{sid}` возвращает `AuthResponse` c правами активной семьи.
- `sess:{jti}` содержит `familyId`, `scopes`, `capVersion`.
- Повторное подтверждение не нарушает консистентность (идемпотентность rebuild).

Тест‑идеи
- Unit `CapabilityService`: изменяем роли → обновляются `scopes` и `capVersion`.
- Интеграционный тест auth-флоу: проверка содержимого кэша после онбординга.
- Тест обработки ошибок: отсутствующий `Member` → `unauthorizeError()`.
