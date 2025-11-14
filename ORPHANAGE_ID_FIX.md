# Orphanage ID Fix

## Problem
The OrphanageHomeScreen was receiving a placeholder string `"placeholder-orphanage-id"` instead of the actual logged-in user's UUID, causing a database error:
```
invalid input syntax for type uuid "placeholder-orphanage-id" code:22P02
```

## Solution
Updated the authentication flow to track and provide the current user's ID from the Supabase session.

## Changes Made

### 1. Updated AuthUiState
**File:** `app/src/main/java/com/example/myapplication/ui/auth/AuthViewModel.kt`
- Added `currentUserId: String?` field to track the logged-in user's ID
- This ID is populated from the AuthResult when signing in or signing up

### 2. Enhanced AuthViewModel
**File:** `app/src/main/java/com/example/myapplication/ui/auth/AuthViewModel.kt`
- Added `init` block that calls `loadCurrentUser()` on ViewModel creation
- Added `loadCurrentUser()` private function to fetch current user from AuthRepository
- Added `getCurrentUserId()` public function to retrieve the current user ID
- Updated `signIn()` to store the user ID in state after successful login
- Updated `signUp()` to store the user ID in state after successful registration
- Updated `logout()` to clear the user ID from state

### 3. Updated NavGraph
**File:** `app/src/main/java/com/example/myapplication/navigation/NavGraph.kt`
- Replaced hardcoded `"placeholder-orphanage-id"` with `authViewModel.getCurrentUserId()`
- The orphanage ID is now dynamically retrieved from the authenticated user's session

## How It Works

1. **On Login/Signup**: When a user successfully logs in or signs up, the AuthViewModel stores their user ID from the AuthResult
2. **On Navigation**: When navigating to OrphanageHome, the NavGraph retrieves the current user ID from AuthViewModel
3. **On Logout**: When logging out, the user ID is cleared from the state
4. **On App Restart**: The ViewModel's init block loads the current user from Supabase session

## Benefits

- ✅ Uses actual UUID from Supabase authentication
- ✅ No more database UUID validation errors
- ✅ Proper user session management
- ✅ Works across app restarts (loads from Supabase session)
- ✅ Automatically cleared on logout

## Testing

To verify the fix:
1. Login as an orphanage user
2. Navigate to the orphanage home screen
3. The screen should now load without UUID errors
4. The correct user ID is being used for database queries
