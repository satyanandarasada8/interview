# Forex Rates Service - Interview Task

## Overview

This project implements a simplified Forex rates service with the following structure:

- **Module**: wires together services, programs, and HTTP routes
- **RatesProgram**: orchestrates service calls
- **RatesService**: provides Forex rate data
- **HTTP Layer**: exposes a REST API for rate queries

## Decisions & Strategies

1. **OneFrame Service Stub**
   - `NewOneFrame.scala` implements a stub for the OneFrame API.
   - **Reasoning**: Avoid HTTP calls during the coding exercise, satisfy the Algebra, and compile cleanly.
   - **Behavior**: Returns dummy rates (`Price = 1.0`) immediately.
   - **Requirements satisfied**:  
     - Service layer abstraction  
     - Allows wiring in `Module.scala`  
     - Compilation success

2. **Cached Rates Service with Expiry Time**
   - `NewCachedRatesService.scala` implements an in-memory cache.
   - **Decisions**:  
     - Renamed TTL → `expiryTime` for clarity  
     - No HTTP requests in this layer (pure caching)  
     - Uses `cats.effect.concurrent.Ref` for thread-safe state
   - **Requirements satisfied**:  
     - Service caching layer  
     - Expiry logic implemented  
     - Clean compilation

3. **Module Wiring**
   - `Module.scala` wires:  
     - `NewOneFrame` → `NewCachedRatesService` → `RatesProgram` → `RatesHttpRoutes`  
   - **Middlewares**:  
     - `AutoSlash` and `Timeout` for HTTP routes  
   - **Requirements satisfied**:  
     - Layering: Service → Program → HTTP  
     - Future-ready structure

4. **Notes on Compilation & Dependencies**
   - Scala 2.13.12  
   - `cats-effect`, `http4s`, `circe`, `pureconfig`  
   - Stubbed services to ensure the project **compiles cleanly** without external HTTP calls.
