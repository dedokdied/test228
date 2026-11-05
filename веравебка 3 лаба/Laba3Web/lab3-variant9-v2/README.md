# Лабораторная работа № 3 — магазин электроники

Переработанная лабораторная работа № 2: данные хранятся в **PostgreSQL**, доступ к ним — через **Spring Data JPA / Hibernate**, схема создаётся миграциями **Flyway** (code-first), API защищено **JWT-аутентификацией** с ролевой моделью, приложение и БД запускаются через **Docker Compose**.

Стек: Java 21, Spring Boot 3.3.5, Spring Web, Spring Data JPA (Hibernate), Spring Security, Flyway, PostgreSQL 16, springdoc-openapi (Swagger UI), Docker.

## Быстрый старт (Docker Compose)

```bash
docker compose up --build
```

После запуска:

- приложение — http://localhost:8080
- Swagger UI — http://localhost:8080/swagger-ui.html
- PostgreSQL — `localhost:5432`, БД `electronics_store`

Остановить: `docker compose down`. Удалить данные БД: `docker compose down -v`.

Параметры можно переопределить в `.env` (шаблон — `.env.example`).

## Запуск без Docker

1. Поднять PostgreSQL (можно только БД из compose):

   ```bash
   docker compose up -d postgres
   ```

2. Запустить приложение:

   ```bash
   mvn spring-boot:run
   ```

Параметры подключения задаются переменными окружения `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (значения по умолчанию — в `src/main/resources/application.properties`).

## Тестовые данные

- **Товары** — SQL-скрипт Flyway `src/main/resources/db/migration/V2__seed_products.sql` (12 позиций).
- **Пользователи** — создаются при первом запуске компонентом `DataSeeder` (пароли хэшируются BCrypt):

| Логин | Пароль     | Роль  |
|-------|------------|-------|
| admin | admin123   | ADMIN |
| user  | user123    | USER  |

Пароли можно переопределить через `SEED_ADMIN_PASSWORD` и `SEED_USER_PASSWORD`.

## Доступ к API (ролевая модель)

| Метод и путь                     | Анонимный | USER | ADMIN |
|----------------------------------|:---------:|:----:|:-----:|
| `POST /api/auth/login`           | ✔         | ✔    | ✔     |
| `GET /api/products`              | ✔         | ✔    | ✔     |
| `GET /api/products/{id}`         | ✔         | ✔    | ✔     |
| `POST /api/products/{id}/purchase` | ✖       | ✔    | ✔     |
| `POST /api/products`             | ✖         | ✖    | ✔     |
| `PUT /api/products/{id}`         | ✖         | ✖    | ✔     |
| `DELETE /api/products/{id}`      | ✖         | ✖    | ✔     |
| `GET /api/auth/me`               | ✖         | ✔    | ✔     |

Пример входа:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Полученный `accessToken` передаётся в заголовке:

```
Authorization: Bearer <token>
```

В Swagger UI токен можно вставить через кнопку **Authorize** (схема `bearerAuth`).

## Схема БД

`products`:

| Поле             | Тип            | Ограничения            |
|------------------|----------------|------------------------|
| `id`             | `bigint`       | PK, identity           |
| `name`           | `varchar(255)` | NOT NULL               |
| `brand`          | `varchar(255)` | NOT NULL, индекс       |
| `price`          | `numeric(12,2)`| NOT NULL, `> 0`        |
| `count_in_stock` | `integer`      | NOT NULL, `>= 0`       |

`app_users`:

| Поле            | Тип            | Ограничения                  |
|-----------------|----------------|------------------------------|
| `id`            | `bigint`       | PK, identity                 |
| `username`      | `varchar(100)` | NOT NULL, UNIQUE, индекс     |
| `password_hash` | `varchar(100)` | NOT NULL (BCrypt)            |
| `role`          | `varchar(20)`  | NOT NULL, `ADMIN` / `USER`   |

Миграции: `V1__create_schema.sql` (схема), `V2__seed_products.sql` (тестовые данные).

## Соответствие требованиям задания

| Требование ТЗ | Реализация |
|---|---|
| Хранить данные в PostgreSQL | `spring-boot-starter-data-jpa`, драйвер `postgresql`, сервис `postgres` в `docker-compose.yml` |
| Спроектировать БД | Таблицы `products`, `app_users`, индексы, CHECK/UNIQUE-ограничения (`V1__create_schema.sql`) |
| ORM | Spring Data JPA / Hibernate (`@Entity`, `JpaRepository`, `JpaSpecificationExecutor`) |
| LINQ-запросы / `IQueryable` | Аналог в Java: динамические `Specification` (Criteria API) в `ProductSpecifications` + JPQL-запрос `decrementStock` |
| Конфигурация моделей (Fluent API) | В Java аналог Fluent API — декларативное отображение JPA: `@Entity`, `@Table`, `@Column`, `@Enumerated` с явным указанием имён, типов и ограничений |
| Code-first + миграции | Flyway (`db/migration`), `ddl-auto=validate` |
| DI | Конструкторная инъекция во всех компонентах (`@Service`, `@Repository`, `@RestController`, `@Configuration`) |
| Манипуляции с БД в репозитории | Доменные порты `ProductRepository` / `UserRepository`, реализации `DbProductRepository` / `DbUserRepository` |
| Тестовые данные | `V2__seed_products.sql` + `DataSeeder` (пользователи) |
| Docker-образ приложения | Многоступенчатый `Dockerfile` (Maven build → JRE 21, запуск от непривилегированного пользователя) |
| Docker Compose | `docker-compose.yml`: сервисы `app` и `postgres`, healthcheck, volume |
| Аутентификация API | JWT (HS256), `JwtService`, `JwtAuthenticationFilter`, `POST /api/auth/login` |
| Авторизация (опционально) | Роли `ADMIN` / `USER` / аноним, разграничение через `SecurityConfig`; пароли — BCrypt |

## Структура проекта

```
src/main/java/com/example/store
├── api                 # REST-контроллеры, DTO, обработка ошибок, OpenAPI
├── application         # бизнес-логика (ProductService), исключения
├── domain              # сущности Product/User, порты репозиториев, фильтр
└── infrastructure
    ├── persistence     # JPA-репозитории, спецификации, адаптеры портов
    ├── security        # JWT, SecurityConfig, UserDetailsService
    └── seed            # создание тестовых пользователей
src/main/resources
├── db/migration        # Flyway-миграции
└── static              # HTML/CSS/JS-клиент
```

## Тесты

```bash
mvn test
```

Интеграционные тесты поднимают встроенный PostgreSQL (zonky embedded-postgres), проверяют Flyway-миграции, валидацию JPA-маппинга, вход по JWT и разграничение доступа по ролям. Docker для тестов не требуется.

## Примечания

- Приложение работает в stateless-режиме: сессии не используются, состояние передаётся в JWT.
- Пароли в БД не хранятся в открытом виде (BCrypt).
- Покупка реализована атомарным `UPDATE ... WHERE count_in_stock >= :quantity`, поэтому одновременные запросы не приводят к отрицательному остатку.
- Для продакшена следует заменить `JWT_SECRET`, пароли БД и тестовых пользователей.
