---
trigger: always_on
---

# AI Editor - Development Rules & Guidelines

## Project Overview

Android AI Editor built with **Kotlin**, **Jetpack Compose**, and **MVVM
architecture**.

## 🏗️ Architecture Rules

### MVVM & Data Flow

-   **Strict Unidirectional Data Flow (UDF)**
    -   View observes state from ViewModel\
    -   View sends events to ViewModel\
    -   ViewModel interacts with Repository layer\
    -   **State flows down, events flow up**
-   **Single Source of Truth**
    -   ViewModel holds the single source of truth for UI state\
    -   UI is always a reflection of state

## 📦 Package Structure

    com.yourapp.aieditor/
    ├── feature/
    │   ├── editor/
    │   │   ├── ui/        # Composables, ViewModel, State, Events
    │   │   ├── domain/    # UseCases, Repository interfaces
    │   │   └── data/      # Repository implementations, DataSources, DTOs
    │   └── history/
    ├── core/
    │   ├── data/          # Retrofit, Database, data sources
    │   ├── domain/        # Base UseCase, Repository interfaces
    │   └── ui/            # Theme, BaseViewModel, common composables
    └── di/                # Dependency Injection modules

## 💻 Code Rules

### Kotlin & Jetpack Compose

#### **Immutable State**

``` kotlin
data class EditorState(
    val text: String = "",
    val isGenerating: Boolean = false,
    val error: String? = null
)
```

### Coroutines & Flows

-   Use **viewModelScope** for coroutines\
-   Expose state as **StateFlow** or `State`\
-   Expose events via **SharedFlow** or **Channel**

### Composable Best Practices

-   Composables must be **side-effect free**\
-   Use `remember` for expensive calculations\
-   Use `derivedStateOf` for computed values

## 🗂️ Data Layer

### Abstract AI Client

``` kotlin
interface AiRepository {
    suspend fun generateText(prompt: String): Result<String>
}
```

### Use `Result` Class

``` kotlin
when (result) {
    is Result.Success -> // Update state
    is Result.Error -> // Handle error
}
```

## 🧪 Testing Rules (MANDATORY)

### Testing Pyramid

-   **ViewModel Tests** → State changes & event handling\
-   **Repository Tests** → Data transformation using fake data sources\
-   **UseCase Tests** → Business logic in isolation\
-   **UI Tests** → Key user flows with ComposeTestRule

### Test-Driven Development (TDD)

1.  **RED**: Write a failing test\
2.  **GREEN**: Implement minimum code to pass\
3.  **REFACTOR**: Improve code while tests remain green

### Dependency Injection for Testing

``` kotlin
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel()
```

### Specific Testing Requirements

-   Every **public ViewModel function** must have tests\
-   Every **non-trivial Repository/UseCase method** must have tests\
-   Prefer **Fakes** over Mocks\
-   Tests must be **isolated and deterministic**\
-   Use **test coroutine dispatchers**

## 📝 Example: Implementing a New Feature

### Feature: "Rephrase" Button

#### RED --- Write failing test

``` kotlin
@Test
fun `rephraseText should set isRephrasing to true, call repository, and update text on success`() = runTest {
    val fakeRepo = FakeAiRepository()
    fakeRepo.simulatedResponse = Result.Success("Rephrased text")
    val viewModel = EditorViewModel(fakeRepo)
    viewModel.onTextChanged("Original text")

    viewModel.onRephraseClicked()

    val state = viewModel.state.value
    assertThat(state.isRephrasing).isFalse()
    assertThat(state.text).isEqualTo("Rephrased text")
}
```

## 🚫 Anti-Patterns to Avoid

-   View directly modifying state\
-   Business logic inside Composables\
-   Tight coupling to AI SDKs\
-   Skipping tests\
-   Blocking main thread

## 🔧 Code Style

-   Use descriptive names\
-   Follow Kotlin conventions\
-   Use sealed classes for events/state\
-   Keep functions small\
-   Document complex logic