---
allowed-tools: Bash, Read, Grep
description: >-
  Verify the Gradle project builds cleanly — compiles main and test sources
  and checks for compilation errors, without running the test suite. On
  failure, diagnoses the root cause (toolchain, dependency resolution, or
  compile error) and reports concrete fix steps. Use before committing, after
  a refactor, or whenever you need a fast signal that the project still
  compiles. Not a substitute for `/accept` or `/tdd`, which run tests as part
  of the red/green cycle.
---

Verify the project builds successfully.

## Command

Run from the project root:

```
./gradlew compileJava compileTestJava
```

This compiles main and test sources without executing tests — the fastest
signal that nothing is broken. Tests are run by `/accept` and `/tdd`, not
here, so their red/green output stays meaningful and isn't duplicated.

If the user explicitly asks for a full build (including tests, e.g. before a
release or a PR), run `./gradlew build` instead.

## Reading the result

- **BUILD SUCCESSFUL** — report success in one line. Nothing else to do.
- **BUILD FAILED** — do not silently retry with flags that mask the failure
  (`--offline`, skipping dependency resolution, etc.). Diagnose it (see
  below) and report:
  1. **What failed** — the exact task and error message from Gradle's output.
  2. **Why** — the root cause, using the categories below to identify it.
  3. **How to fix it** — concrete, actionable steps (exact command or exact
     file/line to change). Don't apply the fix yourself unless the user has
     already asked you to fix build errors as part of a larger task in
     progress — see Boundaries.

## Diagnosing common failures

Match the error text to a category before writing up the report; don't just
paste the raw Gradle output back at the user.

**1. Toolchain / JDK mismatch**
Signature: `Cannot find a Java installation on your machine matching:
{languageVersion=...}` or `Toolchain download repositories have not been
configured.`
Cause: `build.gradle` pins a `languageVersion` (see the `java { toolchain {
... } }` block) that isn't installed locally, and Gradle has no toolchain
repository configured to auto-download one.
Fix steps — offer both, let the user pick:
- Install the pinned JDK version locally (e.g. via `sdkman`, `brew install
  openjdk@<version>`, or the vendor installer), so Gradle's auto-detection
  picks it up.
- Or enable auto-provisioning by adding the Foojay resolver plugin to
  `settings.gradle`:
  ```
  plugins {
      id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
  }
  ```
  This lets Gradle download a matching JDK automatically on any machine.

**2. Dependency resolution failure**
Signature: `Could not resolve all dependencies for configuration
':compileClasspath'` / `':testCompileClasspath'`, `Could not find
<group>:<artifact>:<version>`, or a network/repository error.
Cause: a dependency coordinate in `build.gradle` is wrong or unavailable, a
required repository is missing from the `repositories {}` block, or there's
no network access to `mavenCentral()`.
Fix steps: check the dependency line named in the error against
`build.gradle`, confirm the version exists (typo, yanked release, or wrong
artifact ID), and confirm `repositories { mavenCentral() }` is present. If
it's a network issue, that's environmental — report it as such rather than
editing the build file.

**3. Compilation error in source**
Signature: `error: ...` lines from `javac` under a `:compileJava` or
`:compileTestJava` task, pointing at a specific `.java` file and line number.
Cause: an actual code defect — type mismatch, missing import, syntax error,
undefined symbol.
Fix steps: report the exact file, line, and error message. This is the one
category where "the fix" is a code change, not a config change — describe
what the compiler is objecting to, but only edit the file if the user has
asked you to fix build errors as part of an in-progress task.

**4. Plugin / project configuration error**
Signature: failure during Gradle configuration (before any compile task
starts), e.g. `Plugin [id: '...'] was not found`, or a `build.gradle` /
`settings.gradle` syntax error.
Cause: a malformed or missing plugin declaration, usually in `build.gradle`
or `settings.gradle`.
Fix steps: point to the exact plugin block or line Gradle's error references
and what's wrong with it (missing version, typo'd plugin ID, wrong Gradle
Plugin Portal coordinates).

## Boundaries

- Do NOT modify any files. This is a verification and diagnosis step, not a
  fix step — report the fix, don't apply it, unless the user has already
  asked you to fix build errors as part of a larger task already in
  progress.
- Do NOT run the full test suite unless explicitly asked — that's `/accept`
  and `/tdd`'s job, and running it here would duplicate red/green signal
  outside the TDD cycle.
- Do NOT use `--offline` or skip dependency resolution — a build that can't
  resolve dependencies is a real failure to surface, not one to mask.
