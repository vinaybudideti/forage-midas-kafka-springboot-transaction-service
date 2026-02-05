# Forage Midas — Kafka Transaction Service (Spring Boot + JPA)

Event-driven backend service built as part of the **JPMorgan Chase & Co. (Forage) Midas** simulation.  
It **consumes transaction events from Kafka**, **validates and persists** records with **Spring Data JPA**, **calls an external Incentive API**, and **exposes a REST endpoint** to query user balances.

## Key Features
- **Kafka consumer** for high-volume transaction events (idempotent + validated processing)
- **Persistence layer** with **Spring Data JPA** (H2 for local/testing)
- **External Incentive API integration** via `RestTemplate`
- **Balance query REST API** (clean controller/service boundaries)
- **Test-first reliability**: integration tests + embedded Kafka test support

## Tech Stack
- **Java**, **Spring Boot**
- **Apache Kafka**
- **Spring Data JPA**, **H2**
- **Maven**, **JUnit**

## Architecture (High Level)
Kafka Topic → `TransactionListener` → `TransactionProcessor`
→ (1) validate transaction
→ (2) persist transaction + update balance
→ (3) call Incentive API
→ (4) return updated state via REST endpoint

### Modules / Key Classes
- `TransactionListener` — Kafka consumer and message ingestion
- `TransactionProcessor` — validation + persistence + incentive workflow
- `IncentiveClient` — external Incentive API client
- `BalanceController` — REST endpoint for querying balances
- `TransactionRecordRepository` — JPA repository for transaction storage

## API
### Get user balance
`GET /balance/{userId}`  
Returns current balance for a user (JSON).

> (Update the exact route if your controller uses a different path.)

## Running Locally
### Prerequisites
- Java 17+ (or your project’s configured version)
- Maven
- Kafka broker (or use your local docker-compose if you add one)

### Build + Test
```bash
./mvnw clean test

```
### Run
```bash
./mvnw spring-boot:run

```
### Verification / Test Evidence
- **TaskTwoTests** covers transaction ingestion + persistence + incentive integration behavior
- Embedded Kafka tests validate end-to-end processing

### What I Learned / Engineering Focus
- Designing event-driven workflows with clear boundaries (listener → processor → repository)
- Building production-minded integrations: input validation, idempotency, and error handling
- Writing tests that prove behavior, not just code coverage

### Credits
Simulation: JPMorgan Chase & Co. — Forage “Midas” program.
