# MVVM Architecture Guide for Android Jetpack Compose

## What is MVVM?

**MVVM** stands for **Model-View-ViewModel**. It's an architectural pattern that separates your code into three layers:

```
┌─────────────────────────────────────────────────┐
│                    VIEW                         │
│  (Composables - UI Layer)                      │
│  - Displays data                                │
│  - Handles user interactions                    │
│  - NO business logic                            │
└─────────────────┬───────────────────────────────┘
                  │ observes state
                  │ calls functions
┌─────────────────▼───────────────────────────────┐
│                 VIEWMODEL                       │
│  (Business Logic Layer)                         │
│  - Manages UI state                             │
│  - Handles validation                           │
│  - Processes user actions                       │
│  - Calls repository/use cases                   │
└─────────────────┬───────────────────────────────┘
                  │ fetches/saves data
┌─────────────────▼───────────────────────────────┐
│                  MODEL                          │
│  (Data Layer)                                   │
│  - Data classes                                 │
│  - Repository                                   │
│  - API calls                                    │
│  - Database operations                          │
└─────────────────────────────────────────────────┘
```

## Why Use MVVM?

✅ **Separation of Concerns** - Each layer has a single responsibility
✅ **Testability** - Easy to unit test ViewModels without UI
✅ **Maintainability** - Changes in one layer don't affect others
✅ **Reusability** - ViewModels can be reused across different screens
✅ **Lifecycle Awareness** - ViewModels survive configuration changes

---

## How to Implement MVVM

### Step 1: Create the ViewModel

The ViewModel holds the **state** and **business logic**.

```kotlin
// LoginViewModel.kt
class LoginViewModel : ViewModel() {

    // 1. UI State - holds all data the UI needs
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 2. Events - one-time actions (navigation, snackbar)
    private val _events = MutableStateFlow<LoginEvent?>(null)
    val events: StateFlow<LoginEvent?> = _events.asStateFlow()

    // 3. User actions - functions the UI calls
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onLoginClick() {
        if (validateInputs()) {
            performLogin()
        }
    }

    // 4. Private business logic
    private fun validateInputs(): Boolean {
        // Validation logic here
        return true
    }

    private fun performLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // API call here
            _uiState.update { it.copy(isLoading = false) }
            _events.value = LoginEvent.NavigateToHome
        }
    }

    fun onEventHandled() {
        _events.value = null
    }
}

// UI State - represents what the UI looks like
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

// Events - one-time actions
sealed class LoginEvent {
    object NavigateToHome : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}
```

### Step 2: Create the Composable (View)

The Composable **only displays data** and **calls ViewModel functions**.

```kotlin
// LoginScreen.kt
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(), // Get ViewModel
    onNavigateToHome: () -> Unit = {}
) {
    // 1. Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.events.collectAsState()

    // 2. Handle one-time events
    LaunchedEffect(event) {
        when (event) {
            is LoginEvent.NavigateToHome -> {
                onNavigateToHome()
                viewModel.onEventHandled()
            }
            is LoginEvent.ShowError -> {
                // Show snackbar
                viewModel.onEventHandled()
            }
            null -> { /* No event */ }
        }
    }

    // 3. UI - just displays state and calls ViewModel functions
    Column {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange, // Call ViewModel
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it) } }
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange, // Call ViewModel
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it) } }
        )

        Button(
            onClick = viewModel::onLoginClick, // Call ViewModel
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Login")
            }
        }
    }
}
```

---

## Key Concepts Explained

### 1. StateFlow vs MutableStateFlow

```kotlin
// Private - only ViewModel can modify
private val _uiState = MutableStateFlow(LoginUiState())

// Public - UI can only read
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
```

- **MutableStateFlow** - Can be changed (private in ViewModel)
- **StateFlow** - Read-only (exposed to UI)
- This prevents the UI from directly modifying state

### 2. Updating State

```kotlin
// ❌ DON'T - mutates state directly
_uiState.value.email = "new@email.com"

// ✅ DO - creates new state immutably
_uiState.update { it.copy(email = "new@email.com") }
```

### 3. Events vs State

**State** - Continuous data (email, password, loading)
**Events** - One-time actions (navigation, show snackbar)

```kotlin
// State - always visible
val isLoading: Boolean = true

// Event - happens once then cleared
sealed class LoginEvent {
    object NavigateToHome : LoginEvent()
}
```

### 4. viewModelScope

```kotlin
fun performLogin() {
    viewModelScope.launch {
        // Coroutine automatically cancelled when ViewModel is destroyed
        val result = repository.login(email, password)
    }
}
```

- Automatically manages coroutine lifecycle
- Cancelled when ViewModel is destroyed
- Prevents memory leaks

---

## Complete Example Flow

### User types email:

```
1. User types "test@email.com"
2. TextField calls: viewModel.onEmailChange("test@email.com")
3. ViewModel updates: _uiState.update { it.copy(email = "test@email.com") }
4. UI observes state change and recomposes
5. TextField displays new value
```

### User clicks login:

```
1. User clicks Button
2. Button calls: viewModel.onLoginClick()
3. ViewModel validates inputs
4. ViewModel sets: _uiState.update { it.copy(isLoading = true) }
5. UI shows loading indicator
6. ViewModel calls API
7. ViewModel sets: _events.value = LoginEvent.NavigateToHome
8. UI observes event and navigates
9. UI calls: viewModel.onEventHandled()
```

---

## File Structure

```
app/src/main/java/com/example/myapp/
├── data/
│   ├── model/          # Data classes
│   └── repository/     # Data sources
├── domain/
│   └── usecase/        # Business logic (optional)
└── ui/
    └── auth/
        ├── LoginScreen.kt          # Composable (View)
        ├── LoginViewModel.kt       # ViewModel
        ├── SignupScreen.kt
        └── SignupViewModel.kt
```

---

## Common Mistakes to Avoid

### ❌ DON'T: Put logic in Composables

```kotlin
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    
    Button(onClick = {
        // ❌ Validation logic in Composable
        if (email.isBlank()) {
            // Show error
        }
    })
}
```

### ✅ DO: Put logic in ViewModel

```kotlin
class LoginViewModel : ViewModel() {
    fun onLoginClick() {
        // ✅ Validation in ViewModel
        if (uiState.value.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Required") }
        }
    }
}
```

### ❌ DON'T: Access ViewModel in nested Composables

```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    Column {
        EmailField(viewModel) // ❌ Passing ViewModel down
    }
}
```

### ✅ DO: Pass only what's needed

```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        EmailField(
            email = uiState.email,
            onEmailChange = viewModel::onEmailChange
        ) // ✅ Pass data and callbacks
    }
}
```

---

## Testing ViewModels

ViewModels are easy to test because they don't depend on Android framework:

```kotlin
class LoginViewModelTest {
    @Test
    fun `when email is empty, show error`() {
        val viewModel = LoginViewModel()
        
        viewModel.onLoginClick()
        
        assertEquals("Email required", viewModel.uiState.value.emailError)
    }
}
```

---

## Summary

**MVVM in 3 Rules:**

1. **View (Composable)** - Displays data, calls ViewModel functions
2. **ViewModel** - Manages state, handles logic, exposes StateFlow
3. **Model** - Data classes, repositories, API calls

**Remember:**
- UI reads state, calls functions
- ViewModel manages state, handles logic
- Keep Composables dumb, ViewModels smart

---

## Your Auth Screens

I've created MVVM versions of your auth screens:

- `LoginScreen.kt` + `LoginViewModel.kt`
- `SignupScreenMVVM.kt` + `SignupViewModel.kt`
- `ForgotPasswordScreenMVVM.kt` + `ForgotPasswordViewModel.kt`

All logic is now in ViewModels, and Composables just display and interact!
