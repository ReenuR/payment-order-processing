# Payment Order Processing System

A production-grade backend microservice for processing payment orders using Java 17, Spring Boot, Apache Kafka, PostgreSQL, and Stripe. Built with reliability, idempotency, and observability at its core.

---

## Architecture Overview

```
Client
  │
  ▼
PaymentController (REST API)
  │
  ▼
PaymentService (Orchestration + Idempotency)
  │
  ▼
PaymentProcessor (Stripe Integration + Retry Logic)
  │
  ├──► Stripe API (Payment Gateway)
  │
  └──► Kafka Producer
          │
          ├──► payments.success ──► PaymentEventConsumer ──► Email Notification
          │
          └──► payments.failed  ──► PaymentEventConsumer ──► Email Notification
```

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core language |
| Spring Boot 3.2.3 | Application framework |
| PostgreSQL 15 | Persistent storage |
| Apache Kafka | Async event streaming |
| Stripe API | Payment gateway |
| Spring Retry | Retry with exponential backoff |
| Lombok | Boilerplate reduction |
| Docker Compose | Local infrastructure |

---

## Features

### Idempotency
Every payment request requires a client-supplied `idempotencyKey`. The system checks this key against the database before processing — guaranteeing exactly-once payment execution regardless of duplicate requests or network retries.

### Stripe Integration
Payments are processed via Stripe's PaymentIntent API in test mode. Stripe-level idempotency keys are passed on every charge to prevent double billing even if our service crashes mid-transaction.

### Retry with Exponential Backoff
On transient payment failures (network errors, Stripe 5xx), the system retries up to 3 times using Spring Retry with exponential backoff:
- Attempt 1 → fails → wait 2 seconds
- Attempt 2 → fails → wait 4 seconds
- Attempt 3 → fails → mark as PAYMENT_FAILED

Card declines (permanent failures) are not retried.

### Event-Driven Architecture
Payment outcomes are published to Kafka topics asynchronously:
- `payments.success` — consumed by Notification Service on success
- `payments.failed` — consumed by Notification Service and (future) Inventory Service on failure

### Payment State Machine
Every payment moves through a defined set of states:

```
ORDER_CREATED → INVENTORY_RESERVED → PAYMENT_INITIATED → PAYMENT_SUCCESS
                                                        ↘ PAYMENT_FAILED → INVENTORY_RELEASED → CUSTOMER_NOTIFIED
```

### Global Exception Handling
A `@ControllerAdvice` global handler maps all exceptions to meaningful HTTP responses — no stack traces exposed to clients.

---

## Payment States

| State | Description |
|---|---|
| `ORDER_CREATED` | Payment order received and persisted |
| `INVENTORY_RESERVED` | Items held for this order |
| `PAYMENT_INITIATED` | Stripe charge attempt started |
| `PAYMENT_SUCCESS` | Payment confirmed by Stripe |
| `PAYMENT_FAILED` | All retries exhausted |
| `INVENTORY_RELEASED` | Reserved stock returned (on failure) |
| `CUSTOMER_NOTIFIED` | Email sent to customer |

---

## API Endpoints

### Process Payment
```
POST /api/v1/orders/pay
```

**Request Body:**
```json
{
  "orderId": "a3f8c2d1-4b5e-4c6d-8e9f-123456789abc",
  "customerId": "b4c5d6e7-5f6a-7b8c-9d0e-234567890bcd",
  "paymentType": "CREDIT_CARD",
  "paymentMethodId": "pm_card_visa",
  "currency": "usd",
  "paymentAmount": 99.99,
  "idempotencyKey": "unique-key-001"
}
```

**Response (202 Accepted):**
```json
{
  "orderId": "a3f8c2d1-4b5e-4c6d-8e9f-123456789abc",
  "customerId": "b4c5d6e7-5f6a-7b8c-9d0e-234567890bcd",
  "paymentId": "d3cc379e-161b-49a5-aaae-0109abe27efa",
  "paymentStatus": "PAYMENT_SUCCESS",
  "transactionId": "pi_3TFz21RjcwWvTIj31xP7pP6o",
  "createdAt": "2026-03-28T08:44:13.377781",
  "failureReason": null
}
```

**Error Responses:**

| Status | Scenario |
|---|---|
| 400 Bad Request | Invalid or missing fields |
| 402 Payment Required | Card declined |
| 503 Service Unavailable | Payment gateway temporarily unavailable |
| 500 Internal Server Error | Unexpected system error |

---

## Running Locally

### Prerequisites
- Java 17
- Docker Desktop
- Maven 3.9+

### Steps

**1. Clone the repository:**
```bash
git clone https://github.com/ReenuR/payment-order-processing.git
cd payment-order-processing
```

**2. Set Stripe API key:**
```bash
export STRIPE_API_KEY=sk_test_your_key_here
```

**3. Start infrastructure (PostgreSQL + Kafka + Zookeeper):**
```bash
docker-compose up -d
```

**4. Run the application:**
```bash
mvn spring-boot:run
```

**5. Test the API:**

Use Postman or curl:
```bash
curl -X POST http://localhost:8080/api/v1/orders/pay \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "a3f8c2d1-4b5e-4c6d-8e9f-123456789abc",
    "customerId": "b4c5d6e7-5f6a-7b8c-9d0e-234567890bcd",
    "paymentType": "CREDIT_CARD",
    "paymentMethodId": "pm_card_visa",
    "currency": "usd",
    "paymentAmount": 99.99,
    "idempotencyKey": "test-key-001"
  }'
```

### Stripe Test Cards

| Card Number / Method ID | Behavior |
|---|---|
| `pm_card_visa` | Always succeeds |
| `pm_card_chargeDeclined` | Always declined |
| `pm_card_chargeDeclinedInsufficientFunds` | Insufficient funds |

---

## Project Structure

```
src/main/java/com/payments/payment_order_processing/
├── client/
│   └── StripeClient.java
├── controller/
│   └── PaymentController.java
├── dto/
│   ├── PaymentRequestDTO.java
│   └── PaymentResponseDTO.java
├── entity/
│   └── Payment.java
├── enums/
│   ├── PaymentStatus.java
│   └── PaymentType.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── PaymentDeclinedException.java
│   └── PaymentProcessingException.java
├── kafka/
│   ├── consumer/
│   │   └── PaymentEventConsumer.java
│   ├── event/
│   │   ├── PaymentFailedEvent.java
│   │   └── PaymentSucceededEvent.java
│   └── producer/
│       └── PaymentEventProducer.java
├── repository/
│   └── PaymentRepository.java
└── service/
    ├── PaymentProcessor.java
    └── PaymentService.java
```

---

## Future Improvements

- **Notification Service** — dedicated microservice for email/SMS notifications via SendGrid
- **Inventory Service** — reserve and release inventory based on payment events
- **Reconciliation Job** — scheduled job to recover orders stuck in `PAYMENT_INITIATED` state
- **Distributed Tracing** — add trace IDs across services using Spring Cloud Sleuth
- **Metrics & Monitoring** — Prometheus + Grafana dashboards for payment success rates and latency
- **Refund Support** — handle payment reversals via Stripe Refunds API

---

## Author

**Reenu** — Backend Engineer  
[GitHub](https://github.com/ReenuR/payment-order-processing)