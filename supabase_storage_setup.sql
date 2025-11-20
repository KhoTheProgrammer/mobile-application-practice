-- Supabase Storage Setup for Donation Images
-- Execute this in your Supabase SQL Editor

-- Step 1: Create the donation-images storage bucket
-- Note: This needs to be done via the Supabase Dashboard UI:
-- 1. Go to Storage section
-- 2. Click "New bucket"
-- 3. Name: donation-images
-- 4. Public: Yes
-- 5. File size limit: 5 MB (5242880 bytes)
-- 6. Allowed MIME types: image/jpeg, image/png, image/jpg, image/webp

-- Step 2: Set up RLS policies for the storage bucket

-- Policy 1: Allow authenticated users to upload donation images
CREATE POLICY "Allow authenticated users to upload donation images"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (
  bucket_id = 'donation-images' AND
  (storage.foldername(name))[1] = 'donations'
);

-- Policy 2: Allow public read access to donation images
CREATE POLICY "Allow public read access to donation images"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'donation-images');

-- Policy 3: Allow users to delete their own donation images
CREATE POLICY "Allow users to delete their own donation images"
ON storage.objects FOR DELETE
TO authenticated
USING (
  bucket_id = 'donation-images' AND
  auth.uid()::text = (storage.foldername(name))[2]
);

-- Step 3: Add images column to donations table
ALTER TABLE donations
ADD COLUMN IF NOT EXISTS images TEXT[] DEFAULT '{}';

-- Add a comment to document the column
COMMENT ON COLUMN donations.images IS 'Array of image URLs for donation proof/receipts';

-- Verify the changes
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'donations' AND column_name = 'images';
