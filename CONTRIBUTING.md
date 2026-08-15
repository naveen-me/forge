# Development Workflow

## Before coding

Read:

- AGENTS.md
- PROJECT_SPEC.md
- ARCHITECTURE.md
- DECISIONS.md
- ROADMAP.md

## Change process

1. State the task in a short plan.
2. Identify affected components.
3. Implement the smallest useful change.
4. Add/modify tests.
5. Build.
6. Run tests.
7. Run the relevant integration test.
8. Record important findings.
9. Keep commits focused.

## Architecture changes

If changing a major technology:

- create/update an ADR
- explain the evidence
- explain alternatives
- explain performance/licensing impact
- do not hide the change in a refactor

## Performance changes

Record before/after metrics when possible.

## Commits

Prefer small, meaningful commits such as:

```text
feat: add timeline state model
feat: add same-layer replacement
test: cover exact start boundaries
feat: add WPE frame bridge
perf: reduce compositor frame copies
fix: reconnect HLS source
```
