-- DonateEasy Database Schema for Supabase
-- Created: 2025-11-13
-- This schema supports the DonateEasy mobile application

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- USERS & AUTHENTICATION
-- ============================================

-- User profiles table (extends Supabase auth.users)
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('donor', 'orphanage')),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Donor profiles
CREATE TABLE donor_profiles (
    id UUID PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    total_donations_made DECIMAL(10, 2) DEFAULT 0.00,
    total_donations_count INTEGER DEFAULT 0,
    preferred_categories TEXT[], -- Array of category IDs
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Orphanage profiles
CREATE TABLE orphanage_profiles (
    id UUID PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
    orphanage_name VARCHAR(255) NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    website VARCHAR(255),
    registration_number VARCHAR(100),
    number_of_children INTEGER DEFAULT 0,
    total_donations_received DECIMAL(10, 2) DEFAULT 0.00,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INTEGER DEFAULT 0,
    image_url TEXT,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- CATEGORIES
-- ============================================

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    icon_name VARCHAR(50) NOT NULL,
    color VARCHAR(20) NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Insert default categories
INSERT INTO categories (name, icon_name, color, description, display_order) VALUES
('Food', 'restaurant', '#FF6B6B', 'Food items and groceries', 1),
('Clothes', 'checkroom', '#4ECDC4', 'Clothing and footwear', 2),
('Furniture', 'chair', '#95E1D3', 'Furniture and household items', 3),
('Others', 'category', '#FFE66D', 'Other donation items', 4);

-- ============================================
-- NEEDS & REQUESTS
-- ============================================

CREATE TABLE needs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    orphanage_id UUID NOT NULL REFERENCES orphanage_profiles(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    quantity_fulfilled INTEGER DEFAULT 0,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    description TEXT,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'fulfilled', 'cancelled')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fulfilled_at TIMESTAMP WITH TIME ZONE
);

-- ============================================
-- DONATIONS
-- ============================================

CREATE TABLE donations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    donor_id UUID NOT NULL REFERENCES donor_profiles(id) ON DELETE CASCADE,
    orphanage_id UUID NOT NULL REFERENCES orphanage_profiles(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    need_id UUID REFERENCES needs(id) ON DELETE SET NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    donation_type VARCHAR(20) DEFAULT 'monetary' CHECK (donation_type IN ('monetary', 'in_kind')),
    item_description TEXT,
    quantity INTEGER,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'completed', 'cancelled')),
    note TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_frequency VARCHAR(20) CHECK (recurring_frequency IN ('weekly', 'monthly', 'quarterly', 'yearly')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

-- ============================================
-- PAYMENTS
-- ============================================

CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    donor_id UUID NOT NULL REFERENCES donor_profiles(id) ON DELETE CASCADE,
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('CREDIT_CARD', 'DEBIT_CARD', 'MOBILE_MONEY', 'BANK_TRANSFER', 'PAYPAL')),
    display_name VARCHAR(100) NOT NULL,
    last_four_digits VARCHAR(4),
    expiry_date VARCHAR(7), -- MM/YYYY format
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    donation_id UUID NOT NULL REFERENCES donations(id) ON DELETE CASCADE,
    payment_method_id UUID REFERENCES payment_methods(id) ON DELETE SET NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'completed', 'failed', 'refunded')),
    transaction_reference VARCHAR(255),
    payment_gateway VARCHAR(50),
    gateway_response JSONB,
    error_message TEXT,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- NOTIFICATIONS
-- ============================================

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(30) NOT NULL CHECK (notification_type IN ('DONATION_RECEIVED', 'DONATION_CONFIRMED', 'NEED_UPDATED', 'THANK_YOU_MESSAGE', 'SYSTEM_UPDATE', 'REMINDER')),
    related_id UUID, -- Can reference donation_id, orphanage_id, etc.
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    read_at TIMESTAMP WITH TIME ZONE
);

-- ============================================
-- FAVORITES
-- ============================================

CREATE TABLE favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    donor_id UUID NOT NULL REFERENCES donor_profiles(id) ON DELETE CASCADE,
    orphanage_id UUID NOT NULL REFERENCES orphanage_profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(donor_id, orphanage_id)
);

-- ============================================
-- RATINGS & REVIEWS
-- ============================================

CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    donor_id UUID NOT NULL REFERENCES donor_profiles(id) ON DELETE CASCADE,
    orphanage_id UUID NOT NULL REFERENCES orphanage_profiles(id) ON DELETE CASCADE,
    donation_id UUID REFERENCES donations(id) ON DELETE SET NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(donor_id, orphanage_id, donation_id)
);

-- ============================================
-- MESSAGES
-- ============================================

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    donation_id UUID REFERENCES donations(id) ON DELETE SET NULL,
    subject VARCHAR(255),
    message_text TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- INDEXES
-- ============================================

-- Profiles indexes
CREATE INDEX idx_profiles_user_type ON profiles(user_type);
CREATE INDEX idx_profiles_email ON profiles(email);

-- Orphanage profiles indexes
CREATE INDEX idx_orphanage_profiles_city ON orphanage_profiles(city);
CREATE INDEX idx_orphanage_profiles_state ON orphanage_profiles(state);
CREATE INDEX idx_orphanage_profiles_verified ON orphanage_profiles(verified);
CREATE INDEX idx_orphanage_profiles_rating ON orphanage_profiles(rating DESC);
CREATE INDEX idx_orphanage_profiles_location ON orphanage_profiles(latitude, longitude);

-- Needs indexes
CREATE INDEX idx_needs_orphanage_id ON needs(orphanage_id);
CREATE INDEX idx_needs_category_id ON needs(category_id);
CREATE INDEX idx_needs_status ON needs(status);
CREATE INDEX idx_needs_priority ON needs(priority);

-- Donations indexes
CREATE INDEX idx_donations_donor_id ON donations(donor_id);
CREATE INDEX idx_donations_orphanage_id ON donations(orphanage_id);
CREATE INDEX idx_donations_category_id ON donations(category_id);
CREATE INDEX idx_donations_status ON donations(status);
CREATE INDEX idx_donations_created_at ON donations(created_at DESC);

-- Payment transactions indexes
CREATE INDEX idx_payment_transactions_donation_id ON payment_transactions(donation_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(status);

-- Notifications indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);

-- Messages indexes
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX idx_messages_is_read ON messages(is_read);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);

-- ============================================
-- FUNCTIONS & TRIGGERS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to relevant tables
CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_donor_profiles_updated_at BEFORE UPDATE ON donor_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orphanage_profiles_updated_at BEFORE UPDATE ON orphanage_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_needs_updated_at BEFORE UPDATE ON needs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_donations_updated_at BEFORE UPDATE ON donations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_methods_updated_at BEFORE UPDATE ON payment_methods
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_transactions_updated_at BEFORE UPDATE ON payment_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to update orphanage rating
CREATE OR REPLACE FUNCTION update_orphanage_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE orphanage_profiles
    SET 
        rating = (SELECT AVG(rating)::DECIMAL(3,2) FROM reviews WHERE orphanage_id = NEW.orphanage_id),
        rating_count = (SELECT COUNT(*) FROM reviews WHERE orphanage_id = NEW.orphanage_id)
    WHERE id = NEW.orphanage_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_orphanage_rating_trigger
AFTER INSERT OR UPDATE ON reviews
FOR EACH ROW EXECUTE FUNCTION update_orphanage_rating();

-- Function to update donation totals
CREATE OR REPLACE FUNCTION update_donation_totals()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'completed' THEN
        -- Update orphanage total
        UPDATE orphanage_profiles
        SET total_donations_received = total_donations_received + NEW.amount
        WHERE id = NEW.orphanage_id;
        
        -- Update donor total
        UPDATE donor_profiles
        SET 
            total_donations_made = total_donations_made + NEW.amount,
            total_donations_count = total_donations_count + 1
        WHERE id = NEW.donor_id;
        
        -- Update need quantity if applicable
        IF NEW.need_id IS NOT NULL AND NEW.quantity IS NOT NULL THEN
            UPDATE needs
            SET 
                quantity_fulfilled = quantity_fulfilled + NEW.quantity,
                status = CASE 
                    WHEN quantity_fulfilled + NEW.quantity >= quantity THEN 'fulfilled'
                    ELSE status
                END,
                fulfilled_at = CASE 
                    WHEN quantity_fulfilled + NEW.quantity >= quantity THEN NOW()
                    ELSE fulfilled_at
                END
            WHERE id = NEW.need_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_donation_totals_trigger
AFTER UPDATE ON donations
FOR EACH ROW 
WHEN (OLD.status != 'completed' AND NEW.status = 'completed')
EXECUTE FUNCTION update_donation_totals();

-- ============================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================

-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE donor_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE orphanage_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE needs ENABLE ROW LEVEL SECURITY;
ALTER TABLE donations ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_methods ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

-- Profiles policies
CREATE POLICY "Users can view their own profile" ON profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile" ON profiles
    FOR UPDATE USING (auth.uid() = id);

-- Donor profiles policies
CREATE POLICY "Donors can view their own profile" ON donor_profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Donors can update their own profile" ON donor_profiles
    FOR UPDATE USING (auth.uid() = id);

-- Orphanage profiles policies
CREATE POLICY "Anyone can view orphanage profiles" ON orphanage_profiles
    FOR SELECT USING (true);

CREATE POLICY "Orphanages can update their own profile" ON orphanage_profiles
    FOR UPDATE USING (auth.uid() = id);

-- Categories policies
CREATE POLICY "Anyone can view categories" ON categories
    FOR SELECT USING (true);

-- Needs policies
CREATE POLICY "Anyone can view active needs" ON needs
    FOR SELECT USING (status = 'active');

CREATE POLICY "Orphanages can manage their own needs" ON needs
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM orphanage_profiles 
            WHERE id = needs.orphanage_id AND id = auth.uid()
        )
    );

-- Donations policies
CREATE POLICY "Donors can view their own donations" ON donations
    FOR SELECT USING (auth.uid() = donor_id);

CREATE POLICY "Orphanages can view donations to them" ON donations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM orphanage_profiles 
            WHERE id = donations.orphanage_id AND id = auth.uid()
        )
    );

CREATE POLICY "Donors can create donations" ON donations
    FOR INSERT WITH CHECK (auth.uid() = donor_id);

-- Payment methods policies
CREATE POLICY "Donors can manage their own payment methods" ON payment_methods
    FOR ALL USING (auth.uid() = donor_id);

-- Notifications policies
CREATE POLICY "Users can view their own notifications" ON notifications
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can update their own notifications" ON notifications
    FOR UPDATE USING (auth.uid() = user_id);

-- Favorites policies
CREATE POLICY "Donors can manage their own favorites" ON favorites
    FOR ALL USING (auth.uid() = donor_id);

-- Reviews policies
CREATE POLICY "Anyone can view reviews" ON reviews
    FOR SELECT USING (true);

CREATE POLICY "Donors can create reviews" ON reviews
    FOR INSERT WITH CHECK (auth.uid() = donor_id);

CREATE POLICY "Donors can update their own reviews" ON reviews
    FOR UPDATE USING (auth.uid() = donor_id);

-- Messages policies
CREATE POLICY "Users can view their own messages" ON messages
    FOR SELECT USING (auth.uid() = sender_id OR auth.uid() = recipient_id);

CREATE POLICY "Users can send messages" ON messages
    FOR INSERT WITH CHECK (auth.uid() = sender_id);

-- ============================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================

-- Uncomment below to insert sample data for testing

/*
-- Sample orphanage (you'll need to create the auth user first)
INSERT INTO orphanage_profiles (
    id, orphanage_name, description, address, city, state, country,
    latitude, longitude, contact_phone, contact_email, number_of_children, verified
) VALUES (
    'your-auth-user-uuid-here',
    'Hope Children Home',
    'A loving home for children in need',
    '123 Main Street',
    'Nairobi',
    'Nairobi County',
    'Kenya',
    -1.286389,
    36.817223,
    '+254712345678',
    'info@hopechildrenhome.org',
    45,
    true
);
*/
