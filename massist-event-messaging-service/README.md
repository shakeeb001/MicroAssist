# massist-event-messaging-service

Spring Boot service that **produces** and **consumes** RabbitMQ events. Events are published over REST and inspected through Swagger UI or a consumed-event endpoint.

## Prerequisites

- JDK 21
- Docker (for local RabbitMQ)
- Maven is optional; this module includes the Maven Wrapper (`./mvnw`)

## RabbitMQ topology

| Resource | Name |
| --- | --- |
| Topic exchange | `massist.events` |
| Queue | `massist.events.queue` |
| Binding pattern | `massist.event.#` |
| Default routing key | `massist.event.created` |

## Run locally

Start RabbitMQ:

```bash
docker compose up -d
```

Management UI: [http://localhost:15672](http://localhost:15672) (user `guest` / password `guest`)

Start the service from this directory:

```bash
./mvnw spring-boot:run
```

The API listens on [http://localhost:8080](http://localhost:8080).

## Swagger / OpenAPI

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## API examples

Publish an event:

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "user.registered",
    "routingKey": "massist.event.created",
    "source": "massist-user-service",
    "payload": { "userId": "42", "email": "ada@example.com" }
  }'
```

List recently consumed events (in-memory, newest first):

```bash
curl http://localhost:8080/api/v1/events/consumed
```

Health:

```bash
curl http://localhost:8080/actuator/health
```

## Configuration

| Property | Default | Override |
| --- | --- | --- |
| `server.port` | `8080` | `SERVER_PORT` |
| `spring.rabbitmq.host` | `localhost` | `SPRING_RABBITMQ_HOST` |
| `spring.rabbitmq.port` | `5672` | `SPRING_RABBITMQ_PORT` |
| `spring.rabbitmq.username` | `guest` | `SPRING_RABBITMQ_USERNAME` |
| `spring.rabbitmq.password` | `guest` | `SPRING_RABBITMQ_PASSWORD` |
| `massist.messaging.exchange` | `massist.events` | — |
| `massist.messaging.queue` | `massist.events.queue` | — |
| `massist.messaging.routing-key-pattern` | `massist.event.#` | — |
| `massist.messaging.default-routing-key` | `massist.event.created` | — |
| `massist.messaging.consumed-buffer-size` | `50` | — |

## Tests

```bash
./mvnw test
```
