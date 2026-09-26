\# URL Shortener - Architecture Overview



\## 1. Architecture Goal



The goal is to build a reliable, maintainable, testable, and scalable

URL shortener while keeping the system understandable and easy to run.



The application uses a layered modular-monolith architecture.



The design intentionally introduces infrastructure only where there is

a clear engineering reason for it.



\---



\## 2. High-Level Architecture



&#x20;                   Client

&#x20;                     |

&#x20;                     | HTTP

&#x20;                     v

&#x20;             Spring Boot API

&#x20;                     |

&#x20;         +-----------+-----------+

&#x20;         |                       |

&#x20;         v                       v

&#x20;  URL Controller         Redirect Controller

&#x20;         |                       |

&#x20;         +-----------+-----------+

&#x20;                     |

&#x20;                     v

&#x20;                Service Layer

&#x20;                     |

&#x20;            +--------+--------+

&#x20;            |                 |

&#x20;            v                 v

&#x20;         Redis            PostgreSQL

&#x20;         Cache             Database

&#x20;            |                 |

&#x20;            +--------+--------+

&#x20;                     |

&#x20;                     v

&#x20;                 Analytics



\### Controller Layer



Responsible for:



\- Receiving HTTP requests

\- Request validation

\- Returning appropriate HTTP responses

\- Redirecting short URLs to original URLs



Business logic is kept outside controllers.



\### Service Layer



Responsible for:



\- URL shortening logic

\- Generating unique short codes

\- URL expiration checks

\- Redirect processing

\- Cache interaction

\- Click analytics

\- Business validation



\### Repository Layer



Responsible for:



\- Persisting URL mappings

\- Looking up URLs by short code

\- Checking short-code uniqueness

\- Updating URL information



Spring Data JPA is used to separate persistence concerns from

business logic.



\---



\## 3. Persistence Strategy



\### PostgreSQL



PostgreSQL is used as the primary persistent database.



It stores:



\- Original URL

\- Short code

\- Creation timestamp

\- Optional expiration timestamp

\- Click count



PostgreSQL was selected instead of relying only on an in-memory

database because URL mappings must survive application restarts in

a realistic deployment.



\### H2



H2 may be used in automated tests where an isolated lightweight

database improves test execution.



\### Database Indexing



The short code is used for almost every redirect lookup.



A unique index/constraint will therefore be maintained on the short

code to:



\- Enforce uniqueness

\- Prevent duplicate mappings

\- Improve redirect lookup performance



\---



\## 4. Caching Strategy



Redis is used to cache frequently accessed URL mappings.



URL-shortening systems are typically read-heavy: a URL may be created

once but redirected many times.



Redirect lookup flow:



Request

&#x20; |

&#x20; v

Check Redis

&#x20; |

&#x20; +---- Cache Hit ----> Use cached mapping

&#x20; |

&#x20; +---- Cache Miss

&#x20;            |

&#x20;            v

&#x20;       PostgreSQL

&#x20;            |

&#x20;            v

&#x20;       Store in Redis

&#x20;            |

&#x20;            v

&#x20;         Redirect



This reduces repeated database reads for frequently accessed short

URLs.



PostgreSQL remains the source of truth.



The application must not rely on Redis as permanent storage.



\---



\## 5. Main Request Flows



\### Create Short URL



Client

&#x20; |

&#x20; | POST /api/urls

&#x20; v

Controller

&#x20; |

&#x20; | Validate request

&#x20; v

Service

&#x20; |

&#x20; | Generate short code

&#x20; | Check uniqueness

&#x20; v

Repository

&#x20; |

&#x20; v

PostgreSQL

&#x20; |

&#x20; v

Return created short URL



\### Redirect Short URL



Client

&#x20; |

&#x20; | GET /{shortCode}

&#x20; v

Redirect Controller

&#x20; |

&#x20; v

Service

&#x20; |

&#x20; v

Check Redis

&#x20; |

&#x20; +---- Hit -------------------+

&#x20; |                            |

&#x20; +---- Miss --> PostgreSQL ---+

&#x20;                              |

&#x20;                              v

&#x20;                    Check expiration

&#x20;                              |

&#x20;                              v

&#x20;                   Update click analytics

&#x20;                              |

&#x20;                              v

&#x20;                   Redirect to original URL



\---



\## 6. Technology Decisions



\### Java 21



Java 21 is an LTS release and is available in the development

environment.



\### Spring Boot



Spring Boot provides:



\- REST API support

\- Dependency injection

\- Request validation

\- Exception handling

\- Database integration

\- Caching integration

\- Testing support



\### Spring Data JPA



Spring Data JPA provides a clean abstraction for persistence and

reduces repetitive database-access code.



\### PostgreSQL



PostgreSQL provides durable relational persistence and supports

constraints and indexing required by the service.



\### Redis



Redis is used for frequently accessed URL mappings to reduce database

reads on the redirect path.



\### Maven Wrapper



The Maven Wrapper allows the project to be built consistently without

requiring Maven to be installed globally.



\### Docker Compose



Docker Compose will be used to provide reproducible local PostgreSQL

and Redis infrastructure.



\### JUnit and Mockito



JUnit, Mockito, and Spring Boot testing support will be used for unit

and integration testing.



\---



\## 7. Reliability Strategy



The application explicitly handles expected failure scenarios.



\### Invalid URL



Only supported HTTP and HTTPS destination URLs are accepted.



Result:



HTTP 400 Bad Request



\### Unknown Short Code



If a short code does not exist:



HTTP 404 Not Found



\### Expired URL



If the URL exists but has expired:



HTTP 410 Gone



\### Short-Code Collision



Generated short codes must be unique.



The database enforces uniqueness, and the application will retry

generation when a collision is detected.



\### Unexpected Failure



Unexpected application failures return a controlled error response

without exposing internal implementation details.



\### Cache Failure



PostgreSQL remains the source of truth.



The architecture should allow redirect lookup to continue using the

database when cached data is unavailable rather than treating Redis

as permanent storage.



\---



\## 8. Rate Limiting



Public URL creation endpoints can be abused through excessive

requests.



Basic rate limiting will therefore be added as a reliability and

abuse-protection mechanism.



The implementation will remain intentionally simple for the

prototype.



In a horizontally scaled production deployment, rate-limit state

would need to be coordinated across application instances.



\---



\## 9. Security Considerations



The prototype will:



\- Accept only HTTP and HTTPS destination URLs

\- Validate incoming request data

\- Avoid exposing stack traces or internal exception details

\- Avoid hard-coded credentials

\- Keep secrets outside source control

\- Apply basic rate limiting

\- Validate short-code input



Authentication and authorization are outside the current prototype

scope.



Additional protections such as malicious URL reputation checking

would require external security services and are outside the current

scope.



\---



\## 10. Scalability Strategy



The prototype implements selected scalability and reliability

features where they directly support the URL-shortening use case:



\- PostgreSQL for durable URL storage

\- Database indexing for short-code lookup

\- Redis caching for frequently accessed URL mappings

\- Rate limiting for public endpoints

\- Docker Compose for reproducible local infrastructure



Potential future production improvements include:



\- Asynchronous event-driven analytics

\- Database replication

\- Horizontal application scaling

\- Distributed rate limiting

\- Observability and centralized metrics

\- Multi-region deployment



These are documented rather than implemented because they require

deployment-scale requirements and infrastructure beyond the scope

of this prototype.



\---



\## 11. Why a Modular Monolith?



The application is intentionally implemented as one deployable

Spring Boot service rather than multiple microservices.



The current functional scope does not justify the deployment,

networking, monitoring, and consistency complexity introduced by

multiple services.



Responsibilities are still separated through controller, service,

repository, caching, and validation boundaries.



This keeps the code maintainable and leaves room for future

decomposition if scale or organizational requirements justify it.



\---



\## 12. Analytics Design



The initial analytics requirement is interpreted as:



"Track the total number of successful redirects for each shortened

URL."



The redirect path should remain lightweight.



For the prototype, click-count analytics can be maintained within the

application.



At substantially higher traffic volumes, redirect events could be

published asynchronously and processed separately to avoid adding

analytics-processing latency to redirects.



A message broker is intentionally not introduced until such scale

requirements exist.



\---



\## 13. Engineering Trade-offs



\### PostgreSQL vs H2



PostgreSQL increases setup complexity but provides realistic durable

persistence.



H2 remains useful for lightweight automated testing.



\### Redis



Redis adds another infrastructure dependency but demonstrates a

realistic optimization for the read-heavy redirect path.



The database remains the source of truth to avoid coupling correctness

to cache availability.



\### Kafka / Message Broker



Not implemented.



A message broker would be useful for large-scale asynchronous

analytics, but introducing one solely for incrementing a click counter

would add unnecessary operational complexity to this prototype.



\### Microservices



Not implemented.



A modular monolith provides sufficient separation for the current

scope with substantially lower operational complexity.



\---



\## 14. AI-Assisted Engineering Approach



AI assistance may be used for:



\- Requirement analysis

\- Implementation suggestions

\- Test generation

\- Debugging

\- Refactoring

\- Security review

\- Documentation

\- Review preparation



AI-generated output is not automatically accepted.



For significant changes, the workflow is:



Requirement

&#x20; |

&#x20; v

Task definition and acceptance criteria

&#x20; |

&#x20; v

AI-assisted implementation/suggestion

&#x20; |

&#x20; v

Engineer review

&#x20; |

&#x20; +---- Accept

&#x20; |

&#x20; +---- Edit

&#x20; |

&#x20; +---- Reject

&#x20; |

&#x20; v

Automated/manual validation

&#x20; |

&#x20; v

Engineer approval



Important AI-assisted decisions will be recorded with the reasoning

behind accepted, modified, or rejected suggestions.



The engineer remains responsible for correctness, security,

maintainability, and production readiness.

