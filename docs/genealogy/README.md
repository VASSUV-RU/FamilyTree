# Генеалогия

Минимальная модель людей и связей родства и базовые операции чтения/изменения.

## Права (см. ../base/permissions.md)
- genealogy:person:create / genealogy:person:update / genealogy:person:delete
- genealogy:relation:create / genealogy:relation:update / genealogy:relation:delete
- genealogy:tree:read — получение дерева семьи

## Сущности (минимум)

### Person
- id: string
- familyId: string
- name: string
- birthDate?: string (ISO)
- deathDate?: string (ISO)
- meta?: object

### Relation
- id: string
- familyId: string
- type: string (parent|spouse|sibling|child и т. п.)
- fromPersonId: string
- toPersonId: string
- meta?: object

## Эндпоинты (минимальная версия)

### Дерево
- GET /families/{id}/tree — [genealogy:tree:read] получить дерево (список Person + Relation).

### Персоны
- POST /families/{id}/persons — [genealogy:person:create]
- PATCH /families/{id}/persons/{personId} — [genealogy:person:update]
- DELETE /families/{id}/persons/{personId} — [genealogy:person:delete]

### Связи
- POST /families/{id}/relations — [genealogy:relation:create]
- PATCH /families/{id}/relations/{relationId} — [genealogy:relation:update]
- DELETE /families/{id}/relations/{relationId} — [genealogy:relation:delete]

## Примечания
- Операции изменяют только контент текущей семьи; кросс‑семейные связи не допускаются.
- Валидация типов связей: не допускать циклов и нелепых композиций (например, «сам себе родитель»).

