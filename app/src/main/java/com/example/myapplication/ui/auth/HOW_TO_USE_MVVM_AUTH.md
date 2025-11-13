# How to Use the MVVM Auth Screens

## Quick Start

### 1. In your Navigation setup:

```kotlin
NavHost(navController = navController, startDestination = "login") {
    
    composable("login") {
        LoginScreen(
            // ViewModel is automatically created
            onNavigateToDonor = { navController.navigate("donor_home") },
            onNavigateToOrphanage = { navController.navigate("orphanage_home") },
            onNavigateToSignup = { navController.navigate("signup") },
            onNavigateToForgotPassword = { navController.navigate("forgot_password") }
        )
    }
    
    composable("signup") {
        SignupScreenMVVM(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLogin = { navController.navigate("login") },
            onSignupSuccess = { navController.navigate("donor_home") }
        )
    }
    
    composable("forgot_password") {
        ForgotPasswordScreenMVVM(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLogin = { navController.navigate("login") }
        )
    }
}
```

### 2. That's it! The ViewModels handle everything automatically.

---

## What Each File Does

### LoginScreen.kt (View)
- **What it does:** Displays the login UI
- **What it doesn't do:** Validation, API calls, state management
- **How it works:** 
  - Observes `uiState` from ViewModel
  - Calls ViewModel functions when user interacts
  - Reacts to events for navigation

### LoginViewModel.kt (Logic)
- **What it does:** 
  - Validates email and password
  - Manages loading state
  - Handles login logic
  - Triggers navigation events
- **What it doesn't do:** Display UI, handle navigation directly
- **How it works:**
  - Exposes `uiState` (email, password, errors, loading)
  - Exposes `events` (navigation, messages)
  - Provides functions for UI to call

---

## How Data Flows

### Example: User types email

```
┌─────────────────────────────────────────────────┐
│ 1. User types "test@email.com" in TextField    │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 2. TextField calls:                             │
│    viewModel.onEmailChange("test@email.com")    │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 3. ViewModel updates state:                     │
│    _uiState.update {                            │
│      it.copy(email = "test@email.com")          │
│    }                                            │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 4. UI observes state change via:                │
│    val uiState by viewModel.uiState             │
│                  .collectAsState()              │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 5. Composable recomposes with new value         │
│    TextField shows "test@email.com"             │
└─────────────────────────────────────────────────┘
```

### Example: User clicks Login

```
┌─────────────────────────────────────────────────┐
│ 1. User clicks "Sign In" button                 │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 2. Button calls: viewModel.onLoginClick()       │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 3. ViewModel validates:                         │
│    - validateEmail() → checks format            │
│    - validatePassword() → checks length         │
│    If invalid, updates emailError/passwordError │
└─────────────────┬───────────────────────────────┘
                  │ (if valid)
┌─────────────────▼───────────────────────────────┐
│ 4. ViewModel sets loading:                      │
│    _uiState.update { it.copy(isLoading = true) }│
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 5. UI shows CircularProgressIndicator           │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 6. ViewModel performs login (simulated)         │
│    delay(1500) // Simulate API call             │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 7. ViewModel triggers event:                    │
│    _events.value = LoginEvent.NavigateToDonor   │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 8. UI observes event in LaunchedEffect          │
│    Calls: onNavigateToDonor()                   │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│ 9. Navigation happens                           │
└─────────────────────────────────────────────────┘
```

---

## Key Points

### ✅ What the Composable DOES:
- Display UI elements
- Collect state: `val uiState by viewModel.uiState.collectAsState()`
- Call ViewModel functions: `onClick = viewModel::onLoginClick`
- Handle navigation based on events

### ❌ What the Composable DOESN'T DO:
- Validation logic
- State management
- API calls
- Business logic

### ✅ What the ViewModel DOES:
- Manage UI state
- Validate inputs
- Handle business logic
- Trigger events
- Call repositories/APIs

### ❌ What the ViewModel DOESN'T DO:
- Display UI
- Navigate directly
- Access Composable functions

---

## Customizing for Your Needs

### To add real API calls:

1. Create a Repository:
```kotlin
class AuthRepository {
    suspend fun login(email: String, password: String): Result<User> {
        // Your API call here
    }
}
```

2. Inject into ViewModel:
```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    
    private fun performLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = authRepository.login(
                email = uiState.value.email,
                password = uiState.value.password
            )
            
            when (result) {
                is Result.Success -> {
                    _events.value = LoginEvent.NavigateToDonor
                }
                is Result.Error -> {
                    _events.value = LoginEvent.ShowMessage(result.message)
                }
            }
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
```

### To add more validation:

```kotlin
private fun validatePassword(): Boolean {
    val password = _uiState.value.password
    return when {
        password.isBlank() -> {
            _uiState.update { it.copy(passwordError = "Password is required") }
            false
        }
        password.length < 8 -> {
            _uiState.update { it.copy(passwordError = "Must be at least 8 characters") }
            false
        }
        !password.any { it.isDigit() } -> {
            _uiState.update { it.copy(passwordError = "Must contain a number") }
            false
        }
        else -> {
            _uiState.update { it.copy(passwordError = null) }
            true
        }
    }
}
```

---

## Benefits You Get

1. **Easy Testing** - Test ViewModels without UI
2. **Configuration Changes** - State survives screen rotation
3. **Separation** - UI and logic are independent
4. **Reusability** - ViewModels can be shared
5. **Maintainability** - Easy to find and fix bugs

---

## Files Created

✅ `LoginViewModel.kt` - Login logic
✅ `LoginScreen.kt` - Login UI (MVVM version)
✅ `SignupViewModel.kt` - Signup logic
✅ `SignupScreenMVVM.kt` - Signup UI (MVVM version)
✅ `ForgotPasswordViewModel.kt` - Forgot password logic
✅ `ForgotPasswordScreenMVVM.kt` - Forgot password UI (MVVM version)

The old files (Login.kt, SignupScreen.kt, ForgotPasswordScreen.kt) still exist but have logic in Composables. Use the new MVVM versions instead!
