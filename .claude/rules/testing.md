# Testing Rules

## TDD Workflow
1. Write failing test first
2. Implement to pass
3. Refactor

## Test Structure (Given/When/Then)
```kotlin
@Test
fun `series list should show loading then success`() = runTest {
    // Given
    val mockSeries = listOf(Series(id = "1", title = "Test"))
    coEvery { repository.getSeries() } returns Result.success(mockSeries)

    // When
    viewModel.loadSeries()

    // Then
    val state = viewModel.uiState.value
    assertIs<SeriesUiState.Success>(state)
    assertEquals(mockSeries, state.series)
}
```

## Edge Case Coverage
- Empty states (empty list, null values)
- Error states (network failure, API error)
- Boundary conditions (first item, last item, max limit)
- Configuration changes (rotation)
- Loading states

## ViewModel Tests
```kotlin
@Test
fun `navigation event should be sent once`() = runTest {
    // Given
    val events = mutableListOf<NavigationEvent>()
    val job = launch { viewModel.navigationEvent.toList(events) }

    // When
    viewModel.onItemClick("123")

    // Then
    assertEquals(1, events.size)
    assertIs<NavigationEvent.NavigateToDetail>(events[0])

    job.cancel()
}
```
