\# URL Shortener - Architecture



\## 1. Architecture Overview



The URL shortener is implemented as a layered Spring Boot modular monolith.



The architecture intentionally avoids unnecessary distributed-system

complexity while keeping clear boundaries that support future evolution.



Client

&#x20; |

&#x20; v

Rate Limiting

&#x20; |

&#x20; v

REST Controllers

&#x20; |

&#x20; v

Service Layer

&#x20; |

&#x20; +--------------------+

&#x20; |                    |

&#x20; v                    v

JPA Repository     Async Analytics

&#x20; |

&#x20; v

Database



\---



\## 2. Technology Stack



\- Java 21

\- Spring Boot

\- Spring Web MVC

\- Spring Data JPA

\- Jakarta Validation

\- H2

\- PostgreSQL

\- Maven

\- JUnit 5

\- Mockito

\- Git / GitHub



\---



\## 3. Application Layers



\### Controller Layer



Responsible for:



\- HTTP request/response handling

\- Request validation

\- HTTP status codes

\- Redirect responses



\### Service Layer



Responsible for:



\- URL creation

\- Short-code generation coordination

\- Expiration rules

\- Redirect resolution

\- Analytics coordination



\### Repository Layer



Responsible for:



\- Persistence

\- Short-code lookup

\- Uniqueness checks

\- Analytics persistence operations



\### Validation Layer



Responsible for validating user-provided URLs and rejecting unsupported or

malformed values.



\---



\## 4. Persistence Architecture



The default application profile uses an in-memory H2 database.



This provides a zero-setup evaluator experience:



&#x20;   clone -> build -> run



An optional PostgreSQL profile provides a production-oriented persistence

configuration.



Application

&#x20;   |

Spring Data JPA

&#x20;   |

&#x20;   +---- H2 (default)

&#x20;   |

&#x20;   +---- PostgreSQL (optional profile)



PostgreSQL was selected as the production-oriented database because URL

mappings require durable shared persistence and indexed lookup by short code.



\---



\## 5. Short-Code Strategy



Short codes are generated using a Base62-style character set.



The database also enforces uniqueness.



Collision handling is performed by retrying generation rather than overwriting

an existing mapping.



This provides defense at both the application and persistence layers.



\---



\## 6. Redirect Flow



GET /{shortCode}

&#x20;      |

&#x20;      v

Resolve mapping

&#x20;      |

&#x20;      +---- not found ----> HTTP 404

&#x20;      |

&#x20;      v

Check expiration

&#x20;      |

&#x20;      +---- expired ------> HTTP 410

&#x20;      |

&#x20;      v

Trigger analytics update

&#x20;      |

&#x20;      v

HTTP redirect



The redirect path is intentionally kept small because redirect latency is a

primary performance concern.



\---



\## 7. Asynchronous Analytics



Analytics is intentionally separated from the main redirect response path.



For this prototype, analytics work can be executed using a bounded Spring

asynchronous executor.



Redirect

&#x20;  |

&#x20;  +----> Return HTTP redirect

&#x20;  |

&#x20;  +----> Async analytics task



This reduces unnecessary work on the request thread.



For high-volume production traffic, an in-process executor has limitations.

A durable event broker such as Kafka could replace this mechanism without

changing the external API contract.



Kafka is therefore an evolution path rather than a prototype dependency.



\---



\## 8. Rate Limiting



Selected public endpoints are protected by rate limiting.



The prototype may maintain rate-limit state within the application process.



This is appropriate for demonstrating the control but does not provide a

globally consistent limit across multiple application instances.



At larger scale, rate-limit state should move to shared infrastructure such as

Redis or an API gateway.



\---



\## 9. Horizontal Scaling



The HTTP application is designed to remain stateless.



A production topology could use:



Load Balancer

&#x20;    |

&#x20;+---+---+

&#x20;|   |   |

App App App

&#x20;|   |   |

&#x20;+---+---+

&#x20;    |

Shared PostgreSQL



Application-local mechanisms would need to evolve when multiple instances are

introduced.



Examples:



\- Local rate limiting -> distributed rate limiting

\- In-process analytics -> durable event broker

\- Optional local caching -> distributed cache



This prototype documents these boundaries rather than pretending to provide a

multi-node production deployment.



\---



\## 10. Reliability



Reliability controls include:



\- Database uniqueness constraint for short codes

\- Collision handling

\- URL validation

\- Expiration checks

\- Controlled exception mapping

\- HTTP 404 for unknown short codes

\- HTTP 410 for expired short codes

\- HTTP 429 for rate-limit violations

\- Automated tests

\- Build verification



\---



\## 11. Security Considerations



The service accepts only HTTP and HTTPS URLs.



The redirect service does not fetch the destination URL itself.



Secrets must not be committed to source control.



Database credentials for the PostgreSQL profile are supplied through

environment variables.



Additional production controls could include:



\- Authentication for administrative operations

\- Abuse/phishing detection

\- Domain restrictions

\- Centralized distributed rate limiting

\- TLS termination

\- Audit logging



\---



\## 12. Key Engineering Decisions



\### Modular Monolith Instead of Microservices



The current domain is small and does not justify operationally independent

services.



A modular monolith provides simpler development, testing, and deployment while

preserving clear boundaries.



\### H2 as Default



H2 minimizes evaluator setup and makes the prototype immediately runnable.



\### PostgreSQL as Production-Oriented Profile



PostgreSQL demonstrates durable shared persistence without forcing external

infrastructure on every evaluator.



\### No Mandatory Redis



Caching is not required for correctness at prototype scale.



Redis becomes valuable when measured traffic or distributed coordination

justifies it.



\### No Kafka in the Prototype



A durable event broker would improve analytics durability at significant

traffic volumes, but introducing Kafka solely for a prototype redirect counter

would add unnecessary operational complexity.



The asynchronous boundary is retained so the implementation can evolve toward

event-driven analytics later.



\---



\## 13. Trade-offs



The prototype prioritizes:



\- Correctness

\- Reproducibility

\- Clear code

\- Automated validation

\- Low setup friction

\- Defensible architecture



over demonstrating infrastructure purely for technology breadth.



Known limitations are documented rather than hidden.

