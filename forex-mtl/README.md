# Forex Rates Service - Interview Task

## Overview

This project implements a simplified Forex rates service, designed to be layered, testable, and compile cleanly without external HTTP dependencies.  

The main layers are:

- **Module**: wires services, programs, and HTTP routes together.
- **RatesProgram**: orchestrates calls to services.
- **RatesService**: provides Forex rate data (stubbed or cached).
- **HTTP Layer**: exposes REST API endpoints for rate queries.

---

## Design Decisions & Strategies

### 1. OneFrame Service Stub

- File: `src/main/scala/forex/services/rates/interpreters/NewOneFrame.scala`
- **Purpose**: Stub implementation of OneFrame API to satisfy the Algebra, compile cleanly, and allow wiring in `Module.scala`.
- **Behavior**:
  - Returns dummy rate (`Price = 1.0`) immediately.
  - No HTTP calls are made.
- **Requirements satisfied**:
  - Service layer abstraction.
  - Clean compilation.
  - Layer wiring ready for later real API integration.

### 2. Cached Rates Service with Expiry Time

- File: `src/main/scala/forex/services/rates/interpreters/NewCachedRatesService.scala`
- **Purpose**: Adds an in-memory cache layer on top of `NewOneFrame`.
- **Decisions**:
  - Renamed TTL → `expiryTime` for clarity (friendly for beginners).
  - Cache is implemented using `cats.effect.concurrent.Ref` for thread-safe mutable state.
  - No HTTP calls in this layer (pure caching logic).
- **Behavior**:
  - Stores rates per currency pair along with the time of caching.
  - Checks if the cached entry is still fresh according to `expiryTime`.
  - Passes through to underlying service if expired or missing.
- **Requirements satisfied**:
  - Implements caching layer with expiry.
  - Keeps service wiring consistent.
  - Ready for future integration with real HTTP service.

### 3. Module Wiring

- File: `src/main/scala/forex/Module.scala`
- **Purpose**: Wire all layers together and expose HTTP routes.
- **Wiring**:
# Forex Rates Service - Interview Task

## Overview

This project implements a simplified Forex rates service, designed to be layered, testable, and compile cleanly without external HTTP dependencies.  

The main layers are:

- **Module**: wires services, programs, and HTTP routes together.
- **RatesProgram**: orchestrates calls to services.
- **RatesService**: provides Forex rate data (stubbed or cached).
- **HTTP Layer**: exposes REST API endpoints for rate queries.

---

## Design Decisions & Strategies

### 1. OneFrame Service Stub

- File: `src/main/scala/forex/services/rates/interpreters/NewOneFrame.scala`
- **Purpose**: Stub implementation of OneFrame API to satisfy the Algebra, compile cleanly, and allow wiring in `Module.scala`.
- **Behavior**:
  - Returns dummy rate (`Price = 1.0`) immediately.
  - No HTTP calls are made.
- **Requirements satisfied**:
  - Service layer abstraction.
  - Clean compilation.
  - Layer wiring ready for later real API integration.

### 2. Cached Rates Service with Expiry Time

- File: `src/main/scala/forex/services/rates/interpreters/NewCachedRatesService.scala`
- **Purpose**: Adds an in-memory cache layer on top of `NewOneFrame`.
- **Decisions**:
  - Renamed TTL → `expiryTime` for clarity (friendly for beginners).
  - Cache is implemented using `cats.effect.concurrent.Ref` for thread-safe mutable state.
  - No HTTP calls in this layer (pure caching logic).
- **Behavior**:
  - Stores rates per currency pair along with the time of caching.
  - Checks if the cached entry is still fresh according to `expiryTime`.
  - Passes through to underlying service if expired or missing.
- **Requirements satisfied**:
  - Implements caching layer with expiry.
  - Keeps service wiring consistent.
  - Ready for future integration with real HTTP service.

### 3. Module Wiring

- File: `src/main/scala/forex/Module.scala`
- **Purpose**: Wire all layers together and expose HTTP routes.
- **Wiring**:
NewOneFrame (stub) → NewCachedRatesService → RatesProgram → RatesHttpRoutes → HttpApp

- **Middlewares**:
- `AutoSlash` – removes trailing slashes.
- `Timeout` – applies HTTP request timeout from config.
- **Requirements satisfied**:
- Full layering: service → program → HTTP.
- Stubbed service allows compilation and testing without external dependencies.

### 4. Compilation & Dependencies

- Scala version: 2.13.12
- Key libraries:
- `cats-effect` for effectful programming
- `http4s` for HTTP routes
- `circe` for JSON serialization
- `pureconfig` for configuration parsing
- Stubbed services ensure the project compiles cleanly, even without internet access or API keys.

---

## How to Run

1. Clone the repository:

```bash
git clone https://github.com/satyanandarasada8/interview.git
cd interview
2. COMPILE
sbt compile

3.RUN THE PROJECT
sbt run

HTTP API (if Main wiring is present) will be available at:
http://localhost:<port>

Port is defined in ApplicationConfig

--------------------------------
Future Improvements
--------------------------------
Replace NewOneFrame stub with actual HTTP integration.
Enhance NewCachedRatesService with persistent cache (e.g., Redis).
Add logging, metrics, and error handling.
Unit tests for caching and service layers.