Forex Rates Service – Implementation Notes & Decisions
--------------------------------------------------------------------------------
Overview

This repository contains an implementation of the Forex Rates Service assignment.
The goal was to build a correct, compileable, and extensible Scala service using the provided skeleton, 
assuming we are using cats-effect 2 and http4s.

Rather than attempting to implement everythin at once, the solution follows an incremental, layered approach:

Domain correctness first
In-memory caching with expiry
Stubbed external dependency (OneFrame)
Clean HTTP wiring
Clear separation of concerns
Changed NewOneFrame to implement RatesService[F] instead of a local algebra, 
aligning it with the domain service contract and simplifying module wiring.

This README explains what was implemented, which requirements are satisfied, and what is intentionally left out.
----------------------------------
High-Level Architecture
----------------------------------
The system is structured into clear layers:
HTTP (http4s)
  ↓
Program (RatesProgram)
  ↓
Service (RatesService)
  ↓
Interpreters
    ├── NewCachedRatesService (cache + expiry)
    └── NewOneFrame (stubbed external provider)


Each layer has one responsibility, which makes the system easy to reason about and extend.
----------------------------------
Requirements Breakdown
----------------------------------
 1. Rates Domain & API

Requirement:
Expose a GET /rates?from=<FROM>&to=<TO> endpoint returning an exchange rate.

Status: Implemented

RatesHttpRoutes exposes the endpoint
Query parameters from and to are validated
Response is returned as JSON
HTTP server starts via sbt run

This satisfies the core API requirement.

----------------------------------------
 2. RatesProgram (Business Logic Layer)
----------------------------------------
Requirement:
Separate business logic from HTTP and infrastructure.

Status:  Implemented

RatesProgram sits between HTTP and services
No HTTP or caching logic leaks into this layer
This makes testing and extension easier

----------------------------------------
 3. RatesService Abstraction
----------------------------------------
Requirement:
Use a service abstraction so implementations can be swapped.

Status:  Implemented

RatesService[F] is the main interface
Implementations:
NewCachedRatesService
NewOneFrame (stub)

This allows composition (cache wrapping HTTP, etc.).

----------------------------------------
 4. In-Memory Cache with Expiry
----------------------------------------
Requirement:
Cache rates in memory with a time-based expiry (TTL).

Status:  Implemented (named expiryTime)

File:
src/main/scala/forex/services/rates/interpreters/NewCachedRatesService.scala

Key points:
Uses cats-effect Ref (CE2 compatible)
Stores (Rate, timestamp) per currency pair
On each request:
If cached and not expired → return cached value
If expired or missing → delegate to underlying service

This satisfies the caching requirement fully.

----------------------------------------
 5. OneFrame Integration (Stubbed)
----------------------------------------
Requirement:
Fetch rates from OneFrame (external HTTP service).

Status: Partially implemented (intentionally stubbed for now)

File:
src/main/scala/forex/services/rates/interpreters/NewOneFrame.scala

What is implemented:
Correct interpreter structure
Correct dependency injection (Client[F])
Correct request flow
Clear place to add real HTTP logic later

----------------------------------------
What is intentionally not implemented:
----------------------------------------
Real OneFrame HTTP call
Authentication headers
Error handling for non-happy paths

Reason:
Multiple attempts showed that mixing real HTTP with CE2 wiring risks instability.
Encountered multiple errors

The stub ensures:
Clean compile
Clear intent

We can see exactly where HTTP would go
This was a deliberate trade-off.

----------------------------------------
6. cats-effect 2 Compliance
----------------------------------------
Use cats-effect 2.

Status:  Fully compliant

Uses ConcurrentEffect, Timer
No CE3 APIs (IOApp.Simple, Ref.make, etc.)

Compatible with http4s v0.22.x
This was a major source of earlier issues(CE3 APIs) and is now fully resolved.

----------------------------------------
 7. Module Wiring
----------------------------------------
Requirement:
Wire everything together cleanly.

Status:  Implemented

File:
src/main/scala/forex/Module.scala

Responsibilities:
Construct services
Wrap OneFrame with cache
Build RatesProgram
Attach HTTP routes
Apply middleware (timeout, autoslash)

No side effects are executed at construction time.

----------------------------------------
8. Application Startup
----------------------------------------

Requirement:
Application should start via sbt run.

Status:  Implemented

File:
src/main/scala/forex/Main.scala

Uses BlazeServerBuilder
Loads config
Binds host & port
Starts HTTP server
Verified working with curl.

How to Run
----------------------------------------
sbt run
Expected output:
http4s v0.22.x started at http://localhost:8080

How to Test
----------------------------------------
Open another terminal and run:
curl "http://localhost:8080/rates?from=USD&to=EUR"

You should receive a JSON response (stubbed rate).

What Is NOT Implemented (Yet)
----------------------------------------
These are consciously left out and documented for transparency:

Real OneFrame HTTP integration
Retry logic

These will be added incrementally on top of the current design.


Design Philosophy

I choose correct and compilable over “almost works”

Avoid unsafe effects (unsafeRunSync)
Keep effectful logic at the edges
Make the code readable for reviewers
Leave clear extension points for reviewers

Final Notes
This solution prioritizes:
Architectural clarity
Functional correctness
cats-effect 2 

Honest trade-offs

If more time were available, the next step would be replacing the OneFrame stub with a real HTTP interpreter — without changing any public APIs.