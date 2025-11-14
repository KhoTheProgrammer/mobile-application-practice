# Fix Row Level Security (RLS) Issue

## The Problem
You're getting: **"new row violates row-level security policy for table 'profiles'"**

This means the RLS policies don't allow users to insert their own profiles during signup.

## Quick Fix (2 minutes)

### Step 1: Open Supabase SQL Editor
1. Go to your Supabase Dashboard: https://app.supabase.com
2. Select your project
3. Click **"SQL Editor"** in the left sidebar
4. Click **"New query"**

### Step 2: Run the Fix Script
1. Open the file `fix_rls_policies.sql` from your project
2. Copy ALL the SQL code
3. Paste it into the SQL Editor
4. Click **"Run"** button (or press Ctrl+Enter)

### Step 3: Verify
You should see a success message and a table showing the policies.

### Step 4: Test Again
1. Go back to your app
2. Try signing up again
3. Should work now! ✅

## What This Does

The fix adds **INSERT** policies that were missing:

### Before (❌ Broken):
- Users could only SELECT and UPDATE their profiles
- No INSERT policy = Can't create profile during signup

### After (✅ Fixed):
- Users can INSERT their own profile (during signup)
- Users can SELECT their own profile (view)
- Users can UPDATE their own profile (edit)

## Manual Fix (Alternative)

If you prefer to do it manually in the Supabase UI:

### For `profiles` table:
1. Go to **Authentication → Policies**
2. Find **profiles** table
3. Click **"New Policy"**
4. Choose **"For INSERT"**
5. Policy name: `Users can insert their own profile`
6. Target roles: `authenticated`
7. USING expression: `true`
8. WITH CHECK expression: `auth.uid() = id`
9. Click **"Save"**

### Repeat for `donor_profiles` and `orphanage_profiles`

## Verify Policies Are Working

After running the fix, you can verify in Supabase:

1. Go to **Authentication → Policies**
2. Check **profiles** table has these policies:
   - ✅ Users can view their own profile (SELECT)
   - ✅ Users can insert their own profile (INSERT) ← This was missing!
   - ✅ Users can update their own profile (UPDATE)

3. Check **donor_profiles** table has:
   - ✅ Donors can view their own profile (SELECT)
   - ✅ Donors can insert their own profile (INSERT) ← This was missing!
   - ✅ Donors can update their own profile (UPDATE)

4. Check **orphanage_profiles** table has:
   - ✅ Anyone can view orphanage profiles (SELECT)
   - ✅ Orphanages can insert their own profile (INSERT) ← This was missing!
   - ✅ Orphanages can update their own profile (UPDATE)

## Test Signup Flow

After fixing:

1. **Open your app**
2. **Click "Get Started"**
3. **Click "Sign Up"**
4. **Fill in:**
   - Full Name: Test User
   - Email: test@example.com
   - Password: password123
   - User Type: Donor
5. **Click "Sign Up"**
6. **Should work!** 🎉

## Troubleshooting

### Still getting RLS error?

1. **Check if SQL ran successfully:**
   - Look for green success message in SQL Editor
   - Check for any red error messages

2. **Verify policies exist:**
   - Go to Authentication → Policies
   - Make sure INSERT policies are listed

3. **Check auth.uid():**
   - The user must be authenticated
   - Email confirmation must be disabled
   - User should be logged in after signup

### Error: "permission denied for table profiles"?

This means RLS is working but policies aren't set up correctly. Run the fix script again.

### Error: "duplicate key value violates unique constraint"?

This means the user already exists. Try:
- Using a different email
- Or delete the existing user from Authentication → Users

## Why This Happened

The original `supabase_schema.sql` had RLS policies but was missing INSERT policies for user registration. This is a common oversight when setting up auth systems.

The fix adds the missing INSERT policies while keeping the security intact - users can only insert/update their own data, not others'.

## Security Note

These policies are secure because:
- ✅ Users can only insert rows where `id = auth.uid()` (their own ID)
- ✅ Users can only view/update their own data
- ✅ Users cannot access other users' data
- ✅ Anonymous users cannot do anything

Your data is still protected! 🔒
