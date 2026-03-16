---
description: Complete feature development workflow with multi-pass verification
allowed-tools: Read, Edit, Write, Bash, Glob, Grep
---

# Feature Development Workflow

## Feature Request
$ARGUMENTS

---

## Model Selection
- Planning: **opus** (deep reasoning)
- Exploration: **haiku** (fast research)
- Implementation: **sonnet** (balanced coding)
- Verification: **sonnet** (quality review)

---

## Phase 1: Planning (ultrathink-planner)
1. Analyze requirements with extended thinking
2. Explore existing patterns (via subagent)
3. Consider 2-3 approaches
4. Create step-by-step plan

**Output:** Implementation plan

---

## Phase 2: Edge Case Analysis (edgecase-analyzer)
- Data boundaries (empty, null, large)
- Network failure scenarios
- Configuration changes (rotation)

**Output:** Edge case checklist

---

## Phase 3: Implementation (kotlin-developer)
- [ ] Follow the plan step by step
- [ ] Handle all identified edge cases
- [ ] Channel for navigation events
- [ ] viewModelScope for coroutines
- [ ] Sealed class for UI state

---

## Phase 4: Quality Verification (Pass 1)
Use **quality-guardian** to verify:
- [ ] No force unwraps (`!!`)
- [ ] No GlobalScope
- [ ] No state-based navigation
- [ ] Proper error handling

---

## Phase 5: Testing (Pass 2)
Use **kotlin-tester** to:
- Write failing tests first
- Cover edge cases
- Test state transitions

---

## Phase 6: Final Verification (Pass 3)
Run **quality-guardian** again:
- [ ] All issues resolved
- [ ] No new issues
- [ ] Tests passing

---

## Multi-Pass Summary
| Pass | Agent | Status |
|------|-------|--------|
| 1 | quality-guardian | |
| 2 | kotlin-tester | |
| 3 | quality-guardian | |
