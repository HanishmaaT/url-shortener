\# URL Shortener - Requirements



\## 1. Objective



Build a reliable URL shortening service that converts long HTTP/HTTPS URLs

into short URLs and redirects users to the original destination.



The prototype must be easy to run locally while demonstrating engineering

practices that can evolve toward a production deployment.



The system demonstrates:



\- URL creation and redirection

\- Input validation and error handling

\- Optional URL expiration

\- Redirect analytics

\- Asynchronous analytics processing

\- Rate limiting

\- Durable database support

\- Automated testing

\- Horizontal scaling considerations

\- AI-assisted engineering with human validation



\---



\## 2. Functional Requirements



\### FR-1: Create Short URL



The system shall expose an API to create a shortened URL.



Requirements:



\- Accept HTTP and HTTPS URLs.

\- Reject malformed or unsupported URLs.

\- Generate a unique short code.

\- Persist the mapping.

\- Prevent short-code collisions.

\- Return the generated short URL.



\### FR-2: Redirect



The system shall redirect a valid short code to its original URL.



Requirements:



\- Existing active short codes return an HTTP redirect.

\- Unknown short codes return HTTP 404.

\- Expired short codes return HTTP 410.

\- Redirect processing should remain lightweight.



\### FR-3: URL Expiration



A URL may optionally contain an expiration time.



Requirements:



\- URLs without expiration remain active.

\- URLs with a future expiration remain active until that time.

\- Expired URLs must not redirect.

\- Expired redirects must not contribute to successful redirect analytics.



\### FR-4: Analytics



The system shall maintain the total number of successful redirects for each

short URL.



Requirements:



\- Only successful redirects are counted.

\- Unknown URLs are not counted.

\- Expired URLs are not counted.

\- Analytics shall be retrievable through an API.

\- Analytics updates should not unnecessarily delay the redirect response.



\### FR-5: Rate Limiting



The application shall protect selected public endpoints from excessive request

rates.



Requirements:



\- Requests within the configured limit are processed normally.

\- Requests exceeding the configured limit return HTTP 429.

\- The prototype may use application-local rate limiting.

\- Distributed rate limiting is documented as a production scaling concern.



\---



\## 3. Persistence Strategy



\### Default Profile



H2 is the default database.



Reasons:



\- Zero external infrastructure required.

\- Reviewer can clone and run the application immediately.

\- Suitable for automated tests and prototype evaluation.



\### PostgreSQL Profile



PostgreSQL is supported as an optional production-oriented profile.



Reasons:



\- Durable persistence.

\- Suitable for shared persistence across multiple application instances.

\- PostgreSQL compatibility is validated separately from the zero-setup profile.



The application uses Spring Data JPA so business logic remains independent of

the selected database profile.



\---



\## 4. Reliability Requirements



The implementation should:



\- Enforce uniqueness of short codes.

\- Handle generated-code collisions safely.

\- Return consistent HTTP error responses.

\- Avoid exposing internal exceptions to API consumers.

\- Keep the redirect path lightweight.

\- Prevent analytics failures from breaking successful redirects where practical.



\---



\## 5. Data Requirements



A URL mapping may contain:



\- Internal identifier

\- Short code

\- Original URL

\- Creation timestamp

\- Optional expiration timestamp

\- Successful redirect count



The short code must have a uniqueness constraint/index.



\---



\## 6. Scaling Requirements



The application should be designed so that the API layer can remain stateless.



A production deployment could horizontally scale multiple application

instances behind a load balancer using shared PostgreSQL persistence.



Prototype-local mechanisms such as in-memory rate limiting and in-process

asynchronous analytics would require distributed replacements at larger scale.



Potential production evolutions include:



\- Redis for distributed caching and rate limiting

\- Kafka or another durable event broker for high-volume analytics

\- Multiple stateless application instances

\- Managed PostgreSQL

\- Centralized observability



These are architectural evolution paths, not requirements for this prototype.



\---



\## 7. Scenario 1 - Greenfield Development



Initial requirement:



> Build a URL shortening service.



Scope:



\- URL validation

\- Short-code generation

\- Persistence

\- Create API

\- Redirect API

\- Error handling

\- Automated tests



Acceptance criteria:



\- A valid URL can be shortened.

\- The generated short code is persisted.

\- Visiting the short URL redirects to the original URL.

\- Invalid URLs are rejected.

\- Unknown short codes return 404.



\---



\## 8. Scenario 2 - Brownfield Enhancement



Change request:



> Existing short URLs should optionally expire.



Before implementation, impacted components must be identified.



Expected impact:



\- Persistence model

\- Create request

\- Service logic

\- Redirect behavior

\- Error handling

\- Regression tests



Acceptance criteria:



\- Existing URLs without expiration continue to work.

\- Future expiration timestamps are supported.

\- Expired URLs return HTTP 410.

\- Existing URL-shortening behavior is not broken.



\---



\## 9. Scenario 3 - Ambiguous Requirement



Original requirement:



> Add analytics.



The requirement is ambiguous because "analytics" could include clicks,

geography, devices, referrers, time-series data, or other metrics.



For this prototype, the requirement is normalized to:



> Track the total number of successful redirects for each short URL and expose

> the value through a statistics API.



Acceptance criteria:



\- Successful redirects increment the count.

\- Unknown URLs do not increment analytics.

\- Expired URLs do not increment analytics.

\- Statistics can be retrieved for a short code.

\- Analytics processing is separated from the primary redirect response path.



\---



\## 10. Validation Strategy



Changes will be validated using:



\- Unit tests

\- Integration tests

\- API behavior tests

\- Maven build verification

\- Regression testing after brownfield changes

\- Manual API verification where useful



The final validation command is:



&#x20;   ./mvnw clean verify



On Windows:



&#x20;   mvnw.cmd clean verify



\---



\## 11. Engineering Ownership



AI may assist with:



\- Requirement analysis

\- Task decomposition

\- Implementation suggestions

\- Test generation

\- Debugging

\- Documentation

\- Review



AI-generated suggestions are reviewed before acceptance.



The engineer remains responsible for:



\- Architecture decisions

\- Correctness

\- Security

\- Testing

\- Maintainability

\- Production-readiness assessment

