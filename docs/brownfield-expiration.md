\# Brownfield Scenario - Optional URL Expiration



\## Change Request



Existing short URLs should optionally support an expiration time.



\## Existing Behavior



The current application supports:



\- Creating a short URL

\- Persisting the URL mapping

\- Redirecting a valid short code

\- Returning 404 for unknown short codes

\- Rejecting invalid URLs



The existing Greenfield implementation is validated by automated unit and

integration tests.



\## Impact Analysis



\### UrlMapping

Add an optional `expiresAt` timestamp.



Existing records with a null expiration must remain valid.



\### CreateUrlRequest

Allow the client to optionally provide an expiration timestamp.



The field remains optional so existing requests continue to work.



\### CreateUrlResponse

Return the expiration timestamp when one is configured.



\### UrlService

Creation logic must reject expiration timestamps that are not in the future.



Redirect resolution must check whether the URL has expired.



\### RedirectController

No structural API change is required.



Expiration will be checked by the service before redirecting.



\### Exception Handling

Introduce an expiration-specific exception mapped to HTTP 410 Gone.



\### Tests

Existing tests must continue to pass.



New tests must cover:



\- URL without expiration

\- URL with future expiration

\- URL with past expiration

\- Redirect of expired URL

\- HTTP 410 response



\## Regression Risk



The primary compatibility risk is accidentally treating existing URLs with no

expiration as expired.



Therefore:



&#x20;   expiresAt == null



means the URL does not expire.



\## Acceptance Criteria



1\. Existing requests without expiration continue to work.

2\. A future expiration timestamp can be supplied.

3\. A past or current expiration timestamp is rejected.

4\. URLs without expiration continue to redirect.

5\. Expired URLs return HTTP 410 Gone.

6\. Unknown short codes continue to return HTTP 404.

7\. Existing Greenfield tests continue to pass.



\## Data Flow



Create:



Client

&#x20; -> CreateUrlRequest

&#x20; -> UrlService expiration validation

&#x20; -> UrlMapping

&#x20; -> Database



Redirect:



shortCode

&#x20; -> lookup mapping

&#x20; -> not found: 404

&#x20; -> check expiresAt

&#x20;      -> null/future: redirect

&#x20;      -> expired: 410



\## Engineering Decision



Expiration is stored as an absolute `Instant`.



This avoids coupling persisted expiration data to the application server's

local timezone.

