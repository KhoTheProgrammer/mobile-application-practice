# Donation Form Database Integration Fix

## Problem
The "Donate Now" button in the donation form was not wired to the database. When users clicked "Submit Donation", it would navigate to the thank you screen without actually saving the donation to the database.

## What Was Fixed

### 1. Updated DonationForm Screen (`DonationForm.kt`)
- Added `viewModel` parameter to accept a `DonationFormViewModel`
- Added `itemDescription` state variable to capture donation details
- Added form validation before submission
- Added error handling and display
- Added loading state during submission
- Wired the submit button to call `viewModel.submitDonation()` instead of just navigating away
- Added `LaunchedEffect` to watch for successful donation creation and navigate only after success

### 2. Created DonationFormViewModelFactory (`DonationFormViewModel.kt`)
- Added `DonationFormViewModelFactory` class to properly instantiate the ViewModel with required parameters
- This factory is needed to pass orphanageId, orphanageName, and categoryId to the ViewModel

### 3. Updated Navigation (`NavGraph.kt`)
- Modified the `DonationForm` route to accept `orphanageId` and `orphanageName` as parameters
- Updated `Screen.DonationForm` to include route parameters
- Created ViewModel instance in the navigation composable
- Set the donor ID from the authenticated user
- Updated `OrphanageDetail` screen to pass orphanage data when navigating to donation form

### 4. Fixed Database Issues (`DonationRepository.kt`)
- Changed `donation_categories` to `categories` to match the actual database schema
- Created `CreateDonationDto` serializable class to fix "serializer for class Any not found" error
- Created `UpdateDonationStatusDto` serializable class for status updates
- Replaced `mutableMapOf<String, Any>` with proper serializable DTOs for Supabase operations

### 5. Added General Category SQL Script (`add_general_category.sql`)
- Created a SQL script to add a "General Donations" category
- This category is used as a default for all donations
- Run this script in your Supabase SQL editor to add the category

## How It Works Now

1. User clicks "Donate Now" on an orphanage detail page
2. Navigation passes the orphanage ID and name to the donation form
3. DonationForm creates a ViewModel with the orphanage details and current user ID
4. User fills out the form (category, subcategory, condition, description, pickup/dropoff)
5. User clicks "Submit Donation"
6. Form validates all required fields
7. ViewModel calls `DonationRepository.createDonation()` to save to database
8. On success, user is navigated to the thank you screen
9. On error, an error message is displayed

## Database Schema
The donation is saved to the `donations` table with:
- `donor_id`: Current authenticated user
- `orphanage_id`: Selected orphanage
- `category_id`: General category (or specific category if selected)
- `donation_type`: "in_kind" (physical items)
- `item_description`: Combined description from form fields
- `quantity`: Default 1
- `note`: Pickup/dropoff preference and address
- `status`: "pending" (awaiting orphanage confirmation)

## Next Steps

1. **Run the SQL script**: Execute `add_general_category.sql` in your Supabase SQL editor
2. **Test the flow**: 
   - Sign in as a donor
   - Browse orphanages
   - Click "Donate Now"
   - Fill out the form
   - Submit and verify the donation appears in the database
3. **Optional enhancements**:
   - Add image upload functionality (currently UI exists but not wired)
   - Map form categories to actual database categories
   - Add quantity input field
   - Add monetary donation option
   - Show donation confirmation with details

## Files Modified
- `app/src/main/java/com/example/myapplication/donor/ui/DonationForm.kt`
- `app/src/main/java/com/example/myapplication/donor/domain/DonationFormViewModel.kt`
- `app/src/main/java/com/example/myapplication/core/ui/NavGraph.kt`
- `app/src/main/java/com/example/myapplication/donor/data/DonationRepository.kt`

## Files Created
- `add_general_category.sql`
