-- Add a general category for donations
-- This category will be used when donors submit items that don't fit into specific categories

INSERT INTO categories (id, name, icon_name, color, description, display_order, is_active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'General Donations',
    'category',
    '#9C27B0',
    'General donation items',
    0,
    true
)
ON CONFLICT (id) DO NOTHING;
