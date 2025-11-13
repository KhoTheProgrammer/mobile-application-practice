# Testing Supabase Connection

## Quick Test Guide

Your app is now configured to test the Supabase connection on startup!

### What I've Set Up:

1. **Test Screen** - A beautiful UI that shows connection test results
2. **Automatic Tests** - Tests configuration, connection, and database access
3. **Auto-Launch** - App opens directly to the test screen

### How to Test:

1. **Build and Run the App**
   ```bash
   # In Android Studio, click the Run button or press Shift+F10
   ```

2. **The Test Screen Will Show:**
   - ✅ Configuration check (credentials loaded)
   - ✅ Connection check (client initialized)
   - ✅ Database check (can query tables)

3. **Click "Run Tests" Button**
   - Tests will run automatically
   - You'll see real-time results with icons:
     - 🟢 Green checkmark = Success
     - 🔴 Red X = Failed
     - 🔵 Blue spinner = Running

### What Each Test Does:

#### Test 1: Configuration ⚙️
- Checks if SUPABASE_URL and SUPABASE_KEY are loaded
- Validates URL format
- Shows first 30 characters of your URL

#### Test 2: Connection 🔌
- Initializes the Supabase client
- Verifies it can connect to your project
- Shows the connected URL

#### Test 3: Database 🗄️
- Attempts to query the `categories` table
- Verifies Row Level Security is working
- Tests if schema is properly set up

### Expected Results:

#### ✅ All Tests Pass:
```
✓ Configuration - Credentials loaded
✓ Connection - Client initialized  
✓ Database - Query successful
```
**You're ready to go!** Change the start destination back to "landing" in MainActivity.

#### ❌ Database Test Fails:
```
✓ Configuration - Credentials loaded
✓ Connection - Client initialized
✗ Database - Table not found
```
**Action needed:** Run `supabase_schema.sql` in your Supabase dashboard

#### ❌ Connection Test Fails:
```
✓ Configuration - Credentials loaded
✗ Connection - Connection failed
```
**Action needed:** Check internet connection and verify credentials

### After Testing:

Once all tests pass, update `MainActivity.kt` to start with the landing page:

```kotlin
NavGraph(
    navController = navController,
    startDestination = "landing"  // Change from "supabase_test"
)
```

### Troubleshooting:

**"Missing credentials" error:**
- Check `local.properties` has both URL and key
- Sync Gradle files
- Clean and rebuild project

**"Table not found" error:**
1. Go to Supabase dashboard
2. Open SQL Editor
3. Copy content from `supabase_schema.sql`
4. Run the SQL script

**"Permission denied" error:**
- Row Level Security is blocking access
- Check RLS policies in Supabase dashboard
- Verify you're using the anon key (not service_role)

### View Logs:

You can also check Logcat for detailed logs:
```
Filter: SupabaseTest
```

The original `SupabaseTest.testConnection()` still logs to Logcat for debugging.

### Files Created:

- ✅ `SupabaseTestScreen.kt` - Beautiful test UI
- ✅ `SupabaseTestViewModel.kt` - Test logic (in same file)
- ✅ Updated `NavGraph.kt` - Added test route
- ✅ Updated `MainActivity.kt` - Auto-launch test screen

### Next Steps:

1. Run the app
2. Click "Run Tests"
3. Verify all tests pass
4. If database test fails, run the SQL schema
5. Change start destination back to "landing"
6. Start building your features!

---

**Pro Tip:** Keep the test screen accessible by adding a button in your settings or developer menu for future testing!
