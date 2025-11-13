# Supabase Setup Guide

## 1. Create Supabase Project

1. Go to [Supabase](https://supabase.com) and sign up/login
2. Create a new project
3. Wait for the project to be provisioned

## 2. Set Up Database Schema

1. In your Supabase dashboard, go to **SQL Editor**
2. Open the `supabase_schema.sql` file from this project
3. Copy all the SQL content
4. Paste it into the SQL Editor
5. Click **Run** to execute the schema

## 3. Get Your Credentials

1. In your Supabase dashboard, go to **Settings** → **API**
2. You'll find two important values:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **anon/public key** (a long string starting with `eyJ...`)

## 4. Configure Your Android App

### Option 1: Using local.properties (Recommended)

1. Open `local.properties` in your project root
2. Add your Supabase credentials:

```properties
supabase.url=https://your-project-id.supabase.co
supabase.key=your-anon-public-key-here
```

3. Save the file

**Note:** `local.properties` is already in `.gitignore`, so your credentials won't be committed to version control.

### Option 2: Using Environment Variables (CI/CD)

For CI/CD pipelines, set these environment variables:
- `SUPABASE_URL`
- `SUPABASE_KEY`

## 5. Access Credentials in Code

The credentials are automatically loaded via `BuildConfig`:

```kotlin
import com.example.myapplication.BuildConfig

val supabaseUrl = BuildConfig.SUPABASE_URL
val supabaseKey = BuildConfig.SUPABASE_KEY
```

Or use the pre-configured client:

```kotlin
import com.example.myapplication.data.remote.SupabaseClient

val client = SupabaseClient.client
```

## 6. Test Your Connection

Build and run your app. The Supabase client will be initialized automatically.

## Security Best Practices

✅ **DO:**
- Keep your `local.properties` file private
- Use the anon/public key for client-side apps
- Enable Row Level Security (RLS) in Supabase (already configured in schema)
- Use Supabase Auth for user authentication

❌ **DON'T:**
- Commit `local.properties` to version control
- Use the service_role key in your mobile app
- Disable Row Level Security
- Store sensitive data without encryption

## Troubleshooting

### Build Error: "SUPABASE_URL not found"

Make sure you've added the credentials to `local.properties` and synced your Gradle files.

### Connection Error

1. Check your internet connection
2. Verify the Supabase URL is correct
3. Ensure your Supabase project is active
4. Check if the anon key is valid

### Database Errors

1. Make sure you've run the `supabase_schema.sql` script
2. Check the Supabase logs in your dashboard
3. Verify Row Level Security policies are correct

## Next Steps

1. Implement authentication using Supabase Auth
2. Create repository classes to interact with the database
3. Update ViewModels to use real data instead of mock data
4. Test all CRUD operations

## Useful Links

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Kotlin Client](https://github.com/supabase-community/supabase-kt)
- [Row Level Security Guide](https://supabase.com/docs/guides/auth/row-level-security)
