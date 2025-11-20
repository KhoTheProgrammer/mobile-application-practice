# Accept & Decline Buttons Fix - Summary

## ✅ Issue Resolved

The Accept and Decline buttons in the "All Donations" screen were not working because they had empty onClick handlers.

## 🔧 Changes Made

### 1. ViewAllDonationsViewModel
**File:** `app/src/main/java/com/example/myapplication/orphanage/domain/ViewAllDonationsViewModel.kt`

- ✅ Added `cancelDonation(donationId: String)` method
- Calls `donationRepository.cancelDonation(donationId)`
- Reloads donations and statistics after successful cancellation
- Handles errors and updates UI state

### 2. IncomingDonationCard Composable
**File:** `app/src/main/java/com/example/myapplication/orphanage/ui/ViewAllDonations.kt`

- ✅ Added `onAccept: () -> Unit` callback parameter
- ✅ Added `onDecline: () -> Unit` callback parameter
- ✅ Wired "Accept" button to call `onAccept`
- ✅ Wired "Decline" button to call `onDecline`

### 3. ViewAllDonationsScreen
**File:** `app/src/main/java/com/example/myapplication/orphanage/ui/ViewAllDonations.kt`

- ✅ Connected callbacks to ViewModel methods:
  - `onAccept = { viewModel.confirmDonation(donation.id) }`
  - `onDecline = { viewModel.cancelDonation(donation.id) }`

## 🎯 How It Works Now

### For Pending Donations:
1. **Accept Button** → Calls `confirmDonation()` → Updates status to CONFIRMED in Supabase
2. **Decline Button** → Calls `cancelDonation()` → Updates status to CANCELLED in Supabase
3. **Auto-refresh** → List reloads after each action to show updated status
4. **Statistics Update** → Donation statistics are recalculated

### User Flow:
1. Orphanage views "All Donations" screen
2. Sees pending donations with Accept/Decline buttons
3. Clicks "Accept" → Donation confirmed ✅
4. Clicks "Decline" → Donation cancelled ❌
5. List automatically refreshes with new status

## ✅ Status: WORKING

Both Accept and Decline buttons now properly update the donation status in Supabase and refresh the UI!
