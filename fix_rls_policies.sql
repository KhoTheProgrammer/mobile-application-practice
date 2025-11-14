-- Fix RLS Policies for User Registration
-- Run this in your Supabase SQL Editor

-- Drop existing restrictive policies for profiles
DROP POLICY IF EXISTS "Users can view their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON profiles;

-- Create new policies that allow user registration
CREATE POLICY "Users can view their own profile" ON profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can insert their own profile" ON profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update their own profile" ON profiles
    FOR UPDATE USING (auth.uid() = id);

-- Fix donor_profiles policies
DROP POLICY IF EXISTS "Donors can view their own profile" ON donor_profiles;
DROP POLICY IF EXISTS "Donors can update their own profile" ON donor_profiles;

CREATE POLICY "Donors can view their own profile" ON donor_profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Donors can insert their own profile" ON donor_profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Donors can update their own profile" ON donor_profiles
    FOR UPDATE USING (auth.uid() = id);

-- Fix orphanage_profiles policies
DROP POLICY IF EXISTS "Orphanages can update their own profile" ON orphanage_profiles;

CREATE POLICY "Orphanages can insert their own profile" ON orphanage_profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Orphanages can update their own profile" ON orphanage_profiles
    FOR UPDATE USING (auth.uid() = id);

-- Verify policies are created
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual
FROM pg_policies
WHERE tablename IN ('profiles', 'donor_profiles', 'orphanage_profiles')
ORDER BY tablename, policyname;
