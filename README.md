# Scheduler

Сервис-оркестратор для формирования ежедневных отчётов по задачам пользователей.

Scheduler связывает Task Planner, Summarization Service и Email Sender в единую цепочку формирования и доставки ежедневного summary-отчёта.

## Технологии

- Java 21
- Spring Boot
- Spring Scheduler
- Spring Kafka
- Spring `RestClient`
- MapStruct
- Lombok
- Gradle
- JUnit 5
- Mockito
- Docker
- Docker Compose

## Роль в системе

Scheduler является оркестратором процесса формирования ежедневного отчёта.

```text
                                     ┌───────────────────────┐
                                     │ Summarization Service │
                                     └──────────┬────▲───────┘
                                                │    │
                                    Kafka reply │    │ Kafka request
                                                ▼    │
┌──────────────┐       HTTP request          ┌───────────┐
│ Task Planner │ ◄────────────────────────── │ Scheduler │
│              │ ──────────────────────────► │           │
└──────────────┘       tasks / users         └─────┬─────┘
                                                  │
                                                  │ Kafka:
                                                  │ SUMMARY_SENDING_TASKS
                                                  ▼
                                          ┌──────────────┐
                                          │ Email Sender │
                                          └──────────────┘
```

По расписанию Scheduler:

1. запрашивает у Task Planner завершённые и незавершённые задачи пользователей за отчётный период;
2. передаёт данные каждого пользователя в Summarization Service;
3. получает сформированный `SummaryResponse`;
4. формирует `UserReport`;
5. публикует готовый отчёт в Kafka для Email Sender.

Таким образом, Scheduler управляет всей цепочкой формирования отчёта, но сам не занимается ни суммаризацией текста, ни отправкой email.

## Деплой

Сервис является частью развёрнутого приложения:

[http://77.221.141.215:5173](http://77.221.141.215:5173)

Полный Docker Compose-стек развёрнут на VPS и включает все сервисы приложения, PostgreSQL и Kafka.

## Расписание

Формирование отчётов запускается ежедневно:

```text
23:00
Europe/Moscow
```

Для отчёта используется период между `23:00` предыдущего дня и `23:00` текущего дня.

## Взаимодействие с Task Planner

Scheduler получает данные через внутренний endpoint Task Planner:

```text
GET /tasks/getScheduledTasks?from={Instant}&to={Instant}
```

Доступ защищён отдельным служебным ключом.

Для этого используются:

```env
SCHEDULER_AUTH_HEADER=...
SCHEDULER_AUTH_KEY=...
```

Значения должны совпадать с конфигурацией Task Planner.

## Переменные окружения

Основные переменные окружения:

```env
SCHEDULER_AUTH_HEADER=...
SCHEDULER_AUTH_KEY=...

TASK_PLANNER_BASE_URL=...
KAFKA_BOOTSTRAP_SERVERS=...
```

Пример конфигурации находится в:

```text
.env.example
```

Реальные секреты не должны попадать в Git.

## Запуск всего проекта

Scheduler входит в общий Docker Compose-стек проекта.

Общий `compose.yml` находится в репозитории Task Planner и позволяет запустить Scheduler вместе с остальными сервисами, PostgreSQL и Kafka из готовых Docker-образов.

Инструкция по запуску всего проекта находится в README Task Planner.

## Локальная разработка

Перед локальным запуском Scheduler необходимо сначала поднять общий Docker Compose-стек из репозитория Task Planner:

```bash
docker compose up -d
```

После этого контейнер Scheduler можно остановить:

```bash
docker compose stop scheduler
```

И запустить сервис локально из IDE или через Gradle:

```bash
./gradlew bootRun
```

При локальном запуске используются:

```text
Task Planner -> http://localhost:8080
Kafka        -> localhost:9094
```

Необходимые секреты передаются через environment variables или конфигурацию запуска IDE.

## Тесты

Основная orchestration-логика покрыта unit-тестами.

Проверяются расчёт отчётного периода, получение задач из Task Planner, взаимодействие с Summarization Service и формирование готового отчёта для Email Sender.

Запуск:

```bash
./gradlew test
```

## CI/CD

При push в `main` GitHub Actions запускает тесты, собирает Docker-образ и публикует его в Docker Hub.
