---
description: Debug and fix bugs with TDD approach
allowed-tools: Read, Edit, Write, Bash, Glob, Grep
---

# Bug Fix Workflow

## Bug Description
$ARGUMENTS

---

## Step 1: Investigation (kotlin-debugger)
### Gather Evidence
- What exactly happens? (symptoms)
- What should happen? (expected)
- Steps to reproduce

### Form Hypotheses
| Hypothesis | Likelihood | Evidence Needed |
|------------|------------|-----------------|
| [Cause 1] | High | [What to check] |

**Output:** Root cause with evidence

---

## Step 2: Write Failing Test First
```kotlin
@Test
fun `bug scenario should not crash`() {
    // This test should FAIL before the fix
}
```

---

## Step 3: Fix Implementation (kotlin-developer)
- [ ] Minimal change for root cause
- [ ] Don't refactor unrelated code
- [ ] Follow platform patterns

---

## Step 4: Verification
### Pass 1: Run Failing Test
- Test should now PASS

### Pass 2: Quality Guardian Check
- [ ] Fix doesn't introduce new issues
- [ ] No anti-patterns introduced

### Pass 3: Regression Test
- [ ] No other tests broke

---

## Output
### Root Cause
**Location:** `file:line`
**Problem:** [Technical explanation]

### Fix Applied
```kotlin
// Before
[problematic code]

// After
[fixed code]
```
