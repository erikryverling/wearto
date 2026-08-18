# Issue tracker: GitHub

Issues and specs for this repository live in GitHub Issues. Use the `gh` CLI for all operations and infer `erikryverling/wearto` from the repository remote.

## Conventions

- Create issues with `gh issue create --title "..." --body "..."`.
- Read issues with `gh issue view <number> --comments`.
- List issues with `gh issue list`, requesting labels, body, and comments as needed.
- Comment with `gh issue comment <number> --body "..."`.
- Apply or remove labels with `gh issue edit`.
- Close issues with `gh issue close <number> --comment "..."`.
- When a skill says to publish to the issue tracker, create a GitHub issue.
- When a skill says to fetch a ticket, use `gh issue view <number> --comments`.

## Pull requests as a triage surface

PRs as a request surface: no.

## Dependencies

Use GitHub sub-issues and native issue dependencies when available. If unavailable, link children from a task list and record blockers as `Blocked by: #<number>`.
