---
description: Code review with multi-pass quality checks
allowed-tools: Read, Grep, Glob, Bash(git diff:*), Bash(git log:*)
---

# Code Review Workflow

## Review Target
$ARGUMENTS

---

## Step 1: Get Changes
```bash
git diff HEAD~1
git log --oneline -5
```

---

## Step 2: Quality Guardian Check (Pass 1)

### Critical Issues (Block Merge)
- [ ] Force unwraps (`!!`)
- [ ] GlobalScope usage
- [ ] State-based navigation
- [ ] Missing Channel for events

### Architecture Issues
- [ ] Concrete dependencies (should be interface)
- [ ] God objects (500+ lines)
- [ ] Business logic in wrong layer

---

## Step 3: Navigation Guardian Check (Pass 2)
- [ ] Channel for navigation events
- [ ] LaunchedEffect(Unit) for collection
- [ ] No NavController in composables

---

## Step 4: Code Quality (Pass 3)
### Readability
- [ ] Clear naming
- [ ] Appropriate comments

### Complexity
- [ ] Functions under 30 lines
- [ ] Single responsibility
- [ ] No deep nesting (max 3 levels)

---

## Output Format

### Summary
**Files Reviewed:** [count]
**Risk Level:** Low / Medium / High / Critical

### Must Fix (Blocking)
- `file:line` - [Issue]

### Should Fix (Recommended)
- `file:line` - [Issue]

### Good Patterns
- [Pattern used correctly]
