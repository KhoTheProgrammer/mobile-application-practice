# Logout Functionality Implementation

## Summary
Successfully implemented logout functionality across the application, allowing users to sign out and return to the login screen.

## Changes Made

### 1. Updated CustomAppBar Component
**File:** `app/src/main/java/com/example/myapplication/ui/components/CustomAppBar.kt`
- Added `showLogout` parameter to display logout button
- Added `onLogoutClick` callback for logout action
- Implemented logout confirmation dialog
- Logout button appears as a red logout icon in the app bar

### 2. Updated AuthViewModel
**File:** `app/src/main/java/com/example/myapplication/ui/auth/AuthViewModel.kt`
- Enhanced ViewModel to handle all authentication operations
- Added `logout()` function that calls `AuthRepository.signOut()`
- Added `signUp()` and `signIn()` functions for authentication
- Added `toggleSignUpMode()`, `clearError()`, and `setError()` helper functions
- Manages UI state including loading, errors, and sign-up mode
- Provides callbacks for success and error handling

### 3. Updated DonorHomeScreen
**File:** `app/src/main/java/com/example/myapplication/ui/donor/DonorsHome.kt`
- Added `onLogout` parameter to screen composable
- Integrated logout button in CustomAppBar
- Logout button appears in the top app bar

### 4. Updated OrphanageHomeScreen
**File:** `app/src/main/java/com/example/myapplication/ui/orphanage/OrphanageHome.kt`
- Added `onLogout` parameter to screen composable
- Integrated logout button in CustomAppBar
- Logout button appears in the top app bar

### 5. Updated Navigation Graph
**File:** `app/src/main/java/com/example/myapplication/navigation/NavGraph.kt`
- Created AuthViewModel instance in NavGraph
- Implemented logout logic for both DonorHome and OrphanageHome routes
- Logout clears the entire navigation stack and returns to login screen
- Uses `popUpTo(0) { inclusive = true }` to clear all previous screens

## How It Works

1. **User clicks logout icon** in the app bar (red logout icon)
2. **Confirmation dialog appears** asking "Are you sure you want to logout?"
3. **If confirmed:**
   - AuthViewModel calls `AuthRepository.signOut()`
   - Supabase session is cleared
   - Navigation clears the entire back stack
   - User is redirected to the login screen
4. **If cancelled:** Dialog closes, user stays on current screen

## Features

- ✅ Logout button visible on both Donor and Orphanage home screens
- ✅ Confirmation dialog prevents accidental logouts
- ✅ Proper session cleanup via Supabase
- ✅ Complete navigation stack clearing
- ✅ No way to navigate back after logout
- ✅ Error handling (navigates to login even if logout fails)

## Testing

To test the logout functionality:
1. Login as either a Donor or Orphanage user
2. Look for the red logout icon in the top-right corner of the app bar
3. Click the logout icon
4. Confirm the logout in the dialog
5. Verify you're redirected to the login screen
6. Try pressing the back button - you should not be able to return to the home screen

## Notes

- The logout button uses the Material Icons `Logout` icon
- The button is styled in red to indicate a destructive action
- The AuthRepository already had a `signOut()` method that we're now utilizing
- Navigation uses `popUpTo(0)` to clear the entire back stack, preventing users from navigating back after logout
