---
model: claude-sonnet-4-6
allowed-tools: Read, Edit, Bash, Grep, Skill, AskUserQuestion
description: >-
  Run /build-verify, then if the build fails, show the diagnosis and the
  exact files that need to change, ask permission, and apply the fix only
  if granted. Use when you want to go from "the build is broken" to "the
  build is fixed" in one guided step, rather than just diagnosing it.
---

Get the project building again.

## Step 1 — Run /build-verify

Invoke the `build-verify` command (via the Skill tool, `skill: build-verify`)
to compile the project and diagnose any failure. Don't re-implement its
diagnosis logic here — build-verify is the source of truth for the failure
category, the root cause, and the candidate fix(es).

## Step 2 — Show the result

If build-verify reports **BUILD SUCCESSFUL**, say so in one line and stop.
There's nothing to fix.

If it reports **BUILD FAILED**, present, in this order:
1. **Status:** BUILD FAILED
2. **Category:** the failure category from build-verify (toolchain,
   dependency resolution, compilation error, or plugin/config error)
3. **Root cause:** the explanation build-verify gave, in one or two sentences
4. **Files to change:** an explicit list — every file build-verify's fix
   steps point at (e.g. `settings.gradle`, `build.gradle`, or a specific
   `.java` file and line), with what will change in each. If build-verify
   offered more than one valid fix (e.g. install JDK 21 locally, OR add the
   Foojay resolver plugin to `settings.gradle`), list both options and their
   trade-offs — the user needs to pick one, not just approve "a" fix.

## Step 3 — Ask permission

Use `AskUserQuestion` to ask whether to apply a fix, and which one. If Step 2
surfaced multiple valid options, make each its own answer choice, plus a
"don't fix it, just show me" option to decline. Don't touch any file before
this question is answered.

## Step 4 — Apply the fix, only if granted

If the user approves a fix:
- Make the minimal edit(s) it requires — only the files listed in Step 2,
  only the change described there. Don't refactor or clean up anything else
  while in the file.
- Re-run `/build-verify` to confirm the build is now green.
- Report before/after: what failed, what changed, and the new result.

If the user declines, or picks "just show me": make no changes and stop.
Confirm in one line that nothing was touched.

## Boundaries

- Never modify a file that wasn't named in the Step 2 list the user approved.
- Never skip Step 3 — even if the fix looks trivial or obviously correct,
  this command exists specifically so nothing changes silently.
- Never touch test files or production business logic to "fix" a build
  failure that's actually a config/environment issue (toolchain, dependency
  resolution, or plugin/config categories from build-verify) — those are
  fixed in config, not code.
- If build-verify itself can't run (e.g. `./gradlew` isn't executable),
  report that as the finding — don't try to work around it.
