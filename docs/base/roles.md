# Роли

- owner — владелец семьи, имеет полный доступ.
- admin — администратор семьи, управляет участниками и настройками.
- member — участник семьи, может взаимодействовать с контентом.
- guest — гость с минимальным доступом.

## Права по умолчанию

Ниже приведена таблица с основными пермишенами и доступом к ним по умолчанию для каждой роли. В конкретной семье эти права могут быть настроены иначе.

| Пермишен \\ Роль | owner | admin | member | guest |
| --- | --- | --- | --- | --- |
| family:read | ✅ | ✅ | ✅ | ✅ |
| family:update | ✅ | ✅ | ❌ | ❌ |
| member:invite:create | ✅ | ✅ | ✅ | ❌ |
| member:role:update | ✅ | ✅ | ❌ | ❌ |
| role:permissions:update | ✅ | ❌ | ❌ | ❌ |
| source:create | ✅ | ✅ | ❌ | ❌ |
| media:list | ✅ | ✅ | ✅ | ✅ |
| media:comment:create | ✅ | ✅ | ✅ | ❌ |
| media:comment:moderate | ✅ | ✅ | ❌ | ❌ |
| genealogy:person:delete | ✅ | ✅ | ❌ | ❌ |
| sync:trigger | ✅ | ✅ | ❌ | ❌ |
| security:audit:view | ✅ | ✅ | ❌ | ❌ |
