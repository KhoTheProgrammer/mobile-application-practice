# Supabase Storage Setup for Donation Images

## Overview
This guide explains how to set up a Supabase Storage bucket for storing donation images uploaded by donors.

## Steps to Create the Storage Bucket

### 1. Access Supabase Dashboard
1. Go to https://supabase.com/dashboard
2. Select your project
3. Navigate to **Storage** in the left sidebar

### 2. Create a New Bucket
1. Click **"New bucket"**
2. Enter the following details:
   - **Name**: `donation-images`
   - **Public bucket**: ✅ Enable (so images can be publicly accessed)
   - **File size limit**: 5 MB (recommended)
   - **Allowed MIME types**: `image/*` (only allow images)

3. Click **"Create bucket"**

### 3. Set Up Storage Policies (RLS)

#### Policy 1: Allow Authenticated Users to Upload
```sql
CREATE POLICY "Allow authenticated users to upload donation images"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (
  bucket_id = 'donation-images' AND
  (storage.foldername(name))[1] = 'donations'
);
```

#### Policy 2: Allow Public Read Access
```sql
CREATE POLICY "Allow public read access to donation images"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'donation-images');
```

#### Policy 3: Allow Users to Delete Their Own Images
```sql
CREATE POLICY "Allow users to delete their own donation images"
ON storage.objects FOR DELETE
TO authenticated
USING (
  bucket_id = 'donation-images' AND
  auth.uid()::text = (storage.foldername(name))[2]
);
```

### 4. Folder Structure
Images will be organized as follows:
```
donation-images/
└── donations/
    └── {donationId}/
        ├── {donationId}_{uuid1}.jpg
        ├── {donationId}_{uuid2}.jpg
        └── {donationId}_{uuid3}.jpg
```

## Usage in the App

### Upload Images
```kotlin
val storageRepository = StorageRepository()
val result = storageRepository.uploadDonationImages(
    context = context,
    imageUris = listOf(uri1, uri2, uri3),
    donationId = "donation-123"
)

when (result) {
    is StorageResult.Success -> {
        val imageUrls = result.data
        // Save URLs to database
    }
    is StorageResult.Error -> {
        // Handle error
    }
}
```

### Delete Images
```kotlin
val result = storageRepository.deleteDonationImages(donationId = "donation-123")
```

## Database Schema Update

Add an `images` column to the `donations` table to store image URLs:

```sql
ALTER TABLE donations
ADD COLUMN images TEXT[];
```

Or if you prefer JSONB:

```sql
ALTER TABLE donations
ADD COLUMN images JSONB DEFAULT '[]'::jsonb;
```

## Security Considerations

1. **File Size Limits**: Set appropriate limits to prevent abuse
2. **MIME Type Validation**: Only allow image uploads
3. **Authentication**: Only authenticated users can upload
4. **Folder Structure**: Organize by donation ID for easy management
5. **Cleanup**: Delete images when donations are cancelled/deleted

## Testing

1. Log in as a donor
2. Navigate to an orphanage detail page
3. Click "Donate Now"
4. Fill in the donation form
5. Upload 1-5 images
6. Submit the donation
7. Check the Supabase Storage dashboard to verify images were uploaded

## Troubleshooting

### Images not uploading
- Check that the bucket name matches: `donation-images`
- Verify RLS policies are enabled
- Check network connectivity
- Verify user is authenticated

### Images not displaying
- Ensure bucket is set to **public**
- Check the image URL format
- Verify CORS settings if accessing from web

## Next Steps

1. ✅ Storage bucket created
2. ✅ RLS policies configured
3. ✅ StorageRepository implemented
4. ⏳ Integrate with DonationFormViewModel
5. ⏳ Update DonationRepository to save image URLs
6. ⏳ Display images in ViewMyDonations screen
