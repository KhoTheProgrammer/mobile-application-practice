# Needs Implementation Plan

## Goal
Connect the orphanage "Update Needs" screen and donor home screen to use real data from the database instead of dummy data.

## Current State
- ✅ UpdateNeedsViewModel exists and is fully implemented with all CRUD operations
- ✅ NeedsRepository exists with all database operations
- ❌ UpdateNeedsScreen uses dummy data and doesn't use the ViewModel
- ❌ DonorHomeViewModel uses dummy orphanage data

## Changes Needed

### 1. Update UpdateNeedsScreen
**File:** `app/src/main/java/com/example/myapplication/ui/orphanage/UpdateNeeds.kt`
- Add `orphanageId` parameter
- Create ViewModel instance with orphanageId
- Replace dummy data with ViewModel state
- Connect all buttons to ViewModel functions
- Show loading/error states

### 2. Update NavGraph
**File:** `app/src/main/java/com/example/myapplication/navigation/NavGraph.kt`
- ✅ Pass orphanageId to UpdateNeedsScreen (already done)

### 3. Update DonorHomeViewModel
**File:** `app/src/main/java/com/example/myapplication/ui/donor/DonorHomeViewModel.kt`
- Load real orphanages from OrphanageRepository
- Load needs for each orphanage
- Remove dummy data

### 4. Update OrphanageRepository (if needed)
**File:** `app/src/main/java/com/example/myapplication/data/repository/OrphanageRepository.kt`
- Verify it loads orphanages with their needs
- Add method to get orphanages with needs if missing

## Implementation Steps

1. Update UpdateNeedsScreen to accept orphanageId and use ViewModel
2. Update DonorHomeViewModel to load real data
3. Test the flow:
   - Orphanage adds a need
   - Donor sees the need in their home screen
   - Donor can donate to that need
