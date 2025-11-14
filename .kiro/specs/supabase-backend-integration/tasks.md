# Implementation Plan

- [ ] 1. Setup Supabase project and configure Android app
  - Create Supabase project at supabase.com and obtain project URL and anon key
  - Add Supabase Kotlin dependencies to `build.gradle.kts` (postgrest-kt, auth-kt, storage-kt, realtime-kt, ktor-client-android)
  - Add kotlinx-serialization plugin and dependencies for JSON handling
  - Configure `local.properties` with Supabase credentials (URL and anon key)
  - Update `build.gradle.kts` to expose Supabase credentials via BuildConfig
  - Enable BuildConfig feature in gradle
  - Create `SupabaseClientProvider` singleton object to initialize and provide Supabase client instance
  - Initialize Supabase client in `MainActivity.onCreate()` or Application class
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 2. Create database schema in Supabase
  - Create `users` table with columns: id (UUID, FK to auth.users), email, user_type, full_name, phone_number, profile_image_url, address (JSONB), donor_preferences (JSONB), orphanage_info (JSONB), created_at, updated_at
  - Create `orphanages` table with columns: id (UUID), user_id (FK to users), name, description, address (JSONB), location (GEOGRAPHY), contact_info (JSONB), capacity, number_of_children, verification_status, verification_documents (TEXT[]), images (TEXT[]), is_accepting_donations, created_at, updated_at
  - Create `needs` table with columns: id (UUID), orphanage_id (FK to orphanages), category, item_name, description, quantity_needed, priority, is_urgent, is_active, created_at, updated_at
  - Create `donations` table with columns: id (UUID), donor_id (FK to users), orphanage_id (FK to orphanages), category, sub_category, item_name, description, quantity, condition, size, images (TEXT[]), delivery_method, pickup_address (JSONB), preferred_pickup_date, status, decline_reason, created_at, updated_at, received_at
  - Create `notifications` table with columns: id (UUID), user_id (FK to users), type, title, message, data (JSONB), is_read, created_at
  - Add indexes on frequently queried columns (orphanages.verification_status, donations.donor_id, donations.orphanage_id, donations.status, notifications.user_id)
  - Create database views for analytics: `donor_impact` and `orphanage_analytics`
  - _Requirements: 3.4, 3.5, 4.1, 5.1, 6.1, 6.4, 8.1, 8.4, 9.1, 10.4_

- [ ] 3. Implement Row Level Security (RLS) policies
  - Enable RLS on all tables (users, orphanages, needs, donations, notifications)
  - Create policy: Users can view and update their own profile in `users` table
  - Create policy: Anyone can view verified orphanages in `orphanages` table
  - Create policy: Orphanage users can update their own orphanage profile
  - Create policy: Anyone can view active needs for verified orphanages in `needs` table
  - Create policy: Orphanage owners can manage their own needs
  - Create policy: Donors can view their own donations in `donations` table
  - Create policy: Orphanages can view donations for their organization
  - Create policy: Donors can create and update their own donations
  - Create policy: Orphanages can update donation status for their organization
  - Create policy: Users can view their own notifications
  - _Requirements: 12.1, 12.2, 12.3, 12.4_

- [ ] 4. Create storage buckets and policies
  - Create `profile-images` bucket in Supabase Storage for user profile photos
  - Create `orphanage-images` bucket for orphanage photos and verification documents
  - Create `donation-images` bucket for donation item photos
  - Configure bucket policies: authenticated users can upload to their own folders (path pattern: {user_id}/*)
  - Configure bucket policies: public read access for profile-images and orphanage-images buckets
  - Configure bucket policies: restricted access for verification documents (only orphanage owner and admins)
  - _Requirements: 11.2, 11.3, 11.4, 12.4_

- [ ] 5. Implement core data models and DTOs
  - Create `User` domain model with id, email, userType fields
  - Update `UserProfile` domain model to match requirements (already exists, verify fields)
  - Update `Orphanage` domain model to include verification_status, is_accepting_donations fields
  - Create `Need` domain model with id, orphanageId, category, itemName, description, quantityNeeded, priority, isUrgent, isActive fields
  - Create `Donation` domain model with all required fields (id, donorId, orphanageId, category, subCategory, itemName, description, quantity, condition, size, images, deliveryMethod, pickupAddress, preferredPickupDate, status, declineReason, timestamps)
  - Create `Notification` domain model with id, userId, type, title, message, data, isRead, createdAt fields
  - Create corresponding DTO classes for each domain model (UserDto, OrphanageDto, NeedDto, DonationDto, NotificationDto) with kotlinx.serialization annotations
  - Create mapper interfaces and implementations to convert between DTOs and domain models
  - Create enum classes: UserType, VerificationStatus, DonationStatus, DeliveryMethod, Priority
  - _Requirements: 3.1, 3.2, 4.1, 5.2, 6.1, 6.2, 8.1, 9.2, 10.4_

- [ ] 6. Implement Result wrapper and error handling
  - Create sealed class `Result<T>` with Success and Error cases
  - Create sealed class `AppError` with NetworkError, AuthError, ValidationError, DatabaseError, StorageError, UnknownError cases
  - Create extension functions to convert exceptions to AppError types
  - Create utility functions to wrap suspend functions with try-catch and return Result
  - _Requirements: 1.4_

- [ ] 7. Implement Authentication repository and data source
  - Create `AuthRepository` interface with methods: signUp, signIn, signOut, resetPassword, getCurrentUser, refreshSession, observeAuthState
  - Create `AuthRepositoryImpl` class implementing AuthRepository
  - Implement signUp method using `supabase.auth.signUpWith(Email)` and create user profile in users table
  - Implement signIn method using `supabase.auth.signInWith(Email)` and retrieve session
  - Implement signOut method using `supabase.auth.signOut()` and clear stored tokens
  - Implement resetPassword method using `supabase.auth.resetPasswordForEmail()`
  - Implement getCurrentUser method to retrieve current authenticated user from session
  - Implement refreshSession method using `supabase.auth.refreshCurrentSession()`
  - Implement observeAuthState method returning Flow<AuthState> that emits auth state changes
  - Create `TokenManager` class using EncryptedSharedPreferences to securely store and retrieve session tokens
  - Integrate TokenManager with AuthRepositoryImpl to persist tokens on login and clear on logout
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.6, 2.7, 12.5_

- [ ] 8. Implement User Profile repository
  - Create `UserRepository` interface with methods: createProfile, getProfile, updateProfile, uploadProfileImage
  - Create `UserRepositoryImpl` class implementing UserRepository
  - Implement createProfile method to insert user profile into `users` table using `supabase.postgrest["users"].insert()`
  - Implement getProfile method to fetch user profile by ID using `supabase.postgrest["users"].select().eq("id", userId).single()`
  - Implement updateProfile method to update user profile using `supabase.postgrest["users"].update().eq("id", userId)`
  - Implement uploadProfileImage method to compress image, upload to `profile-images` bucket, and return public URL
  - Create `ImageCompressor` utility class to compress images before upload (max 1024px width, 80% quality)
  - _Requirements: 3.1, 3.2, 3.3, 3.5, 11.1, 11.3, 11.4_

- [ ] 9. Implement Orphanage repository
  - Create `OrphanageRepository` interface with methods: getAllVerifiedOrphanages, getOrphanageById, searchOrphanages, updateOrphanageProfile, updateVerificationStatus, uploadOrphanageImages
  - Create `OrphanageRepositoryImpl` class implementing OrphanageRepository
  - Implement getAllVerifiedOrphanages method to fetch orphanages with verification_status = 'VERIFIED' using `supabase.postgrest["orphanages"].select().eq("verification_status", "VERIFIED")`
  - Implement getOrphanageById method to fetch single orphanage by ID
  - Implement searchOrphanages method with full-text search on name, description, and location fields, plus category filters
  - Implement updateOrphanageProfile method to update orphanage data
  - Implement updateVerificationStatus method to update verification_status field (admin function)
  - Implement uploadOrphanageImages method to upload multiple images to `orphanage-images` bucket and return URLs
  - _Requirements: 4.1, 4.2, 5.1, 5.2, 5.3, 5.4, 11.1, 11.2, 11.3, 11.4_

- [ ] 10. Implement Needs List repository
  - Create `NeedsRepository` interface with methods: createNeed, getNeedsByOrphanage, updateNeed, deleteNeed, observeNeeds
  - Create `NeedsRepositoryImpl` class implementing NeedsRepository
  - Implement createNeed method to insert need into `needs` table
  - Implement getNeedsByOrphanage method to fetch all needs for an orphanage
  - Implement updateNeed method to update need details
  - Implement deleteNeed method to delete need by ID
  - Implement observeNeeds method using Supabase Realtime to subscribe to changes on `needs` table filtered by orphanage_id
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 11. Implement Donation repository
  - Create `DonationRepository` interface with methods: createDonation, getDonationById, getDonationsByDonor, getDonationsByOrphanage, updateDonationStatus, uploadDonationImages, observeDonations
  - Create `DonationRepositoryImpl` class implementing DonationRepository
  - Implement createDonation method to insert donation into `donations` table with status 'DRAFT' or 'PENDING'
  - Implement getDonationById method to fetch single donation by ID
  - Implement getDonationsByDonor method to fetch all donations for a donor user
  - Implement getDonationsByOrphanage method to fetch all donations for an orphanage
  - Implement updateDonationStatus method to update donation status field and trigger notification
  - Implement uploadDonationImages method to upload up to 5 images to `donation-images` bucket
  - Implement observeDonations method using Supabase Realtime to subscribe to donation changes for a user
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 7.1, 7.2, 7.4, 9.1, 9.2, 9.3, 9.4, 9.5, 11.1, 11.2, 11.3, 11.4_

- [ ] 12. Implement Storage repository
  - Create `StorageRepository` interface with methods: uploadImage, deleteImage, getPublicUrl
  - Create `StorageRepositoryImpl` class implementing StorageRepository
  - Implement uploadImage method to compress image using ImageCompressor, upload to specified bucket and path using `supabase.storage[bucket].upload(path, data)`
  - Implement deleteImage method to delete file from storage using `supabase.storage[bucket].delete(path)`
  - Implement getPublicUrl method to generate public URL for a file using `supabase.storage[bucket].publicUrl(path)`
  - _Requirements: 11.1, 11.2, 11.3, 11.5_

- [ ] 13. Implement Notification repository and realtime service
  - Create `NotificationRepository` interface with methods: createNotification, getNotifications, markAsRead, observeNotifications
  - Create `NotificationRepositoryImpl` class implementing NotificationRepository
  - Implement createNotification method to insert notification into `notifications` table
  - Implement getNotifications method to fetch notifications for a user ordered by created_at DESC
  - Implement markAsRead method to update is_read field to true
  - Implement observeNotifications method using Supabase Realtime to subscribe to new notifications for a user
  - Create `RealtimeNotificationService` class to manage Realtime channel subscriptions
  - Implement subscription logic to listen for INSERT events on `notifications` table filtered by user_id
  - _Requirements: 7.3, 9.1, 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 14. Implement Analytics repository
  - Create `AnalyticsRepository` interface with methods: getDonorImpact, getOrphanageAnalytics
  - Create `AnalyticsRepositoryImpl` class implementing AnalyticsRepository
  - Implement getDonorImpact method to query `donor_impact` view for a donor's statistics
  - Implement getOrphanageAnalytics method to query `orphanage_analytics` view for an orphanage's donation trends
  - Create domain models: DonorImpact (totalDonations, orphanagesHelped, totalItemsDonated), OrphanageAnalytics (month, category, donationCount, totalItems)
  - _Requirements: 14.1, 14.2, 14.3, 14.4_

- [ ] 15. Create ViewModels for authentication screens
  - Update `LoginViewModel` to use AuthRepository for sign-in operations
  - Update `SignupViewModel` to use AuthRepository for sign-up operations and UserRepository for profile creation
  - Update `ForgotPasswordViewModel` to use AuthRepository for password reset
  - Update `ChangePasswordViewModel` to use AuthRepository for password change
  - Implement UI state classes for each ViewModel (LoginUiState, SignupUiState, etc.) with loading, success, error states
  - Implement proper error handling and user feedback for auth operations
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 16. Create ViewModels for donor screens
  - Create `OrphanageBrowseViewModel` to fetch and display verified orphanages using OrphanageRepository
  - Create `OrphanageDetailViewModel` to display orphanage details and needs using OrphanageRepository and NeedsRepository
  - Create `DonationFormViewModel` to handle donation creation using DonationRepository and StorageRepository for image uploads
  - Create `DonationHistoryViewModel` to display donor's donation history using DonationRepository
  - Implement search and filter functionality in OrphanageBrowseViewModel
  - Implement image upload handling in DonationFormViewModel with compression
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.5_

- [ ] 17. Create ViewModels for orphanage screens
  - Create `OrphanageHomeViewModel` to display orphanage dashboard and pending donations using DonationRepository
  - Create `NeedsManagementViewModel` to manage orphanage needs list using NeedsRepository
  - Create `DonationReviewViewModel` to review and accept/decline donations using DonationRepository
  - Implement realtime updates for new donation notifications in OrphanageHomeViewModel
  - Implement needs CRUD operations in NeedsManagementViewModel
  - Implement donation status update logic in DonationReviewViewModel
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 18. Update ProfileViewModel with Supabase integration
  - Update `ProfileViewModel` to use UserRepository for fetching and updating user profile
  - Implement profile image upload functionality using StorageRepository
  - Add support for both donor and orphanage profile types
  - Implement profile update with optimistic UI updates
  - _Requirements: 3.1, 3.2, 3.3, 11.1_

- [ ] 19. Create NotificationsViewModel
  - Create `NotificationsViewModel` to display user notifications using NotificationRepository
  - Implement realtime notification subscription using observeNotifications
  - Implement mark as read functionality
  - Display notifications grouped by date with unread indicators
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 20. Create AnalyticsViewModel
  - Create `DonorImpactViewModel` to display donor impact metrics using AnalyticsRepository
  - Create `OrphanageAnalyticsViewModel` to display orphanage analytics using AnalyticsRepository
  - Implement data visualization preparation (format data for charts)
  - Add time period filtering for analytics
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [ ] 21. Update authentication UI screens
  - Update `LoginScreen` to integrate with updated LoginViewModel and handle auth states
  - Update `SignupScreen` to integrate with updated SignupViewModel and collect user type selection
  - Update `ForgotPasswordScreen` to integrate with updated ForgotPasswordViewModel
  - Add email verification prompt after signup
  - Add loading indicators and error messages for all auth operations
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 22. Update donor UI screens
  - Update `DonorsHome` screen to display orphanages from OrphanageBrowseViewModel
  - Update `OrphanageDetail` screen to display orphanage details and needs from OrphanageDetailViewModel
  - Update `DonationForm` screen to integrate with DonationFormViewModel and handle image uploads
  - Update `ViewMyDonations` screen to display donation history from DonationHistoryViewModel
  - Add search and filter UI components to DonorsHome
  - Add image picker and preview in DonationForm
  - Add donation status badges and timeline in ViewMyDonations
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3, 7.5_

- [x] 23. Update orphanage UI screens
  - Update `OrphanageHome` screen to display dashboard with pending donations from OrphanageHomeViewModel
  - Update `UpdateNeeds` screen to integrate with NeedsManagementViewModel for CRUD operations
  - Update `ViewAllDonations` screen to display donations for orphanage from DonationReviewViewModel
  - Add donation review UI with accept/decline actions
  - Add realtime notification indicators for new donations
  - Add needs list management UI with add/edit/delete actions
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5_

- [ ] 24. Create NotificationsScreen
  - Create `NotificationsScreen` composable to display notifications from NotificationsViewModel
  - Implement notification list with unread indicators
  - Add pull-to-refresh functionality
  - Add mark as read on tap functionality
  - Display notification details with appropriate icons and formatting
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 25. Create analytics dashboard screens
  - Create `DonorImpactScreen` to display donor impact metrics from DonorImpactViewModel
  - Create `OrphanageAnalyticsScreen` to display orphanage analytics from OrphanageAnalyticsViewModel
  - Add visual charts for donation trends (use a charting library or custom composables)
  - Add summary cards for key metrics
  - Add time period selector for analytics
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [ ] 26. Implement offline support with local caching
  - Add Room database dependencies to build.gradle.kts
  - Create Room entities for caching: CachedOrphanage, CachedDonation, CachedNeed
  - Create Room DAOs for each entity with insert, update, delete, query methods
  - Create Room database class with version and migration strategy
  - Update repositories to check local cache first, then fetch from Supabase
  - Implement cache invalidation strategy (time-based: 5 minutes)
  - Implement sync logic to upload local drafts when connectivity is restored
  - Add connectivity status monitoring using ConnectivityManager
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 27. Update navigation graph
  - Update `NavGraph` to include new screens: NotificationsScreen, DonorImpactScreen, OrphanageAnalyticsScreen
  - Add navigation routes for all new screens
  - Implement deep linking for notifications
  - Add authentication state-based navigation (redirect to login if not authenticated)
  - _Requirements: 2.6_

- [ ] 28. Implement app initialization and dependency injection
  - Create Application class to initialize Supabase client on app startup
  - Create dependency injection container or manual DI setup for repositories
  - Initialize all repositories with Supabase client instance
  - Set up ViewModel factories to inject repositories
  - Add error logging and crash reporting initialization
  - _Requirements: 1.1, 1.2_

- [ ] 29. Add loading states and error handling UI
  - Create reusable loading indicator composable
  - Create reusable error message composable with retry action
  - Create reusable empty state composable
  - Update all screens to display loading, error, and empty states appropriately
  - Add snackbar or toast notifications for success/error messages
  - _Requirements: 1.4, 1.5_

- [ ] 30. Implement image upload with compression
  - Integrate ImageCompressor utility in all image upload flows
  - Add image picker using ActivityResultContracts
  - Add image preview before upload
  - Add upload progress indicators
  - Handle upload errors with retry logic
  - _Requirements: 11.1, 11.2, 11.3_

- [ ] 31. Add input validation
  - Create validation utility functions for email, password, phone number, required fields
  - Add validation to all form screens (signup, profile edit, donation form, needs form)
  - Display validation errors inline with form fields
  - Disable submit buttons until form is valid
  - _Requirements: 2.5, 3.5_

- [ ] 32. Implement session management
  - Add automatic session refresh logic in AuthRepository
  - Implement session expiry detection and auto-logout after 30 days of inactivity
  - Add session state persistence across app restarts
  - Implement token refresh on API 401 errors
  - _Requirements: 2.6, 2.7_

- [ ] 33. Add realtime updates to UI
  - Subscribe to realtime notifications in NotificationsViewModel
  - Subscribe to realtime donation updates in OrphanageHomeViewModel
  - Subscribe to realtime needs updates in OrphanageDetailViewModel
  - Display realtime update indicators in UI (e.g., "New donation received")
  - Handle realtime connection errors gracefully
  - _Requirements: 10.1, 10.2_

- [ ] 34. Implement search and filter functionality
  - Add search bar to DonorsHome screen
  - Implement debounced search input to avoid excessive API calls
  - Add filter chips for categories in DonorsHome
  - Implement filter logic in OrphanageBrowseViewModel
  - Add clear filters action
  - _Requirements: 5.2, 5.3, 5.4_

- [ ] 35. Add donation status tracking UI
  - Create donation status timeline component showing status progression
  - Add status badges with color coding (pending, accepted, received, etc.)
  - Add status change notifications
  - Display timestamps for each status change
  - _Requirements: 7.2, 7.3, 7.4_

- [ ] 36. Implement orphanage verification UI
  - Create verification status badge component
  - Display verification status on orphanage profiles
  - Add verification document upload for orphanage users during registration
  - Create admin verification screen (future enhancement placeholder)
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 37. Add user onboarding flow
  - Create onboarding screens for first-time users
  - Add user type selection screen (Donor vs Orphanage)
  - Add profile completion prompts after registration
  - Add app feature tour for new users
  - Store onboarding completion status in preferences
  - _Requirements: 2.1, 3.1_

- [ ] 38. Implement data synchronization
  - Create sync manager to handle background data sync
  - Implement conflict resolution strategy (server wins)
  - Add sync status indicators in UI
  - Implement manual sync trigger (pull-to-refresh)
  - Add sync error handling and retry logic
  - _Requirements: 13.2, 13.4_

- [ ] 39. Add performance optimizations
  - Implement pagination for orphanage list (load 20 items at a time)
  - Implement pagination for donation history
  - Add image caching with Coil
  - Optimize database queries with proper indexes
  - Add memory cache for frequently accessed data
  - Implement lazy loading for images in lists
  - _Requirements: 5.1, 7.1_

- [ ] 40. Add security enhancements
  - Verify all RLS policies are working correctly
  - Add input sanitization for user-generated content
  - Implement rate limiting on client side for API calls
  - Add certificate pinning for Supabase connections (optional)
  - Audit all data access points for security vulnerabilities
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 41. Create comprehensive error messages
  - Create user-friendly error messages for all error types
  - Add contextual help text for common errors
  - Implement error reporting to help users troubleshoot
  - Add retry actions for recoverable errors
  - Log errors for debugging purposes
  - _Requirements: 1.4_

- [ ] 42. Implement app settings screen
  - Create settings screen with user preferences
  - Add notification settings toggle
  - Add account management options (change password, delete account)
  - Add app version and about information
  - Add logout functionality
  - _Requirements: 2.3, 2.4_

- [ ] 43. Add accessibility features
  - Add content descriptions for all images and icons
  - Ensure proper contrast ratios for text
  - Add support for screen readers
  - Implement keyboard navigation support
  - Test with TalkBack and adjust as needed
  - _Requirements: General usability_

- [ ] 44. Create app documentation
  - Create README with setup instructions
  - Document Supabase schema and RLS policies
  - Create API documentation for repositories
  - Add inline code comments for complex logic
  - Create user guide for app features
  - _Requirements: General documentation_

- [ ] 45. Perform end-to-end testing
  - Test complete donor flow: browse orphanages, create donation, track status
  - Test complete orphanage flow: manage needs, review donations, update status
  - Test authentication flow: signup, login, logout, password reset
  - Test realtime notifications across devices
  - Test offline mode and data synchronization
  - Test image upload and display
  - Verify RLS policies prevent unauthorized access
  - Test error scenarios and recovery
  - _Requirements: All requirements_
