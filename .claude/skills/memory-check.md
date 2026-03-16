---
name: memory-check
description: Scan codebase for memory leaks, retain cycles, and lifecycle issues
allowed-tools: Read, Glob, Grep
---

# Memory Safety Check Skill

Scan the Android codebase for common memory safety issues.

## Checks

### 1. GlobalScope usage
```bash
grep -rn "GlobalScope" --include="*.kt"
```

### 2. Missing viewModelScope
```bash
# Find launch without viewModelScope
grep -rn "\.launch" --include="*.kt" | grep -v "viewModelScope"
```

### 3. Activity/Context leaks
```bash
# Find context storage without applicationContext
grep -rn "private.*context:" --include="*.kt" | grep -v "applicationContext"
```

### 4. WeakReference for action callbacks (CRITICAL)
```bash
# Find WeakReference that might be used for critical callbacks
grep -rn "WeakReference.*action\|WeakReference.*callback\|WeakReference.*navigate" --include="*.kt"
```

### 5. Missing Channel cleanup
```bash
# Find Channel declarations - verify they're closed properly
grep -rn "Channel<" --include="*.kt"
```

### 6. collectAsState without Lifecycle
```bash
grep -rn "collectAsState()" --include="*.kt" | grep -v "Lifecycle"
```

## Output Format

```markdown
## Memory Safety Report

### Critical Issues
- [ ] `file:line` - [Issue] - [Fix]

### Warnings
- [ ] `file:line` - [Issue] - [Fix]

### Summary
- ViewModels scanned: X
- Issues found: X critical, X warnings
```
