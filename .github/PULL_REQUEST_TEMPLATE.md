## What
<!-- Brief summary of what changed -->

## Why

## How to Test
<!-- Steps a reviewer can follow to verify the change locally -->
1.

---

## Author Checklist

- [ ] Branch is up to date with the parent epic branch (or `main` for epic-level PRs)
- [ ] Code builds locally without errors
- [ ] All existing tests pass
- [ ] New logic is covered by tests (or a Jira sub-task exists for test coverage)
- [ ] No debug output, dead code, or untracked TODO comments left in
- [ ] No secrets or credentials committed — use `.env.example` for new environment keys
- [ ] If an endpoint was added or changed: [docs/api-reference.md](../docs/API-REFERENCE.md) and the Postman collections in [docs/postman/](../docs/postman) are updated in this PR (see [docs/postman/README.md](../docs/postman/README.md)`)
- [ ] PR title follows the `B3-### Description` format
- [ ] PR is targeting the correct base branch (epic branch for stories/tasks, `main` for epics)

## Reviewer Checklist

- [ ] Logic is correct and matches the ticket scope
- [ ] No security issues introduced (auth checks, input validation, no secrets in code)
- [ ] Code follows existing patterns and naming conventions in the module
- [ ] Tests are meaningful and cover the intended behavior
