# Disable Email Confirmation in Supabase

## Issue
When signing up, you're getting "User ID not found" error. This happens because Supabase has email confirmation enabled by default, which means users need to verify their email before they can log in.

## Quick Fix: Disable Email Confirmation

### Steps:

1. **Go to your Supabase Dashboard**
   - Navigate to: https://app.supabase.com

2. **Select your project**
   - Click on your DonateEasy project

3. **Go to Authentication Settings**
   - Click on "Authentication" in the left sidebar
   - Click on "Providers" or "Settings"

4. **Disable Email Confirmation**
   - Look for "Email Auth" section
   - Find "Enable email confirmations" toggle
   - **Turn it OFF**
   - Click "Save"

### Alternative: Configure Email Confirmation

If you want to keep email confirmation enabled:

1. **In Supabase Dashboard:**
   - Go to Authentication → Email Templates
   - Configure the confirmation email template
   - Set up your email provider (SMTP)

2. **Update the App:**
   - Add email confirmation handling
   - Show "Check your email" message after signup
   - Add email verification screen

## Testing After Disabling

Once email confirmation is disabled:

1. **Try signing up again:**
   - Full Name: "Test User"
   - Email: "test@example.com"
   - Password: "password123"
   - User Type: Donor

2. **You should be able to:**
   - Sign up successfully
   - Be automatically logged in
   - Navigate to the home screen

3. **Then try signing in:**
   - Use the same credentials
   - Should work without issues

## Verify Settings

To confirm email confirmation is disabled:

1. Go to Supabase Dashboard
2. Authentication → Settings
3. Look for these settings:
   - ✅ "Enable email confirmations" should be OFF
   - ✅ "Enable email change confirmations" can be OFF for testing
   - ✅ "Secure email change" can be OFF for testing

## Production Considerations

For production, you should:
- ✅ Enable email confirmation
- ✅ Set up proper email templates
- ✅ Configure SMTP provider
- ✅ Add email verification UI in the app
- ✅ Handle unverified users appropriately

## Code Already Handles Both Cases

The updated `AuthRepository.signUp()` method now handles both scenarios:
- If email confirmation is disabled: Gets user ID immediately
- If email confirmation is enabled: Shows appropriate error message

```kotlin
val userId = authResult.user?.id ?: client.auth.currentUserOrNull()?.id 
    ?: throw Exception("User ID not found. Please check if email confirmation is required.")
```

## Troubleshooting

### Still getting "User ID not found"?

1. **Check Supabase logs:**
   - Go to Supabase Dashboard → Logs
   - Look for authentication errors

2. **Verify your credentials:**
   - Check `local.properties` has correct URL and key
   - Make sure you're using the anon/public key

3. **Check database:**
   - Go to Table Editor
   - Check if user was created in `auth.users`
   - Check if profile was created in `profiles` table

4. **Clear app data:**
   - Uninstall and reinstall the app
   - Or clear app data in Android settings

### Email confirmation is disabled but still not working?

Check if there are other auth settings:
- Go to Authentication → Settings
- Check "Site URL" is configured
- Check "Redirect URLs" if needed
- Verify RLS policies allow inserts

## Quick Test

After disabling email confirmation, run this test:

1. Open app
2. Click "Get Started"
3. Click "Sign Up"
4. Fill in details
5. Select user type
6. Click "Sign Up"
7. Should navigate to home screen immediately

If it works, you're all set! 🎉
