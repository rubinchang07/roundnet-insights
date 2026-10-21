# ADR 0002: Ingest Loader

- Status: Accepted
- Date: 2026-10-21

## Context
The loader takes the exported JSON file from Roundnet Analyzer, rejects any missing or corrupted data, maps the players to names, cleans it up by ignoring repeats, and replaces games when a newer export of the same match comes in. It also decides if the game is finished. The app needs a loader so data can be analyzed: the data from the Analyzer's JSON file gets turned into tables for the analytics to read. Roundnet Analyzer already has a loader, and this ADR decides whether to keep it or replace it.

## Options Considered
1) Java App Keeps Using the DB Function: Here, the app gets the JSON file and directly passes it to the DB function with a SQL call. The upside is that Ingest works immediately, and the rules are already tested and working. However, it undermines the core of this project, which would be centered around Java. It also contradicts ADR 0001, where I claimed JPA would save a whole match as a Java object, and JPA wouldn't save anything here. Any errors generated would also come back as plain text, which is hard to structure.
2) Rewrite the Function in Java: We follow the plan from ADR 0001, where the app reads the JSON file into separate Java objects, a rule-check is run, and then JPA saves the whole match in one all-or-nothing transaction. Here, we can test the rules with unit tests, and for errors, we can return a structured list of problems instead of one blob of text. The main drawback is that it's a lot of work, and could potentially be dangerous rewriting working code as my Java version might behave differently than the original. 
3) Start with 1, and end with 2: I need to have two loaders running, doing the same job, and kept in sync.

## Decision
Ultimately, I want to be able to talk about my Java project in Java terms, and make it consistent with my ADR 0001 plan. I don't mind writing it again in Java, especially since reading as Java objects enables errors to be outputted as structured lists.

## Consequences
1) Validation exists in both the DB and Java code. Java checks the JSON files with the rules first, then rejects the file with a list of problems. The DB is an extra wave of defense in case the Java code has a bug. 
2) Cost of rewriting + Java version issues. The old loader should only exist in tests as the oracle, not in Flyway, as it only has one responsibility and should not exist in two places (that might confuse people).
3) Possible drifting between Roundnet Analyzer and this project as Roundnet Analyzer may get updated in the future with more file formats. 

## When to Revisit
- If the rewrite to Java takes super long, I might just go back to Option 1 or 3.
- If the oracle tests show differences I can't explain, I would likely keep the old loader (go back to Option 1).
