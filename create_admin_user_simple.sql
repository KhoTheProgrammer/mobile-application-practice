-- Simple Admin User Creation Script
-- Follow these steps in order

-- ============================================
-- STEP 1: First, create a user in Supabase Dashboard
-- ============================================
-- 1. Go to Supabase Dashboard → Authentication → Users
-- 2. Click "Add user" or "Invite user"
-- 3. Enter email and password
-- 4. Copy the UUID that appears
-- 5. Replace 'YOUR-ADMIN-UUID-HERE' below with that UUID

-- ============================================
-- STEP 2: Run this query to create admin profile
-- ============================================

-- IMPORTANT: Replace 'YOUR-ADMIN-UUID-HERE' with the actual UUID from Supabase Auth
-- IMPORTANT: Replace 'admin@donateeasy.com' with the actual email you used

DO $$
DECLARE
    admin_uuid UUID := 'YOUR-ADMIN-UUID-HERE'; -- REPLACE THIS
    admin_email TEXT := 'admin@donateeasy.com'; -- REPLACE THIS
BEGIN
    -- Check if user exists in auth.users
    IF NOT EXISTS (SELECT 1 FROM auth.users WHERE id = admin_uuid) THEN
        RAISE EXCEPTION 'User with UUID % does not exist in auth.users. Please create the user in Supabase Dashboard first.', admin_uuid;
    END IF;

    -- Create profile
    INSERT INTO profiles (id, user_type, full_name, email, status, verified)
    VALUES (admin_uuid, 'admin', 'System Administrator', admin_email, 'active', true)
    ON CONFLICT (id) DO UPDATE 
    SET user_type = 'admin', status = 'active', verified = true;

    -- Create admin profile
    INSERT INTO admin_profiles (id, role, permissions)
    VALUES (admin_uuid, 'super_admin', ARRAY['all'])
    ON CONFLICT (id) DO UPDATE 
    SET role = 'super_admin', permissions = ARRAY['all'];

    RAISE NOTICE 'Admin user created successfully!';
END $$;

-- ============================================
-- STEP 3: Verify the admin user was created
-- ============================================

SELECT 
    p.id,
    p.email,
    p.user_type,
    p.status,
    p.verified,
    ap.role,
    ap.permissions
FROM profiles p
LEFT JOIN admin_profiles ap ON p.id = ap.id
WHERE p.user_type = 'admin';

-- ============================================
-- ALTERNATIVE: Convert existing user to admin
-- ============================================

-- If you want to convert an existing user to admin instead:
-- 1. First, find your existing user:

-- SELECT id, email, user_type FROM profiles WHERE email = 'your-email@example.com';

-- 2. Then run this (replace the UUID):

/*
DO $$
DECLARE
    existing_user_uuid UUID := 'EXISTING-USER-UUID-HERE'; -- REPLACE THIS
BEGIN
    -- Update profile to admin
    UPDATE profiles 
    SET user_type = 'admin', verified = true, status = 'active'
    WHERE id = existing_user_uuid;

    -- Create admin profile
    INSERT INTO admin_profiles (id, role, permissions)
    VALUES (existing_user_uuid, 'super_admin', ARRAY['all'])
    ON CONFLICT (id) DO UPDATE 
    SET role = 'super_admin', permissions = ARRAY['all'];

    RAISE NOTICE 'User converted to admin successfully!';
END $$;
*/
