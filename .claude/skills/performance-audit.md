---
name: performance-audit
description: Audit Android app performance against industry benchmarks
allowed-tools: Read, Glob, Grep, Bash
---

# Android Performance Audit Skill

Audit the codebase against mobile performance benchmarks.

## Performance Targets

| Metric | Target | Critical |
|--------|--------|----------|
| Cold start time | <1.5s | <3s |
| Memory baseline | <120MB | <200MB |
| Frame rate | 60 FPS | 30 FPS |
| Battery/hour | <4% | <8% |
| Crash rate | <0.1% | <1% |
| APK size (initial) | <40MB | <100MB |

## Checks

### 1. Main Thread I/O
```bash
# Find I/O without Dispatchers.IO
grep -rn "File(" --include="*.kt" | grep -v "Dispatchers.IO"
grep -rn "readText\|writeText" --include="*.kt"
```

### 2. Recomposition Issues
```bash
# Find unstable lambdas (not method references)
grep -rn "onClick = {" --include="*.kt" | grep -v "::"

# Find missing keys in LazyColumn
grep -rn "items(" --include="*.kt" | grep -v "key ="
```

### 3. Missing @Immutable
```bash
# Find data classes with List (should use ImmutableList)
grep -rn "data class.*List<" --include="*.kt" | grep -v "ImmutableList"
```

### 4. Large Bitmaps
```bash
grep -rn "BitmapFactory.decode" --include="*.kt"
```

### 5. Missing remember
```bash
# Objects created in composable without remember
grep -rn "= DateFormat\|= SimpleDateFormat\|= DecimalFormat" --include="*.kt"
```

## Output Format

```markdown
## Performance Audit Report

### Metrics Status
| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Cold start | ? | <1.5s | ? |
| Memory | ? | <120MB | ? |
| Frame rate | ? | 60 FPS | ? |

### Issues Found

#### Critical (Blocks Release)
- [ ] `file:line` - [Issue] - [Impact]

#### High (Fix Before Release)
- [ ] `file:line` - [Issue] - [Impact]

### Recommendations
1. [Recommendation 1]
2. [Recommendation 2]
```
