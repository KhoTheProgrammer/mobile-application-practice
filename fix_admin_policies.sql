-- Fix Admin RLS Policies - Remove Infinite Recursion
-- Run this script to fix the policy issues

-- ============================================
-- STEP 1: Drop conflicting policies
-- ============================================

-- Drop the conflicting admin policies on profiles
DROP POLICY IF EXISTS "Admins can view all profiles" ON profiles;
DROP POLICY IF EXISTS "Admins can update any profile" ON profiles;
DROP POLICY IF EXISTS "Admins can view all donations" ON donations;
DROP POLICY IF EXISTS "Admins can update donations" ON donations;

-- ============================================
-- STEP 2: Create non-recursive admin policies
-- ============================================

-- For profiles table - use a simpler approach
-- Admins can view all profiles (using a direct check without recursion)
CREATE POLICY "Admins can view all profiles" ON profiles
    FOR SELECT USING (
        auth.uid() = id OR 
        EXISTS (
            SELECT 1 FROM admin_profiles 
            WHERE admin_profiles.id = auth.uid()
        )
    );

-- Admins can update any profile
CREATE POLICY "Admins can update any profile" ON profiles
    FOR UPDATE USING (
        auth.uid() = id OR 
        EXISTS (
            SELECT 1 FROM admin_profiles 
            WHERE admin_profiles.id = auth.uid()
        )
    );

-- For donations table
CREATE POLICY "Admins can view all donations" ON donations
    FOR SELECT USING (
        auth.uid() = donor_id OR 
        EXISTS (
            SELECT 1 FROM orphanage_profiles 
            WHERE id = donations.orphanage_id AND id = auth.uid()
        ) OR
        EXISTS (
            SELECT 1 FROM admin_profiles 
            WHERE admin_profiles.id = auth.uid()
        )
    );

CREATE POLICY "Admins can update donations" ON donations
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM admin_profiles 
            WHERE admin_profiles.id = auth.uid()
        )
    );

-- ============================================
-- STEP 3: Ensure admin_profiles policies are correct
-- ============================================

-- Drop existing admin_profiles policies
DROP POLICY IF EXISTS "Admins can view all admin profiles" ON admin_profiles;
DROP POLICY IF EXISTS "Admins can update their own profile" ON admin_profiles;

-- Recreate with correct logic
CREATE POLICY "Admins can view all admin profiles" ON admin_profiles
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM admin_profiles ap
            WHERE ap.id = auth.uid()
        )
    );

CREATE POLICY "Admins can update their own profile" ON admin_profiles
    FOR UPDATE USING (auth.uid() = id);

-- ============================================
-- STEP 4: Admin activity logs policies
-- ============================================

DROP POLICY IF EXISTS "Admins can view all activity logs" ON admin_activity_logs;
DROP POLICY IF EXISTS "Admins can create activity logs" ON admin_activity_logs;

CREATE POLICY "Admins can view all activity logs" ON admin_activity_logs
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM admin_profiles 
            WHERE admin_profiles.id = auth.uid()
        )
    );

CREATE POLICY "Admins can create activity logs" ON admin_activity_logs
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM admin_profiles 
            WHERE admin_profiles.id = auth.uid()
        )
    );

-- ============================================
-- STEP 5: System statistics policies
-- ============================================

DROP POLICY IF EXISTS "Admins can view system statistics" ON system_statistics;

CREATE POLICY "Admins can view system statistics" ON system_statistics
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM admin_profiles 
            WHERE admin_profiles.id = auth.uid()
        )
    );

-- ============================================
-- VERIFICATION
-- ============================================

-- Check all policies are created correctly
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual
FROM pg_policies
WHERE tablename IN ('profiles', 'admin_profiles', 'admin_activity_logs', 'donations', 'system_statistics')
ORDER BY tablename, policyname;
