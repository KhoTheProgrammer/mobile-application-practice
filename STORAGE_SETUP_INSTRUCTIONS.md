# Supabase Storage Setup Instructions

## Quick Setup Guide

Follow these steps to complete the storage setup for donation images:

### Step 1: Create Storage Bucket (via Dashboard)

1. Go to your Supabase Dashboard: https://supabase.com/dashboard
2. Select your project
3. Navigate to **Storage** in the left sidebar
4. Click **"New bucket"**
5. Configure the bucket:
   - **Name**: `donation-images`
   - **Public bucket**: ✅ **Enable** (important for public image access)
   - **File size limit**: `5242880` (5 MB)
   - **Allowed MIME types**: 
     - `image/jpeg`
     - `image/png`
     - `image/jpg`
     - `image/webp`
6. Click **"Create bucket"**

### Step 2: Execute SQL Script

1. In your Supabase Dashboard, go to **SQL Editor**
2. Click **"New query"**
3. Copy the contents of `supabase_storage_setup.sql`
4. Paste into the SQL editor
5. Click **"Run"** or press `Ctrl+Enter`

This will:
- Create RLS policies for the storage bucket
- Add the `images` column to the `donations` table

### Step 3: Verify Setup

Run this query in the SQL Editor to verify:

```sql
-- Check if images column exists
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'donations' AND column_name = 'images';

-- Check storage policies
SELECT * FROM pg_policies 
WHERE tablename = 'objects' 
AND schemaname = 'storage';
```

### Expected Folder Structure

Images will be organized as:
```
donation-images/
└── donations/
    └── {donationId}/
        ├── {donationId}_{uuid1}.jpg
        ├── {donationId}_{uuid2}.jpg
        └── {donationId}_{uuid3}.jpg
```

### Security Features

✅ Only authenticated users can upload  
✅ Public read access for displaying images  
✅ Users can only delete their own images  
✅ 5 MB file size limit  
✅ Only image files allowed  

### Next Steps

After completing the setup:
1. Test image upload from the app
2. Verify images appear in Storage dashboard
3. Check that image URLs are saved in the donations table
4. Test image display in the app

### Troubleshooting

**Bucket creation fails:**
- Make sure you're using a unique bucket name
- Check that you have admin permissions

**Policies not working:**
- Ensure RLS is enabled on storage.objects
- Verify the bucket name matches exactly: `donation-images`

**Images not uploading:**
- Check user authentication status
- Verify MIME types are correct
- Check file size is under 5 MB

**Images not displaying:**
- Ensure bucket is set to public
- Verify the image URL format
- Check CORS settings if needed
