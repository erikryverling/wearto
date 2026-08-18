# Domain docs

This repository uses a single-context domain documentation layout.

## Before exploring

- Read root `CONTEXT.md` when it exists.
- Read relevant decisions under `docs/adr/`.
- If either is absent, proceed silently. Domain documentation is created lazily when terminology or decisions are resolved.

## Layout

- `CONTEXT.md` contains the domain glossary.
- `docs/adr/` contains system-wide architectural decisions.

## Consumer rules

Use glossary terms consistently in issues, plans, tests, and code. If work conflicts with an ADR, identify the conflict explicitly rather than silently overriding the decision.
