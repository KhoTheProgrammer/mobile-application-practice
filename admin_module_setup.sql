-- Admin Module Database Setup
-- This script adds admin functionality to the existing schema

-- ============================================
-- ADD ADMIN USER TYPE
-- ============================================

-- Update profiles table to include admin user type
ALTER TABLE profiles DROP CONSTRAINT IF EXISTS profiles_user_type_check;
ALTER TABLE profiles ADD CONSTRAINT profiles_user_type_check 
    CHECK (user_type IN ('donor', 'orphanage', 'admin'));

-- ============================================
-- ADMIN PROFILES TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS admin_profiles (
    id UUID PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'admin' CHECK (role IN ('admin', 'super_admin')),
    permissions TEXT[], -- Array of permission strings
    department VARCHAR(100),
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- ADMIN ACTIVITY LOGS
-- ============================================

CREATE TABLE IF NOT EXISTS admin_activity_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    admin_id UUID NOT NULL REFERENCES admin_profiles(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL, -- e.g., 'USER_SUSPENDED', 'DONATION_APPROVED', etc.
    target_type VARCHAR(50), -- e.g., 'USER', 'DONATION', 'ORPHANAGE'
    target_id UUID,
    description TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- USER MANAGEMENT ENHANCEMENTS
-- ============================================

-- Add status and verification fields to profiles
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'active' 
    CHECK (status IN ('active', 'suspended', 'banned', 'pending'));
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS verified BOOLEAN DEFAULT FALSE;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP WITH TIME ZONE;

-- Add verification status to orphanage profiles
ALTER TABLE orphanage_profiles ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) DEFAULT 'pending'
    CHECK (verification_status IN ('pending', 'verified', 'rejected'));
ALTER TABLE orphanage_profiles ADD COLUMN IF NOT EXISTS verification_notes TEXT;
ALTER TABLE orphanage_profiles ADD COLUMN IF NOT EXISTS verified_by UUID REFERENCES admin_profiles(id);
ALTER TABLE orphanage_profiles ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- SYSTEM STATISTICS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS system_statistics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    stat_date DATE NOT NULL UNIQUE,
    total_users INTEGER DEFAULT 0,
    total_donors INTEGER DEFAULT 0,
    total_orphanages INTEGER DEFAULT 0,
    total_donations DECIMAL(12, 2) DEFAULT 0.00,
    total_donations_count INTEGER DEFAULT 0,
    active_needs INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_admin_profiles_role ON admin_profiles(role);
CREATE INDEX IF NOT EXISTS idx_admin_activity_logs_admin_id ON admin_activity_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_admin_activity_logs_action_type ON admin_activity_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_admin_activity_logs_created_at ON admin_activity_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_profiles_status ON profiles(status);
CREATE INDEX IF NOT EXISTS idx_profiles_verified ON profiles(verified);
CREATE INDEX IF NOT EXISTS idx_orphanage_profiles_verification_status ON orphanage_profiles(verification_status);

-- ============================================
-- TRIGGERS
-- ============================================

CREATE TRIGGER update_admin_profiles_updated_at BEFORE UPDATE ON admin_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_statistics_updated_at BEFORE UPDATE ON system_statistics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- ROW LEVEL SECURITY
-- ============================================

ALTER TABLE admin_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_activity_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE system_statistics ENABLE ROW LEVEL SECURITY;

-- Admin profiles policies
CREATE POLICY "Admins can view all admin profiles" ON admin_profiles
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

CREATE POLICY "Admins can update their own profile" ON admin_profiles
    FOR UPDATE USING (auth.uid() = id);

-- Admin activity logs policies
CREATE POLICY "Admins can view all activity logs" ON admin_activity_logs
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

CREATE POLICY "Admins can create activity logs" ON admin_activity_logs
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

-- System statistics policies
CREATE POLICY "Admins can view system statistics" ON system_statistics
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

-- Update existing policies to allow admin access
CREATE POLICY "Admins can view all profiles" ON profiles
    FOR SELECT USING (
        auth.uid() = id OR 
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

CREATE POLICY "Admins can update any profile" ON profiles
    FOR UPDATE USING (
        auth.uid() = id OR 
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

CREATE POLICY "Admins can view all donations" ON donations
    FOR SELECT USING (
        auth.uid() = donor_id OR 
        EXISTS (
            SELECT 1 FROM orphanage_profiles 
            WHERE id = donations.orphanage_id AND id = auth.uid()
        ) OR
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

CREATE POLICY "Admins can update donations" ON donations
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

-- ============================================
-- FUNCTIONS FOR ADMIN DASHBOARD
-- ============================================

-- Function to get dashboard statistics
CREATE OR REPLACE FUNCTION get_admin_dashboard_stats()
RETURNS TABLE (
    total_users BIGINT,
    total_donors BIGINT,
    total_orphanages BIGINT,
    total_admins BIGINT,
    active_users BIGINT,
    suspended_users BIGINT,
    total_donations_amount DECIMAL,
    total_donations_count BIGINT,
    pending_donations BIGINT,
    active_needs BIGINT,
    verified_orphanages BIGINT,
    pending_orphanages BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM profiles)::BIGINT,
        (SELECT COUNT(*) FROM profiles WHERE user_type = 'donor')::BIGINT,
        (SELECT COUNT(*) FROM profiles WHERE user_type = 'orphanage')::BIGINT,
        (SELECT COUNT(*) FROM profiles WHERE user_type = 'admin')::BIGINT,
        (SELECT COUNT(*) FROM profiles WHERE status = 'active')::BIGINT,
        (SELECT COUNT(*) FROM profiles WHERE status = 'suspended')::BIGINT,
        (SELECT COALESCE(SUM(amount), 0) FROM donations WHERE status = 'completed')::DECIMAL,
        (SELECT COUNT(*) FROM donations)::BIGINT,
        (SELECT COUNT(*) FROM donations WHERE status = 'pending')::BIGINT,
        (SELECT COUNT(*) FROM needs WHERE status = 'active')::BIGINT,
        (SELECT COUNT(*) FROM orphanage_profiles WHERE verification_status = 'verified')::BIGINT,
        (SELECT COUNT(*) FROM orphanage_profiles WHERE verification_status = 'pending')::BIGINT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute permission to authenticated users (will be restricted by RLS)
GRANT EXECUTE ON FUNCTION get_admin_dashboard_stats() TO authenticated;

-- ============================================
-- SAMPLE ADMIN USER (OPTIONAL)
-- ============================================

-- To create an admin user:
-- 1. First create the user in Supabase Auth
-- 2. Then run the following (replace 'your-admin-user-uuid' with actual UUID):

/*
INSERT INTO profiles (id, user_type, full_name, email, status, verified)
VALUES ('your-admin-user-uuid', 'admin', 'System Administrator', 'admin@donateeasy.com', 'active', true);

INSERT INTO admin_profiles (id, role, permissions)
VALUES ('your-admin-user-uuid', 'super_admin', ARRAY['all']);
*/
