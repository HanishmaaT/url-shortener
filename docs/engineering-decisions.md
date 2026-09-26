\# Engineering Decisions and AI-Assisted Development



\## 1. Purpose



This document records the major engineering decisions, alternatives considered,

trade-offs, validation steps, and AI-assisted development practices used while

building the URL Shortener.



AI was used as an engineering accelerator for requirement analysis, design

exploration, implementation support, debugging, testing, and documentation.



All generated suggestions were reviewed before adoption. Final responsibility

for architecture, implementation, validation, and production-readiness decisions

remained with the engineer.



\---



\## 2. Architecture Decision



\### Decision



The application is implemented as a modular Spring Boot application using:



\- Java 21

\- Spring Boot

\- Spring Web MVC

\- Spring Data JPA

\- Bean Validation

\- H2

\- PostgreSQL

\- Maven

\- JUnit 5

\- Mockito



\### Why a Modular Monolith?



A microservice architecture was considered unnecessary for the current scope.



The URL shortening, redirect, expiration, analytics, and rate-limiting

capabilities are closely related and can be implemented cleanly within one

deployable application.



This keeps the prototype:



\- easy to understand

\- easy to test

\- easy to run

\- operationally simple



The internal separation between controllers, services, repositories,

validation, configuration, and persistence still allows components to evolve

independently if future requirements justify it.



\---



\## 3. Database Strategy



\### Decision



H2 is the default runtime database.



PostgreSQL is supported through a separate application profile.



\### Reason



The default H2 configuration allows an evaluator to run the project without

installing or configuring external infrastructure.



PostgreSQL provides a more production-oriented relational database option.



The application uses JPA so business logic remains largely independent of the

selected relational database.



\### Validation



The application was locally validated with PostgreSQL 17 before H2 was selected

as the zero-setup default.



\### Trade-off



H2 data is stored in memory and is lost when the application stops.



A production deployment should use a persistent database such as PostgreSQL.



\---



\## 4. Short-Code Generation



\### Decision



Short URLs use randomly generated Base62 identifiers.



The alphabet contains:



\- 0-9

\- A-Z

\- a-z



A seven-character Base62 identifier provides a large identifier space while

keeping URLs compact.



\### Collision Handling



The database enforces uniqueness on the short-code column.



The application also checks for an existing short code and retries generation

when necessary.



Database uniqueness remains the final integrity boundary.



\---



\## 5. Greenfield Scenario



\### Requirement



Build the initial URL shortening capability from scratch.



\### Decomposition



The work was divided into:



1\. URL validation

2\. short-code generation

3\. persistence

4\. URL creation API

5\. redirect API

6\. exception handling

7\. unit testing

8\. integration testing



\### Result



The initial implementation supports:



\- URL creation

\- HTTP/HTTPS validation

\- short-code generation

\- persistence

\- HTTP redirect

\- HTTP 400 for invalid URLs

\- HTTP 404 for unknown short codes



\### Validation



The implementation was validated incrementally with automated tests and manual

HTTP requests before additional features were introduced.



\---



\## 6. Brownfield Scenario - URL Expiration



\### Change Request



Add optional expiration to the existing URL shortener.



\### Impact Analysis



Before implementation, the existing code was reviewed to identify affected

areas:



\- URL persistence model

\- creation request

\- creation response

\- service validation

\- redirect behavior

\- exception handling

\- unit tests

\- integration tests



\### Decision



Expiration is represented as an absolute `Instant`.



This avoids coupling stored expiration values to a server-local timezone.



\### Backward Compatibility



Expiration is optional.



Existing URLs without an expiration timestamp continue to behave exactly as

before.



\### Behavior



\- Future expiration -> accepted

\- Missing expiration -> URL remains active

\- Past expiration during creation -> HTTP 400

\- Expired short URL -> HTTP 410 Gone

\- Unknown short URL -> HTTP 404



\### Validation



Existing tests were rerun after the change to verify that the original URL

creation and redirect behavior remained functional.



\---



\## 7. Ambiguous Scenario - Analytics



\### Original Requirement



"Add analytics."



\### Ambiguity



The requirement could reasonably refer to:



\- total redirects

\- unique visitors

\- geographic analytics

\- browser analytics

\- device analytics

\- referrer tracking

\- time-series reporting

\- failed redirect attempts



Implementing all possible interpretations would introduce unnecessary scope and

data collection.



\### Normalized Requirement



For this prototype, analytics means:



> Track the total number of successful redirects for each short URL.



Statistics are exposed through:



&#x20;   GET /api/urls/{shortCode}/stats



\### Counting Rules



\- Successful active redirects are counted.

\- Unknown short codes are not counted.

\- Expired URLs are not counted.

\- Reading the statistics endpoint does not increment the counter.



\### Privacy Decision



The prototype intentionally does not persist:



\- visitor IP history

\- geographic information

\- browser fingerprints

\- device information

\- per-user tracking



This keeps analytics focused on the stated engineering requirement.



\---



\## 8. Asynchronous Analytics



\### Decision



Redirect analytics is processed asynchronously using a bounded Spring task

executor.



\### Reason



Redirect latency is more important than immediately consistent analytics.



The redirect request can return without waiting for the analytics database

operation to complete.



\### Consistency Model



Analytics is eventually consistent.



Immediately after a redirect, the statistics endpoint may briefly return the

previous count.



\### Executor Controls



The executor uses:



\- bounded worker threads

\- bounded queue capacity

\- controlled behavior when the queue is saturated



This prevents uncontrolled creation of background tasks.



\### Failure Isolation



Analytics is considered secondary to the redirect operation.



Analytics saturation should not cause an otherwise valid redirect to fail.



\### Production Evolution



For a larger production system, the in-process analytics mechanism could be

replaced with durable event processing.



Example:



&#x20;   Redirect Request

&#x20;         |

&#x20;         v

&#x20;   Publish Redirect Event

&#x20;         |

&#x20;         +------> Return HTTP 302

&#x20;         |

&#x20;         v

&#x20;   Analytics Consumer

&#x20;         |

&#x20;         v

&#x20;   Analytics Store



A durable event broker was intentionally not introduced into this prototype

because it would add operational complexity beyond the current requirement.



\---



\## 9. Concurrent Analytics Updates



\### Risk



A normal read-modify-write sequence can lose increments when multiple redirects

are processed concurrently.



Example:



&#x20;   Request A reads count = 5

&#x20;   Request B reads count = 5

&#x20;   Request A writes 6

&#x20;   Request B writes 6



The expected result is 7, but the stored result becomes 6.



\### Decision



The repository performs an atomic database update:



&#x20;   click\_count = click\_count + 1



This reduces the risk of lost analytics updates during concurrent redirects.



\---



\## 10. Rate Limiting



\### Decision



URL creation is protected using a lightweight fixed-window rate limiter.



Current prototype limit:



&#x20;   20 URL creation requests per client per 60 seconds



Requests exceeding the limit receive:



&#x20;   HTTP 429 Too Many Requests



\### Why URL Creation?



Creation is a write operation and is more appropriate to protect from excessive

or abusive requests in this prototype.



Redirect remains a lightweight read-oriented path.



\### Current Limitation



Rate-limit counters are maintained in application memory.



This works for a single application instance but is not a distributed rate

limiting solution.



\### Production Evolution



For multiple application instances, rate limiting should be moved to shared

infrastructure such as:



\- Redis

\- API Gateway

\- dedicated distributed rate-limiting infrastructure



\---



\## 11. Horizontal Scaling



The application is designed so persistent URL information can be stored in

shared PostgreSQL.



Application instances can therefore remain largely stateless.



A production-oriented architecture could use:



&#x20;   Client

&#x20;     |

&#x20;     v

&#x20;   Load Balancer / API Gateway

&#x20;     |

&#x20;     +-------------------+

&#x20;     |                   |

&#x20;     v                   v

&#x20;   App Instance 1      App Instance 2

&#x20;     |                   |

&#x20;     +---------+---------+

&#x20;               |

&#x20;               v

&#x20;        Shared PostgreSQL



Components requiring additional work for true multi-instance deployment are:



\- application-local rate limiting

\- in-process asynchronous analytics



These could evolve to distributed rate limiting and durable event processing.



Horizontal scaling is therefore designed for but not artificially simulated in

the prototype.



\---



\## 12. Reliability and Failure Handling



The implementation explicitly handles several failure scenarios.



\### Invalid URL



Returns:



&#x20;   HTTP 400 Bad Request



\### Unknown Short Code



Returns:



&#x20;   HTTP 404 Not Found



\### Expired URL



Returns:



&#x20;   HTTP 410 Gone



\### Rate Limit Exceeded



Returns:



&#x20;   HTTP 429 Too Many Requests



\### Short-Code Collision



A new code can be generated rather than intentionally overwriting an existing

mapping.



Database uniqueness provides the final integrity constraint.



\### Analytics Failure



Analytics is isolated from the primary redirect behavior where practical.



\---



\## 13. AI-Assisted Engineering



AI assistance was used during multiple stages of development.



\### Requirement Analysis



AI helped identify:



\- functional requirements

\- ambiguous areas

\- failure scenarios

\- validation requirements

\- architecture alternatives



The engineer reviewed and normalized these into explicit acceptance criteria.



\### Implementation



AI assisted with:



\- implementation alternatives

\- Spring Boot patterns

\- test generation

\- debugging

\- refactoring suggestions

\- documentation structure



Generated code was not treated as automatically correct.



Changes were compiled, tested, reviewed, and refined before acceptance.



\### Iterative Refinement



Development followed an incremental workflow:



&#x20;   Requirement

&#x20;       |

&#x20;       v

&#x20;   Decomposition

&#x20;       |

&#x20;       v

&#x20;   AI-Assisted Exploration

&#x20;       |

&#x20;       v

&#x20;   Engineer Review

&#x20;       |

&#x20;       v

&#x20;   Implementation

&#x20;       |

&#x20;       v

&#x20;   Automated Validation

&#x20;       |

&#x20;       v

&#x20;   Manual Validation

&#x20;       |

&#x20;       v

&#x20;   Git Commit



This preserved human control while using AI to accelerate individual engineering

tasks.



\---



\## 14. Example of a Modified AI-Assisted Direction



An initial infrastructure direction included a containerized database setup.



The local development environment did not provide the required virtualization

support.



Rather than spending prototype time modifying machine-level virtualization

configuration, the approach was revised.



The engineer:



1\. installed and validated PostgreSQL locally

2\. retained PostgreSQL as an optional application profile

3\. selected H2 as the default zero-setup database



This demonstrates that AI-assisted suggestions were evaluated against actual

environment constraints rather than followed automatically.



\---



\## 15. Alternatives Considered



\### Microservices



Not selected.



Reason:



The prototype does not contain enough independently evolving business domains to

justify distributed-system complexity.



\### Kafka or Another Message Broker



Not selected for the prototype.



Reason:



A bounded asynchronous executor demonstrates asynchronous analytics without

requiring additional infrastructure.



A durable broker remains a reasonable production evolution.



\### Mandatory PostgreSQL



Not selected as the default.



Reason:



It would increase setup requirements for an evaluator.



PostgreSQL support remains available through an application profile.



\### Mandatory Redis



Not selected.



Reason:



The current prototype does not require distributed caching or distributed rate

limiting.



Redis becomes relevant when running multiple application instances.



\---



\## 16. Testing Strategy



The project uses multiple levels of validation.



\### Unit Tests



Used for isolated behavior such as:



\- URL validation

\- short-code generation

\- URL service behavior

\- expiration rules

\- analytics triggering

\- rate limiting



\### Integration Tests



Used to verify behavior across Spring components and persistence.



Examples include:



\- URL creation

\- redirect behavior

\- invalid URL handling

\- unknown short code

\- expired URL behavior

\- asynchronous analytics



\### Manual API Validation



HTTP requests were also executed manually to verify the application from the

client perspective.



\### Regression Testing



The full test suite was rerun after major feature changes.



Final validation is performed using:



&#x20;   mvnw.cmd clean verify



\---



\## 17. Secure Development Considerations



No database passwords or credentials are intentionally committed to source

control.



The PostgreSQL profile obtains sensitive configuration from environment

variables.



Internal source material and assessment documents are not included in the

public repository.



Only implementation artifacts and derived engineering documentation are

included.



The analytics implementation intentionally avoids collecting unnecessary

visitor information.



\---



\## 18. Known Limitations



The prototype intentionally accepts several limitations:



\- H2 is in-memory and not durable across application restarts.

\- The local rate limiter is not distributed.

\- Asynchronous analytics is best-effort and not backed by a durable event queue.

\- Analytics is eventually consistent.

\- The prototype does not implement authentication or user-owned URLs.

\- Horizontal scaling is represented through architecture and design decisions,

&#x20; not a deployed multi-node environment.

\- Production observability would require additional metrics, tracing, alerting,

&#x20; and centralized logging.

\- Production database schema changes should use a controlled migration tool

&#x20; rather than relying on automatic schema updates.



These limitations are deliberate trade-offs for a focused prototype rather than

unrecognized production gaps.



\---



\## 19. Engineering Ownership



AI accelerated analysis, implementation, debugging, testing, and documentation,

but did not replace engineering judgment.



The final solution reflects explicit decisions about:



\- scope

\- architecture

\- reliability

\- privacy

\- scalability

\- testing

\- operational complexity

\- failure behavior



Every accepted change remained subject to engineer review and validation.

