# Scheduler

Сервис-оркестратор для формирования ежедневных отчётов по задачам пользователей.

Scheduler работает в связке с остальными сервисами Task Planner:

```text
Task Planner
    ↓ REST
Scheduler
    ↓ Kafka RPC
Summarization Service
    ↓ Kafka RPC response
Scheduler
    ↓ Kafka
Email Sender
```

Раз в сутки сервис получает из Task Planner сделанные за отчётный период и оставшиеся незавершёнными задачи пользователей, передаёт их в Summarization Service для формирования текстового отчёта и отправляет готовый результат в Kafka для дальнейшей отправки пользователю по email.

---

## Схема работы

По расписанию Scheduler:

1. формирует временной диапазон отчёта;
2. запрашивает задачи пользователей у Task Planner через REST;
3. для каждого пользователя формирует `SummarizationRequest`;
4. отправляет запрос в Summarization Service через Kafka RPC;
5. получает `SummarizationResponse`;
6. формирует `UserReport`;
7. публикует готовый отчёт в Kafka для Email Sender.

```text
@Scheduled
    ↓
calculate from / to
    ↓
GET Task Planner
/tasks/getScheduledTasks
    ↓
List<UserTask>
    ↓
SummarizationRequest
    ↓
SCHEDULER_SUMMARIZATION_REQUESTS
    ↓
Summarization Service
    ↓
SCHEDULER_SUMMARIZATION_REPLIES
    ↓
SummarizationResponse
    ↓
UserReport
    ↓
SUMMARY_SENDING_TASKS
    ↓
Email Sender
```

---

## Расписание

Scheduler запускает формирование отчётов ежедневно:

```text
23:00
Europe/Moscow
```

Для отчёта используется интервал между `23:00` предыдущего дня и `23:00` текущего дня.

Внутри приложения время рассчитывается через `Clock`, сконфигурированный для:

```text
Europe/Moscow
```

---

## Получение задач из Task Planner

Scheduler обращается к внутренней ручке Task Planner:

```http
GET /tasks/getScheduledTasks?from={Instant}&to={Instant}
```

В ответ приходит список пользователей с их задачами:

```text
UserTask
├── userId
├── email
├── finishedTasks
└── unfinishedTasks
```

Доступ к этой ручке предназначен только для Scheduler и защищён отдельным служебным ключом.

Имя HTTP-заголовка и ключ передаются через переменные окружения:

```env
SCHEDULER_AUTH_HEADER=YOUR_HEADER_NAME
SCHEDULER_AUTH_KEY=YOUR_KEY
```

Значения должны совпадать в конфигурации Task Planner и Scheduler.

---

## Summarization Service

Для каждого пользователя Scheduler преобразует полученные задачи в:

```text
SummarizationRequest
```

Запрос содержит:

```text
from
to
finishedTasks
unfinishedTasks
```

Обмен с Summarization Service реализован через Kafka по RPC-схеме.

Запрос отправляется в:

```text
SCHEDULER_SUMMARIZATION_REQUESTS
```

Ответ ожидается из:

```text
SCHEDULER_SUMMARIZATION_REPLIES
```

Для request/reply взаимодействия используется `ReplyingKafkaTemplate`.

Таймаут ожидания ответа:

```text
15 секунд
```

Полученный `SummarizationResponse` содержит сформированный текст отчёта.

---

## Отправка отчёта

После получения суммаризации Scheduler формирует:

```text
UserReport
```

```json
{
  "email": "user@example.com",
  "summarization": "Daily task report..."
}
```

Готовые отчёты публикуются в Kafka-топик:

```text
SUMMARY_SENDING_TASKS
```

Далее сообщение обрабатывается сервисом Email Sender, который непосредственно отправляет письмо пользователю.

Scheduler сам с SMTP не работает.

---

## Kafka topics

| Topic | Назначение |
|---|---|
| `SCHEDULER_SUMMARIZATION_REQUESTS` | Запросы Scheduler → Summarization Service |
| `SCHEDULER_SUMMARIZATION_REPLIES` | Ответы Summarization Service → Scheduler |
| `SUMMARY_SENDING_TASKS` | Готовые отчёты Scheduler → Email Sender |

---

## Используемые технологии

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

---

## Переменные окружения

Для доступа к внутреннему API Task Planner необходимо указать:

```env
SCHEDULER_AUTH_HEADER=YOUR_HEADER_NAME
SCHEDULER_AUTH_KEY=YOUR_KEY
```

Реальный ключ не должен попадать в Git.

Пример конфигурации находится в:

```text
.env.example
```

---

## Локальный запуск

Перед запуском Scheduler должны быть доступны:

- Task Planner;
- Kafka;
- Summarization Service;
- Email Sender — для полного прохождения цепочки доставки отчёта.

В текущей конфигурации Scheduler ожидает:

```text
Task Planner → http://localhost:8080
Kafka        → localhost:9092
```

После настройки переменных окружения приложение запускается через:

```text
SchedulerApplication
```

---

## Тесты

Основная orchestration-логика покрыта unit-тестами.

Проверяются:

- расчёт отчётного временного диапазона;
- получение пользовательских отчётов;
- преобразование задач в запрос для Summarization Service;
- обработка ответа Summarization Service;
- формирование `UserReport`;
- передача сформированного отчёта в Kafka-слой.

Инфраструктурная механика Spring Scheduler и Kafka отдельно не дублируется unit-тестами.

Запуск:

```bash
./gradlew test
```

Для Windows:

```text
gradlew.bat test
```

---

## Docker

Scheduler является частью многосервисного приложения и предполагается к запуску вместе с:

```text
Task Planner
Scheduler
Summarization Service
Email Sender
Kafka
PostgreSQL
```

На текущем этапе отдельный Dockerfile для Scheduler ещё не добавлен.

В дальнейшем сервис планируется упаковать в Docker-образ и включить в общий Docker Compose стек проекта вместе с остальными сервисами и инфраструктурой.

Перед контейнеризацией также потребуется учитывать, что текущие адреса:

```text
http://localhost:8080
localhost:9092
```

рассчитаны на локальный запуск. При запуске сервисов внутри Docker Compose подключения должны использовать адреса соответствующих сервисов либо передаваться через конфигурацию окружения.
