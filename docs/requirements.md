\# URL Shortener - Requirement Analysis



\## 1. Objective



Build a reliable URL shortener service that converts long URLs into

short, shareable URLs and redirects users back to the original URL.



The solution should demonstrate:



\- Clean and maintainable API design

\- Durable URL persistence

\- Redirect handling

\- Optional URL expiration

\- Basic analytics

\- Reliability and error handling

\- Caching for frequently accessed URLs

\- Basic abuse protection

\- Automated testing

\- AI-assisted engineering with human review and validation



\---



\## 2. Functional Requirements



\### FR-1: Create Short URL



The system shall accept a valid HTTP or HTTPS URL and generate a

unique short code.



Example:



Input:



https://example.com/products/123



Output:



http://localhost:8080/aB3x9K



Acceptance Criteria:



\- HTTP and HTTPS URLs are accepted.

\- Invalid or unsupported URLs are rejected.

\- Every stored URL has a unique short code.

\- The URL mapping is persisted in PostgreSQL.

\- Short-code collisions must not overwrite an existing mapping.



\---



\### FR-2: Redirect Short URL



When a user accesses a valid short URL, the system shall redirect

the user to the corresponding original URL.



Acceptance Criteria:



\- Existing and active short codes redirect to the original URL.

\- Unknown short codes return HTTP 404.

\- Expired short URLs do not redirect.

\- Frequently accessed mappings may be served from Redis cache.

\- PostgreSQL remains the source of truth.



\---



\### FR-3: URL Expiration



A shortened URL may optionally have an expiration time.



Acceptance Criteria:



\- Expiration is optional.

\- A URL without expiration remains active.

\- An expired URL returns HTTP 410 Gone.

\- Expired URLs must not be treated as successful redirects.

\- Expired redirects must not increment analytics.



\---



\### FR-4: Analytics



The system shall track the total number of successful redirects

for each shortened URL.



Acceptance Criteria:



\- Each successful redirect increments the click count.

\- Failed redirect attempts do not increment the count.

\- Expired redirect attempts do not increment the count.

\- Analytics can be retrieved for a short code.



\---



\### FR-5: Caching



Frequently accessed URL mappings should be cached using Redis to

reduce repeated database reads.



Acceptance Criteria:



\- The application checks Redis before querying PostgreSQL during

&#x20; redirect lookup.

\- On a cache miss, the mapping is retrieved from PostgreSQL.

\- Retrieved mappings may then be stored in Redis.

\- PostgreSQL remains the authoritative source of data.

\- Correctness must not depend on Redis being permanent storage.



\---



\### FR-6: Rate Limiting



The application should provide basic protection against excessive

requests to public endpoints.



Acceptance Criteria:



\- Excessive requests are rejected with an appropriate HTTP response.

\- Normal application usage is not affected.

\- The implementation remains simple enough for the prototype.

\- Limitations of local rate limiting are documented.



\---



\## 3. Reliability and Validation



The service shall handle expected failures consistently.



\- Invalid URL -> HTTP 400 Bad Request

\- Unknown short code -> HTTP 404 Not Found

\- Expired short code -> HTTP 410 Gone

\- Excessive requests -> HTTP 429 Too Many Requests

\- Unexpected server failure -> HTTP 500 Internal Server Error

\- Short-code collisions must not overwrite existing mappings.

\- Cache failures should not make Redis the source of truth.

\- API responses should not expose internal stack traces.



\---



\## 4. Data Requirements



Each shortened URL should contain sufficient information to support

the required functionality.



The persisted URL mapping includes:



\- Unique identifier

\- Short code

\- Original URL

\- Creation timestamp

\- Optional expiration timestamp

\- Successful redirect count



The short code must have a unique database constraint/index because

it is the primary lookup value during redirects.



\---



\## 5. Assumptions



\- Only HTTP and HTTPS destination URLs are supported.

\- Authentication and authorization are outside the scope of this

&#x20; prototype.

\- Analytics means total successful redirect count for the prototype.

\- PostgreSQL is used as the primary persistent database.

\- H2 may be used for lightweight automated tests.

\- Redis is used as a cache optimization.

\- PostgreSQL remains the source of truth.

\- The service is implemented as a modular monolith.

\- Docker Compose may be used to provide local PostgreSQL and Redis

&#x20; infrastructure.



\---



\## 6. Out of Scope



The prototype does not implement:



\- User accounts

\- Authentication and authorization

\- Geographic analytics

\- Device or browser analytics

\- Malicious URL reputation checking

\- Kafka or another message broker

\- Database replication

\- Kubernetes deployment

\- Multi-region deployment

\- Full distributed observability infrastructure



These can be considered future production enhancements when justified

by scale or business requirements.



\---



\## 7. Engineering Scenarios



\### Scenario 1: Greenfield Development



Build the initial URL-shortening service from scratch.



Initial scope:



\- URL validation

\- Short-code generation

\- PostgreSQL persistence

\- Short URL creation

\- Redirect functionality

\- Consistent error handling

\- Automated tests



The greenfield implementation should be validated before introducing

later enhancements.



\---



\### Scenario 2: Brownfield Enhancement



After the core application is working, enhance the existing system

with optional URL expiration.



Before implementation:



\- Identify affected components.

\- Identify persistence changes.

\- Identify redirect-flow changes.

\- Identify regression risks.

\- Define additional acceptance criteria.



Then implement expiration and validate that existing URL creation and

redirect behavior continues to work.



Caching and additional reliability improvements can also be introduced

incrementally after the core behavior is stable.



\---



\### Scenario 3: Ambiguous Requirement



Original requirement:



"Add analytics."



This requirement is ambiguous because it does not specify which

analytics are required.



For this prototype, the requirement is normalized to:



"Track the total number of successful redirects for each shortened

URL and expose that count through an API."



Assumptions:



\- Only successful redirects count as clicks.

\- Unknown short codes do not increment analytics.

\- Expired URLs do not increment analytics.

\- Geographic, device, browser, and time-series analytics are outside

&#x20; the current scope.



More detailed analytics would require additional product requirements.



\---



\## 8. Task Decomposition



1\. Initialize and verify the Spring Boot project.

2\. Document requirements and architecture decisions.

3\. Configure PostgreSQL persistence.

4\. Define the initial URL persistence model.

5\. Implement URL validation.

6\. Implement unique short-code generation.

7\. Implement short-URL creation.

8\. Implement redirect functionality.

9\. Add consistent API error handling.

10\. Add unit and integration tests for the greenfield implementation.

11\. Validate the greenfield implementation.

12\. Analyze the existing codebase for expiration impact.

13\. Implement optional URL expiration.

14\. Add expiration regression tests.

15\. Clarify and normalize the analytics requirement.

16\. Implement successful redirect-count analytics.

17\. Add analytics tests.

18\. Introduce Redis caching for redirect lookups.

19\. Validate database fallback behavior.

20\. Add basic rate limiting.

21\. Review security and failure scenarios.

22\. Add Docker Compose for local infrastructure.

23\. Run automated quality gates.

24\. Perform end-to-end API validation.

25\. Document AI-assisted engineering decisions.

26\. Document trade-offs, limitations, and future improvements.

27\. Perform final engineer review and sign-off.



\---



\## 9. Validation Strategy



The implementation will be validated through:



\- Unit tests for business logic

\- Integration tests for API behavior

\- Database persistence tests

\- Positive and negative URL validation tests

\- Unknown short-code tests

\- Expiration tests

\- Analytics tests

\- Cache hit and cache miss behavior

\- Rate-limit behavior

\- Regression testing after brownfield changes

\- Maven build and test execution

\- Manual end-to-end API verification



A change is not considered complete solely because AI generated code

successfully. Generated or modified code must be reviewed and validated

before acceptance.



\---



\## 10. Engineering Ownership



AI tools may assist with:



\- Requirement analysis

\- Task decomposition

\- Implementation suggestions

\- Debugging

\- Refactoring

\- Test generation

\- Documentation

\- Security and reliability review



AI-generated output will be reviewed before acceptance.



Important suggestions may be:



\- Accepted

\- Modified

\- Rejected



The reasoning for significant decisions will be documented where

appropriate.



The engineer remains responsible for correctness, maintainability,

security, testing, architecture decisions, and final approval.

