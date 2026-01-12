 Forex Rates Service

A functional Scala service that exposes foreign exchange rates via HTTP,
backed by an external provider (One-Frame), with in-memory caching.

---

##  Architecture Overview

This project follows a clean, modular architecture:

HTTP → Program → Service → Interpreter → External API
↑
Cache

Each layer has a single responsibility and is wired using dependency injection.

---

##  Requirements Satisfied

### 1. External Rates Provider
- Implemented via `NewOneFrame`
- Calls One-Frame API using http4s client
- Token-based authentication
- No hard-coded values

### 2. HTTP Client Usage
- Uses `http4s-blaze-client`
- Proper resource management via `Resource`
- Status-based response handling

### 3. JSON Parsing
- Circe-based decoding
- DTOs separated from domain
- Explicit mapping step

### 4. Domain Mapping
- External DTO → `Rate`
- Parsing timestamps using `OffsetDateTime`

### 5. In-Memory Caching
- Implemented in `NewCachedRatesService`
- Time-based expiry
- Transparent to callers
- Cache updated only on successful fetch

### 6. Configuration
- PureConfig-based loading
- Externalized settings:
  - HTTP server
  - One-Frame base URI
  - Token
  - Cache TTL

### 7. Modularity
- No HTTP logic in services
- No business logic in Module


---
