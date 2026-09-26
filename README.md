\# AI-Assisted URL Shortener



A production-oriented URL shortening prototype built with Java 21 and Spring Boot.



The project demonstrates an engineer-led, AI-assisted software engineering workflow across three development scenarios:



\- Greenfield development

\- Brownfield enhancement

\- Ambiguous requirement clarification



The focus is not only on implementing a URL shortener, but also on demonstrating requirement decomposition, controlled change management, validation, reliability, engineering trade-offs, and responsible use of AI during development.



\---



\## Features



\- Create short URLs

\- HTTP/HTTPS URL validation

\- Base62 short-code generation

\- Redirect short URLs using HTTP 302

\- Optional URL expiration

\- HTTP 410 for expired URLs

\- Redirect analytics

\- Asynchronous analytics processing

\- Atomic analytics counter updates

\- Statistics API

\- URL creation rate limiting

\- Structured error responses

\- H2 zero-setup runtime

\- Optional PostgreSQL profile

\- Unit and integration tests



\---



\## Technology Stack



\- Java 21

\- Spring Boot 4.1.1

\- Spring Web MVC

\- Spring Data JPA

\- Jakarta Bean Validation

\- H2

\- PostgreSQL

\- Maven Wrapper

\- JUnit 5

\- Mockito



\---



\## Architecture



The application uses a modular layered architecture.



```text

Client

&#x20; |

&#x20; v

Controllers

&#x20; |

&#x20; v

Services

&#x20; |

&#x20; +--------------------+

&#x20; |                    |

&#x20; v                    v

Repository        Async Analytics

&#x20; |                    |

&#x20; +---------+----------+

&#x20;           |

&#x20;           v

&#x20;       Database

```



Primary responsibilities are separated into:



```text

controller/

&#x20;   HTTP API and request handling



service/

&#x20;   Business logic, short-code generation,

&#x20;   analytics and rate limiting



repository/

&#x20;   Persistence operations



model/

&#x20;   Persistent domain model



dto/

&#x20;   API request/response contracts



validation/

&#x20;   URL validation



exception/

&#x20;   Consistent API error handling



config/

&#x20;   Application infrastructure configuration

```



A modular monolith was selected deliberately. The current domain does not justify the operational complexity of microservices.



For additional design details, see:



`docs/architecture.md`



\---



\## API



\### Create Short URL



```http

POST /api/urls

Content-Type: application/json

```



Request:



```json

{

&#x20; "url": "https://example.com"

}

```



Example response:



```json

{

&#x20; "shortCode": "Ab12Cd3",

&#x20; "shortUrl": "http://localhost:8080/Ab12Cd3",

&#x20; "originalUrl": "https://example.com",

&#x20; "createdAt": "2026-09-26T12:00:00Z",

&#x20; "expiresAt": null

}

```



\---



\## Optional Expiration



```http

POST /api/urls

Content-Type: application/json

```



Request:



```json

{

&#x20; "url": "https://example.com",

&#x20; "expiresAt": "2026-12-31T23:59:59Z"

}

```



Expiration uses an absolute UTC timestamp.



Expired URLs return:



```text

HTTP 410 Gone

```



\---



\## Redirect



```http

GET /{shortCode}

```



Example:



```http

GET /Ab12Cd3

```



Successful response:



```text

HTTP 302 Found

Location: https://example.com

```



\---



\## Analytics



```http

GET /api/urls/{shortCode}/stats

```



Example response:



```json

{

&#x20; "shortCode": "Ab12Cd3",

&#x20; "originalUrl": "https://example.com",

&#x20; "redirectCount": 3,

&#x20; "createdAt": "2026-09-26T12:00:00Z",

&#x20; "expiresAt": null

}

```



Only successful redirects are counted.



Analytics processing is asynchronous, so statistics are eventually consistent.



\---



\## HTTP Error Behavior



| Scenario | Status |

|---|---:|

| Invalid URL | 400 Bad Request |

| Unknown short code | 404 Not Found |

| Expired short URL | 410 Gone |

| Rate limit exceeded | 429 Too Many Requests |

| Short-code generation failure | 500 Internal Server Error |



Errors use structured Spring `ProblemDetail` responses.



\---



\## Rate Limiting



URL creation is protected with a lightweight application-local fixed-window rate limiter.



Current prototype limit:



```text

20 creation requests per client per 60 seconds

```



Requests above the limit receive:



```text

HTTP 429 Too Many Requests

```



The current limiter is intentionally local to one application instance. A multi-instance production deployment should use distributed rate limiting through infrastructure such as Redis or an API gateway.



\---



\# Running the Application



\## Prerequisites



Required:



```text

Java 21

```



A global Maven installation is not required because the repository contains the Maven Wrapper.



\---



\## Windows



From the project root:



```cmd

mvnw.cmd spring-boot:run

```



The application starts on:



```text

http://localhost:8080

```



The default configuration uses an in-memory H2 database, so no external database setup is required.



\---



\## macOS / Linux



```bash

chmod +x mvnw

./mvnw spring-boot:run

```



\---



\# PostgreSQL Profile



H2 is used by default for zero-setup evaluation.



PostgreSQL is also supported as a production-oriented profile.



The profile expects:



```text

DB\_URL

DB\_USERNAME

DB\_PASSWORD

```



Default PostgreSQL URL:



```text

jdbc:postgresql://localhost:5432/urlshortener

```



Example on Windows:



```cmd

set DB\_USERNAME=postgres

set DB\_PASSWORD=your\_password

mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres

```



The PostgreSQL configuration was locally validated during development.



Credentials are not stored in source control.



\---



\# Testing



Run all automated tests:



\## Windows



```cmd

mvnw.cmd test

```



\## macOS / Linux



```bash

./mvnw test

```



Final verification:



```cmd

mvnw.cmd clean verify

```



The test suite covers areas including:



\- URL validation

\- short-code generation

\- URL creation

\- redirects

\- unknown short codes

\- expiration

\- analytics triggering

\- asynchronous analytics persistence

\- statistics retrieval

\- rate limiting

\- regression behavior



Manual HTTP validation was also performed during development.



\---



\# Engineering Scenarios



\## 1. Greenfield



The initial URL shortener was implemented from scratch.



Work was decomposed into:



```text

Requirement

&#x20;  |

&#x20;  +--> URL validation

&#x20;  +--> Short-code generation

&#x20;  +--> Persistence

&#x20;  +--> Creation API

&#x20;  +--> Redirect API

&#x20;  +--> Error handling

&#x20;  +--> Tests

```



The initial implementation was validated before additional features were introduced.



\---



\## 2. Brownfield



After the core system was working, optional URL expiration was introduced as a change to the existing application.



Impact analysis was performed before implementation.



The change affected:



\- persistence

\- API contracts

\- validation

\- redirect behavior

\- exception handling

\- tests



Backward compatibility was preserved for URLs without expiration.



See:



`docs/brownfield-expiration.md`



\---



\## 3. Ambiguous Requirement



The requirement:



> Add analytics.



was intentionally treated as ambiguous rather than immediately implemented.



Possible interpretations were identified and the requirement was normalized to:



> Track the total number of successful redirects for each short URL.



The resulting implementation includes asynchronous analytics and a statistics API.



See:



`docs/analytics-clarification.md`



\---



\# AI-Assisted Engineering



AI was used as an engineering accelerator for:



\- requirement analysis

\- task decomposition

\- design exploration

\- implementation support

\- test generation

\- debugging

\- documentation

\- trade-off analysis



AI-generated suggestions were reviewed before adoption.



The development process followed:



```text

Requirement

&#x20;   |

&#x20;   v

Decomposition

&#x20;   |

&#x20;   v

AI-Assisted Exploration

&#x20;   |

&#x20;   v

Engineer Review

&#x20;   |

&#x20;   v

Implementation

&#x20;   |

&#x20;   v

Automated + Manual Validation

&#x20;   |

&#x20;   v

Accepted Change

```



Some AI-assisted directions were modified or rejected when they did not fit the actual environment or project scope.



See:



`docs/engineering-decisions.md`



\---



\# Reliability and Scaling



The prototype includes several reliability controls:



\- database uniqueness for short codes

\- collision handling

\- URL validation

\- expiration validation

\- explicit HTTP error behavior

\- bounded asynchronous execution

\- atomic analytics increments

\- rate limiting

\- automated regression testing



For horizontal scaling, persistent data can be moved to shared PostgreSQL.



A larger deployment could evolve toward:



```text

&#x20;                 +--> Application Instance 1 --+

Client -> Gateway |                             |

&#x20;                 +--> Application Instance 2 --+--> PostgreSQL

&#x20;                        |

&#x20;                        +--> Durable Event Processing

```



Distributed rate limiting, durable event delivery, centralized observability, and production database migrations would be appropriate next steps at larger scale.



\---



\# Known Limitations



This is a focused engineering prototype.



Known limitations include:



\- H2 default storage is not durable across restarts.

\- Rate limiting is application-local rather than distributed.

\- Async analytics is best-effort rather than backed by a durable message broker.

\- Analytics is eventually consistent.

\- Authentication and user-owned URL management are not implemented.

\- Horizontal scaling is designed for but not deployed as a multi-node environment.

\- Production observability would require additional metrics, tracing, logging, and alerting.

\- Production schema evolution should use controlled database migrations rather than automatic schema updates.



These trade-offs are documented rather than hidden behind unnecessary prototype infrastructure.



\---



\# Documentation



Additional engineering documentation:



```text

docs/

├── requirements.md

├── architecture.md

├── brownfield-expiration.md

├── analytics-clarification.md

└── engineering-decisions.md

```



These documents capture requirement normalization, architecture, impact analysis, AI-assisted engineering decisions, validation strategy, trade-offs, and production evolution.



\---



\# Final Validation



Before submission, the project is validated using:



```cmd

mvnw.cmd clean verify

```



The final implementation remains engineer-owned: AI assists the engineering process, while correctness, validation, maintainability, and production-readiness decisions remain human responsibilities.

