\# Ambiguous Scenario - Redirect Analytics



\## Original Requirement



> Add analytics.



\## Identified Ambiguity



The requirement does not define what "analytics" means.



Possible interpretations include:



\- Total redirect count

\- Unique visitors

\- Geographic location

\- Referrer

\- Device or browser information

\- Time-series metrics

\- Failed redirect attempts

\- Expired URL attempts



Implementing all of these without clarification would increase scope and

introduce unnecessary data collection.



\## Clarified Requirement



For this prototype, analytics is normalized to:



> Track the total number of successful redirects for each short URL and expose

> that count through a statistics API.



\## Acceptance Criteria



1\. Each successful redirect increments the redirect count.

2\. Unknown short codes do not increment analytics.

3\. Expired short URLs do not increment analytics.

4\. Analytics can be retrieved using:



&#x20;      GET /api/urls/{shortCode}/stats



5\. The statistics response includes:

&#x20;  - short code

&#x20;  - original URL

&#x20;  - successful redirect count

&#x20;  - creation timestamp

&#x20;  - optional expiration timestamp



6\. Analytics processing should not unnecessarily delay the redirect response.



\## Execution Decision



Redirect analytics will be dispatched asynchronously using a bounded Spring

task executor.



The redirect flow becomes:



shortCode

&#x20; -> lookup URL

&#x20; -> validate expiration

&#x20; -> dispatch analytics update

&#x20; -> return HTTP 302



\## Consistency Trade-off



Because analytics processing is asynchronous, the statistics endpoint provides

eventual rather than immediate consistency.



A statistics request made immediately after a redirect may briefly observe the

previous count.



For this prototype, that trade-off is acceptable because redirect latency is

prioritized over immediately consistent analytics.



\## Persistence Decision



The redirect count is persisted with the URL mapping.



The database update should use an atomic increment operation to reduce the risk

of lost updates from concurrent analytics tasks.



\## Failure Isolation



Analytics is secondary to the redirect operation.



An analytics processing failure should be observable but should not turn an

otherwise valid redirect into a failed redirect.



\## Production Evolution



The prototype uses an in-process asynchronous executor.



At larger scale, a durable event broker could replace this mechanism:



Redirect

&#x20;  -> publish redirect event

&#x20;  -> return response



Event consumer

&#x20;  -> update analytics store



This would improve durability and support independent scaling.



A message broker is intentionally not introduced into this prototype because

the current requirement does not justify that operational complexity.



\## Out of Scope



The prototype does not collect:



\- IP addresses

\- Geographic information

\- Device fingerprints

\- Browser information

\- Referrer history

\- Per-user tracking



This keeps the analytics requirement focused and avoids unnecessary collection

of user data.

