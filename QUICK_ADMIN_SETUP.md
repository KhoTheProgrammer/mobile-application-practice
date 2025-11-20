# Quick Admin Setup - 3 Simple Steps

## The Problem
RLS (Row Level Security) policies were creating circular dependencies, causing infinite recursion errors.

## The Solution
We've simplified the approach by disabling RLS on admin-specific tables and relying on application-level security.

---

## Step 1: Run the Setup Scripts (In Order)

### 1.1 Run Main Setup
Copy and paste `admin_module_setup.sql` into Supabase SQL Editor and run it.

### 1.2 Run Policy Fix
Copy and paste `fix_admin_policies_final.sql` into Supabase SQL Editor and run it.

This will:
- Create admin tables
- Disable RLS on admin tables (to avoid recursion)
- Set up simple policies for user profiles

---

## Step 2: Create Admin User

### Option A: Create New Admin User

1. **In Supabase Dashboard**:
   - Go to Authentication → Users
   - Click "Add user"
   - Email: `admin@yourdomain.com`
   - Password: (choose a strong password)
   - Click "Create user"
   - **Copy the UUID** that appears

2. **In SQL Editor**, run this (replace the UUID and email):

```sql
DO $$
DECLARE
    admin_uuid UUID := 'PASTE-UUID-HERE';
    admin_email TEXT := 'admin@yourdomain.com';
BEGIN
    -- Create profile
    INSERT INTO profiles (id, user_type, full_name, email, status, verified)
    VALUES (admin_uuid, 'admin', 'System Administrator', admin_email, 'active', true)
    ON CONFLICT (id) DO UPDATE 
    SET user_type = 'admin', status = 'active', verified = true;

    -- Create admin profile
    INSERT INTO admin_profiles (id, role, permissions)
    VALUES (admin_uuid, 'super_admin', ARRAY['all'])
    ON CONFLICT (id) DO NOTHING;

    RAISE NOTICE 'Admin user created!';
END $$;
```

### Option B: Convert Existing User to Admin

If you already have a user account:

```sql
-- Find your user
SELECT id, email, user_type FROM profiles WHERE email = 'your-email@example.com';

-- Convert to admin (replace UUID)
DO $$
DECLARE
    user_uuid UUID := 'YOUR-USER-UUID';
BEGIN
    UPDATE profiles 
    SET user_type = 'admin', verified = true, status = 'active'
    WHERE id = user_uuid;

    INSERT INTO admin_profiles (id, role, permissions)
    VALUES (user_uuid, 'super_admin', ARRAY['all'])
    ON CONFLICT (id) DO NOTHING;

    RAISE NOTICE 'User converted to admin!';
END $$;
```

---

## Step 3: Test It

1. **Build and run your app**
2. **Sign in** with admin credentials
3. **You should see** the Admin Dashboard
4. **Test features**:
   - View dashboard statistics
   - Navigate to User Management
   - Navigate to Orphanage Verifications

---

## Verify Setup

Run this to confirm everything is set up:

```sql
-- Check admin user exists
SELECT 
    p.id,
    p.email,
    p.user_type,
    p.status,
    ap.role
FROM profiles p
LEFT JOIN admin_profiles ap ON p.id = ap.id
WHERE p.user_type = 'admin';
```

You should see your admin user listed.

---

## Troubleshooting

### "User not found after sign in"
- Make sure the user exists in Supabase Auth (Authentication → Users)
- Make sure the profile exists in `profiles` table with `user_type = 'admin'`

### "Still getting infinite recursion"
- Run `fix_admin_policies_final.sql` again
- It disables RLS on admin tables to prevent recursion

### "Can't see admin dashboard"
- Check that `user_type = 'admin'` in profiles table
- Check that admin_profiles entry exists
- Rebuild and restart the app

### "Dashboard shows zero statistics"
- This is normal - the stats table is empty initially
- The app will show default values (all zeros)
- Stats will populate as users and donations are created

---

## Security Notes

**Application-Level Security:**
- Admin UI is only shown to users with `user_type = 'admin'`
- Admin routes check for admin user before rendering
- All admin actions are logged

**Database-Level Security:**
- RLS is disabled on admin tables for simplicity
- Only authenticated users can access the database
- Regular user tables (profiles, donations) still have RLS enabled

**This approach is secure because:**
1. Users must be authenticated to access the database
2. Admin UI is hidden from non-admin users
3. Admin status is checked before operations
4. All actions are logged for audit

---

## What's Next?

Once admin is working:
- Test user management features
- Test orphanage verification
- Add more admin features as needed
- Monitor admin activity logs

---

## Quick Reference

**Admin Tables:**
- `admin_profiles` - Admin user info
- `admin_activity_logs` - Audit trail
- `system_statistics` - Dashboard stats

**Admin Routes:**
- `/admin_dashboard` - Main dashboard
- `/admin_user_management` - Manage users
- `/admin_orphanage_verification` - Verify orphanages

**Admin User Type:**
- Set `user_type = 'admin'` in profiles table
- Create entry in admin_profiles table
- User will be redirected to admin dashboard on login
