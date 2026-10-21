# ADR 0001: Data access with Spring Data JPA

- Status: Accepted
- Date: 2026-10-21

## Context
The app uses the data from Roundnet Analyzer and allows users to ask roundnet-statistics related queries about specific players and the match. The app will respond accordingly, e.g. "By what margin was Player X's kill % better than Player Y's?" The app answers through fixed endpoints, that returns numbers, and then a script compares them. The app will do two kinds of work: Saving and Reading. Saving happens once, per whole match, and Reading happens constantly, computing math over many points into statistics. The app will create a copy of the existing database from Roundnet Analyzer using Flyway. The database design came first and keeps many of its rules inside Postgres. Later, the video-clip queue will have several workers, and each must grab a different job.

## Options considered
1. jOOQ: not as dominant in jobs, but would have been better at big statistics queries, as jOOQ is a SQL-style Java tool.
2. Spring Data JDBC: writes queries as plain text strings, and that doesn't make it better than JPA at handling statistical queries. They aren't checked by the compiler.
3. Hybrid (jOOQ + JPA): two tools would map the same tables in two different ways, which means double the setup. The app only has one kind of saving (ingest), and that’s not enough to justify a second tool.

## Decision
I chose JPA because of job-market familiarity and learning Hibernate. JPA is also good at saving a whole match because it pretends that the database is Java objects, so it is good at loading/saving those objects. The call save(match) also saves its games, points, and serves.

## Consequences
1. Translation Setup: Because the database design came first, it has special column types that JPA doesn't automatically understand, like fixed-value lists and tables keyed by two columns. I need to handle that separately.
2. Late Errors: Because JPA doesn't constantly flush, and it sends the saves together in chunks, if there is an error, the error isn't reported in that instant, but rather when the whole chunk is flushed. To address this, we should check the file first before saving, and also save the whole match in one transaction (either nothing saves or everything saves).
3. Unchecked Stats Queries: Most stats queries will be raw SQL text inside Java. If a column gets renamed, the compiler won’t notice, and the query only breaks when it runs. However, via Testcontainers, we can create a test that runs against the real database, so in the case this error happens, it fails on the test first before it is visible to the user.
4. Flyway owns the tables, and Hibernate only checks that the Java classes match them.

## When to revisit
1. Since with JPA, if the analytics would end up as native SQL strings/raw SQL text, and the reading stages of mapping query results to Java objects would be arduous. At that stage, we could consider adding jOOQ for reading only.
2. Postgres has a feature, SKIP LOCKED, which means: skip jobs another worker has grabbed. If this turns out to be awkward from JPA, I wouldn't want to switch tools for the whole app, but rather switch to something smaller, which would be just one raw SQL query used for "grab the next free job."