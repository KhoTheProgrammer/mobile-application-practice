# Sign-In Debug Guide

## Issue
When signing in, the screen flashes and resets the input fields without navigating or showing an error.

## Changes Made to Help Debug

### 1. Added Input Validation
**File:** `app/src/main/java/com/example/myapplication/ui/auth/AuthScreen.kt`
- Added validation for empty email and password fields
- Shows clear error messages for missing inputs
- Prevents sign-in attempt if fields are blank

### 2. Enhanced Error Display
**File:** `app/src/main/java/com/example/myapplication/ui/auth/AuthScreen.kt`
- Error messages now appear in a colored card (red background)
- More prominent and easier to see
- Uses medium font weight for better visibility

### 3. Added Logging
**File:** `app/src/main/java/com/example/myapplication/ui/auth/AuthViewModel.kt`
- Added Android Log statements to track sign-in flow
- Logs when sign-in starts, succeeds, or fails
- Includes user ID and type on success
- Includes error messages on failure

## How to Debug

### Step 1: Check Logcat
Run the app and try to sign in. Then check Android Studio's Logcat for messages tagged with "AuthViewModel":

```
# Look for these log messages:
D/AuthViewModel: Attempting sign in for: [email]
D/AuthViewModel: Sign in successful, user ID: [id], type: [DONOR/ORPHANAGE]
# OR
E/AuthViewModel: Sign in failed: [error message]
# OR
E/AuthViewModel: Sign in exception: [exception message]
```

### Step 2: Check for Error Messages
After attempting sign-in, look for a red error card above the sign-in button showing:
- "Please enter your email"
- "Please enter your password"
- Any authentication errors from Supabase

### Step 3: Common Issues and Solutions

#### Issue: "Please enter your email/password"
**Solution:** Make sure you've filled in both fields before clicking sign in.

#### Issue: "Profile not found. Please sign up first."
**Solution:** The email/password combination doesn't exist. Try signing up first.

#### Issue: "Invalid login credentials"
**Solution:** The password is incorrect for that email.

#### Issue: No error shown, just resets
**Possible causes:**
1. Network issue - check internet connection
2. Supabase configuration issue - verify SUPABASE_URL and SUPABASE_KEY in local.properties
3. Database issue - check if profiles table exists and has data

### Step 4: Verify Test Credentials
Try signing in with credentials you know exist:
1. First, try signing up a new account
2. Then try signing in with those same credentials
3. Check Logcat for the full error message

### Step 5: Check Supabase Dashboard
1. Go to your Supabase project dashboard
2. Navigate to Authentication > Users
3. Verify the user exists
4. Check Table Editor > profiles to see if the profile was created

## Expected Behavior

### Successful Sign-In Flow:
1. User enters email and password
2. Clicks "Sign In" button
3. Button shows loading spinner
4. Logcat shows: "Attempting sign in for: [email]"
5. Logcat shows: "Sign in successful, user ID: [id], type: [type]"
6. App navigates to DonorHome or OrphanageHome based on user type

### Failed Sign-In Flow:
1. User enters email and password
2. Clicks "Sign In" button
3. Button shows loading spinner briefly
4. Logcat shows: "Sign in failed: [error]"
5. Red error card appears with the error message
6. User can try again

## Next Steps

1. **Run the app** and try to sign in
2. **Check Logcat** immediately after the flash/reset
3. **Look for error messages** in the red card
4. **Share the log output** if you need further help

The logs will tell us exactly what's happening during the sign-in process.
