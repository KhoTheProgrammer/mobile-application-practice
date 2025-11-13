# Design Document: Supabase Backend Integration

## Overview

This design document outlines the architecture and implementation strategy for integrating Supabase as the backend service for the Donate Easy Android application. The integration will leverage Supabase's authentication, PostgreSQL database, storage, and real-time capabilities to provide a complete backend solution.

### Technology Stack

- **Frontend**: Kotlin, Jetpack Compose, Android SDK 24+
- **Backend**: Supabase (PostgreSQL, Auth, Storage, Realtime)
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Manual DI with Repository pattern
- **Networking**: Supabase Kotlin Client (supabase-kt)
- **Image Loading**: Coil (already integrated)
- **Local Storage**: EncryptedSharedPreferences for tokens, Room for offline caching

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  (Composables + ViewModels + UI State)                      │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                     Domain Layer                             │
│  (Use Cases + Domain Models + Repository Interfaces)        │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                     Data Layer                               │
│  (Repository Implementations + Data Sources)                 │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Supabase   │  │    Local     │  │   Mappers    │     │
│  │ Data Source  │  │ Data Source  │  │              │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                  Supabase Services                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │   Auth   │ │ Database │ │ Storage  │ │ Realtime │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

**Presentation Layer**
- Jetpack Compose UI components
- ViewModels managing UI state
- Navigation handling
- User input validation

**Domain Layer**
- Business logic and use cases
- Domain models (clean, framework-independent)
- Repository interfaces (contracts)

**Data Layer**
- Repository implementations
- Supabase data source (remote)
- Room data source (local cache)
- Data mappers (DTO ↔ Domain models)

## Components and Interfaces

### 1. Supabase Client Configuration

**SupabaseClient.kt**
```kotlin
object SupabaseClientProvider {
    private var client: SupabaseClient? = null
    
    fun initialize(context: Context) {
        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
    
    fun getClient(): SupabaseClient = client ?: throw IllegalStateException("...")
}
```

**Configuration Storage**
- Store credentials in `local.properties` (not committed to git)
- Expose via `BuildConfig` in `build.gradle.kts`

### 2. Authentication Module

**AuthRepository Interface**
```kotlin
interface AuthRepository {
    suspend fun signUp(email: String, password: String, userType: UserType): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun refreshSession(): Result<Unit>
    fun observeAuthState(): Flow<AuthState>
}
```

**AuthRepositoryImpl**
- Uses `supabase.auth` for all operations
- Stores session tokens in `EncryptedSharedPreferences`
- Emits auth state changes via Flow
- Handles automatic session refresh

**AuthState Sealed Class**
```kotlin
sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
```

### 3. User Profile Module

**UserRepository Interface**
```kotlin
interface UserRepository {
    suspend fun createProfile(profile: UserProfile): Result<UserProfile>
    suspend fun getProfile(userId: String): Result<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Result<UserProfile>
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String>
}
```

**Database Schema - users table**
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY REFERENCES auth.users(id),
    email TEXT NOT NULL,
    user_type TEXT NOT NULL CHECK (user_type IN ('DONOR', 'ORPHANAGE')),
    full_name TEXT NOT NULL,
    phone_number TEXT,
    profile_image_url TEXT,
    address JSONB,
    donor_preferences JSONB,
    orphanage_info JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

**Row Level Security (RLS) Policies**
```sql
-- Users can read their own profile
CREATE POLICY "Users can view own profile"
ON users FOR SELECT
USING (auth.uid() = id);

-- Users can update their own profile
CREATE POLICY "Users can update own profile"
ON users FOR UPDATE
USING (auth.uid() = id);
```

### 4. Orphanage Module

**OrphanageRepository Interface**
```kotlin
interface OrphanageRepository {
    suspend fun getAllVerifiedOrphanages(): Result<List<Orphanage>>
    suspend fun getOrphanageById(id: String): Result<Orphanage>
    suspend fun searchOrphanages(query: String, filters: SearchFilters): Result<List<Orphanage>>
    suspend fun updateOrphanageProfile(orphanage: Orphanage): Result<Orphanage>
    suspend fun updateVerificationStatus(id: String, status: VerificationStatus): Result<Unit>
    suspend fun uploadOrphanageImages(id: String, images: List<Uri>): Result<List<String>>
}
```

**Database Schema - orphanages table**
```sql
CREATE TABLE orphanages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    address JSONB NOT NULL,
    location GEOGRAPHY(POINT),
    contact_info JSONB,
    capacity INT,
    number_of_children INT,
    verification_status TEXT DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED')),
    verification_documents TEXT[],
    images TEXT[],
    is_accepting_donations BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_orphanages_verification ON orphanages(verification_status);
CREATE INDEX idx_orphanages_location ON orphanages USING GIST(location);
```

**RLS Policies**
```sql
-- Anyone can view verified orphanages
CREATE POLICY "Anyone can view verified orphanages"
ON orphanages FOR SELECT
USING (verification_status = 'VERIFIED');

-- Orphanage users can update their own profile
CREATE POLICY "Orphanages can update own profile"
ON orphanages FOR UPDATE
USING (user_id = auth.uid());
```

### 5. Needs List Module

**NeedsRepository Interface**
```kotlin
interface NeedsRepository {
    suspend fun createNeed(orphanageId: String, need: Need): Result<Need>
    suspend fun getNeedsByOrphanage(orphanageId: String): Result<List<Need>>
    suspend fun updateNeed(need: Need): Result<Need>
    suspend fun deleteNeed(needId: String): Result<Unit>
    fun observeNeeds(orphanageId: String): Flow<List<Need>>
}
```

**Database Schema - needs table**
```sql
CREATE TABLE needs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    orphanage_id UUID REFERENCES orphanages(id) ON DELETE CASCADE,
    category TEXT NOT NULL,
    item_name TEXT NOT NULL,
    description TEXT,
    quantity_needed INT NOT NULL,
    priority TEXT NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    is_urgent BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_needs_orphanage ON needs(orphanage_id);
CREATE INDEX idx_needs_category ON needs(category);
```

**RLS Policies**
```sql
-- Anyone can view active needs for verified orphanages
CREATE POLICY "Anyone can view active needs"
ON needs FOR SELECT
USING (
    is_active = TRUE AND
    EXISTS (
        SELECT 1 FROM orphanages
        WHERE orphanages.id = needs.orphanage_id
        AND orphanages.verification_status = 'VERIFIED'
    )
);

-- Orphanage owners can manage their needs
CREATE POLICY "Orphanages can manage own needs"
ON needs FOR ALL
USING (
    EXISTS (
        SELECT 1 FROM orphanages
        WHERE orphanages.id = needs.orphanage_id
        AND orphanages.user_id = auth.uid()
    )
);
```

### 6. Donation Module

**DonationRepository Interface**
```kotlin
interface DonationRepository {
    suspend fun createDonation(donation: Donation): Result<Donation>
    suspend fun getDonationById(id: String): Result<Donation>
    suspend fun getDonationsByDonor(donorId: String): Result<List<Donation>>
    suspend fun getDonationsByOrphanage(orphanageId: String): Result<List<Donation>>
    suspend fun updateDonationStatus(id: String, status: DonationStatus): Result<Unit>
    suspend fun uploadDonationImages(donationId: String, images: List<Uri>): Result<List<String>>
    fun observeDonations(userId: String, userType: UserType): Flow<List<Donation>>
}
```

**Database Schema - donations table**
```sql
CREATE TABLE donations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    donor_id UUID REFERENCES users(id) NOT NULL,
    orphanage_id UUID REFERENCES orphanages(id) NOT NULL,
    category TEXT NOT NULL,
    sub_category TEXT,
    item_name TEXT NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    condition TEXT,
    size TEXT,
    images TEXT[],
    delivery_method TEXT NOT NULL CHECK (delivery_method IN ('PICKUP', 'DROP_OFF')),
    pickup_address JSONB,
    preferred_pickup_date TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PENDING', 'ACCEPTED', 'SCHEDULED', 'RECEIVED', 'DECLINED', 'CANCELLED')),
    decline_reason TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    received_at TIMESTAMPTZ
);

CREATE INDEX idx_donations_donor ON donations(donor_id);
CREATE INDEX idx_donations_orphanage ON donations(orphanage_id);
CREATE INDEX idx_donations_status ON donations(status);
```

**RLS Policies**
```sql
-- Donors can view their own donations
CREATE POLICY "Donors can view own donations"
ON donations FOR SELECT
USING (donor_id = auth.uid());

-- Orphanages can view donations for their organization
CREATE POLICY "Orphanages can view their donations"
ON donations FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM orphanages
        WHERE orphanages.id = donations.orphanage_id
        AND orphanages.user_id = auth.uid()
    )
);

-- Donors can create and update their own donations
CREATE POLICY "Donors can manage own donations"
ON donations FOR INSERT
WITH CHECK (donor_id = auth.uid());

CREATE POLICY "Donors can update own donations"
ON donations FOR UPDATE
USING (donor_id = auth.uid());

-- Orphanages can update status of donations for their organization
CREATE POLICY "Orphanages can update donation status"
ON donations FOR UPDATE
USING (
    EXISTS (
        SELECT 1 FROM orphanages
        WHERE orphanages.id = donations.orphanage_id
        AND orphanages.user_id = auth.uid()
    )
);
```

### 7. Storage Module

**StorageRepository Interface**
```kotlin
interface StorageRepository {
    suspend fun uploadImage(bucket: String, path: String, imageUri: Uri): Result<String>
    suspend fun deleteImage(bucket: String, path: String): Result<Unit>
    fun getPublicUrl(bucket: String, path: String): String
}
```

**Storage Buckets**
- `profile-images`: User profile photos
- `orphanage-images`: Orphanage photos and verification documents
- `donation-images`: Donation item photos

**Bucket Policies**
- Authenticated users can upload to their own folders
- Public read access for profile and orphanage images
- Restricted access for verification documents

**Image Compression Strategy**
```kotlin
class ImageCompressor {
    fun compressImage(uri: Uri, maxWidth: Int = 1024, quality: Int = 80): ByteArray {
        // Load bitmap
        // Resize if needed
        // Compress to JPEG
        // Return byte array
    }
}
```

### 8. Realtime Notifications Module

**NotificationRepository Interface**
```kotlin
interface NotificationRepository {
    suspend fun createNotification(notification: Notification): Result<Notification>
    suspend fun getNotifications(userId: String): Result<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    fun observeNotifications(userId: String): Flow<List<Notification>>
}
```

**Database Schema - notifications table**
```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    data JSONB,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, created_at DESC);
```

**Realtime Subscription**
```kotlin
class RealtimeNotificationService {
    fun subscribeToNotifications(userId: String): Flow<Notification> {
        return supabase.realtime.channel("notifications:$userId")
            .postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "notifications"
                filter = "user_id=eq.$userId"
            }
            .map { it.decodeRecord<NotificationDto>().toDomain() }
    }
}
```

### 9. Analytics Module

**AnalyticsRepository Interface**
```kotlin
interface AnalyticsRepository {
    suspend fun getDonorImpact(donorId: String): Result<DonorImpact>
    suspend fun getOrphanageAnalytics(orphanageId: String, period: TimePeriod): Result<OrphanageAnalytics>
}
```

**Database Views**
```sql
-- Donor impact view
CREATE VIEW donor_impact AS
SELECT 
    donor_id,
    COUNT(*) as total_donations,
    COUNT(DISTINCT orphanage_id) as orphanages_helped,
    SUM(quantity) as total_items_donated
FROM donations
WHERE status = 'RECEIVED'
GROUP BY donor_id;

-- Orphanage analytics view
CREATE VIEW orphanage_analytics AS
SELECT 
    orphanage_id,
    DATE_TRUNC('month', received_at) as month,
    category,
    COUNT(*) as donation_count,
    SUM(quantity) as total_items
FROM donations
WHERE status = 'RECEIVED'
GROUP BY orphanage_id, month, category;
```

## Data Models

### Domain Models

**User.kt**
```kotlin
data class User(
    val id: String,
    val email: String,
    val userType: UserType
)
```

**Donation.kt**
```kotlin
data class Donation(
    val id: String,
    val donorId: String,
    val orphanageId: String,
    val category: String,
    val subCategory: String?,
    val itemName: String,
    val description: String,
    val quantity: Int,
    val condition: String?,
    val size: String?,
    val images: List<String>,
    val deliveryMethod: DeliveryMethod,
    val pickupAddress: Address?,
    val preferredPickupDate: Long?,
    val status: DonationStatus,
    val declineReason: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val receivedAt: Long?
)

enum class DonationStatus {
    DRAFT, PENDING, ACCEPTED, SCHEDULED, RECEIVED, DECLINED, CANCELLED
}

enum class DeliveryMethod {
    PICKUP, DROP_OFF
}
```

### DTOs (Data Transfer Objects)

**UserDto.kt** - Maps to Supabase `users` table
**OrphanageDto.kt** - Maps to Supabase `orphanages` table
**DonationDto.kt** - Maps to Supabase `donations` table

**Mapper Pattern**
```kotlin
interface Mapper<DTO, Domain> {
    fun toDomain(dto: DTO): Domain
    fun toDto(domain: Domain): DTO
}
```

## Error Handling

### Error Types

```kotlin
sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class AuthError(val message: String) : AppError()
    data class ValidationError(val field: String, val message: String) : AppError()
    data class DatabaseError(val message: String) : AppError()
    data class StorageError(val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}
```

### Result Wrapper

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: AppError) : Result<Nothing>()
}
```

### Error Handling Strategy

1. **Repository Layer**: Catch exceptions, map to AppError, return Result
2. **ViewModel Layer**: Handle Result, update UI state accordingly
3. **UI Layer**: Display error messages to user

## Testing Strategy

### Unit Tests

**Repository Tests**
- Mock Supabase client
- Test success and error scenarios
- Verify data mapping

**ViewModel Tests**
- Mock repositories
- Test state transitions
- Verify business logic

### Integration Tests

**Supabase Integration Tests**
- Use Supabase test project
- Test actual API calls
- Verify RLS policies

**End-to-End Tests**
- Test complete user flows
- Verify data persistence
- Test realtime updates

### Test Data

- Use Supabase seeding scripts for test data
- Create test users with known credentials
- Populate test orphanages and donations

## Performance Considerations

### Caching Strategy

1. **Memory Cache**: Recent orphanages and donations in ViewModel
2. **Disk Cache**: Room database for offline access
3. **Cache Invalidation**: Time-based (5 minutes) or event-based

### Pagination

```kotlin
interface PaginatedRepository {
    suspend fun getItems(page: Int, pageSize: Int): Result<PaginatedResult<T>>
}

data class PaginatedResult<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val hasMore: Boolean
)
```

### Image Optimization

- Compress images before upload (max 1024px, 80% quality)
- Use Coil for efficient image loading and caching
- Generate thumbnails on server side (Supabase Edge Functions)

### Database Optimization

- Create indexes on frequently queried columns
- Use database views for complex analytics queries
- Implement connection pooling

## Security Considerations

### Authentication Security

- Store tokens in EncryptedSharedPreferences
- Implement automatic token refresh
- Clear tokens on logout

### Data Security

- Enable RLS on all tables
- Validate user permissions in policies
- Sanitize user inputs

### Storage Security

- Restrict bucket access with policies
- Validate file types and sizes
- Scan uploaded files for malware (future enhancement)

### Network Security

- Use HTTPS for all connections
- Implement certificate pinning (optional)
- Validate SSL certificates

## Migration Strategy

### Phase 1: Setup and Authentication
- Configure Supabase project
- Implement authentication module
- Migrate existing auth screens

### Phase 2: Core Data Models
- Create database schema
- Implement repositories
- Add RLS policies

### Phase 3: Feature Integration
- Integrate orphanage browsing
- Implement donation flow
- Add needs management

### Phase 4: Realtime and Storage
- Implement notifications
- Add image upload
- Enable realtime updates

### Phase 5: Analytics and Polish
- Add analytics dashboard
- Implement offline support
- Performance optimization

## Dependencies

### Required Gradle Dependencies

```kotlin
// Supabase
implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
implementation("io.github.jan-tennert.supabase:auth-kt:2.0.0")
implementation("io.github.jan-tennert.supabase:storage-kt:2.0.0")
implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")
implementation("io.ktor:ktor-client-android:2.3.7")

// Security
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// Room (for offline caching)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// JSON
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
```

## Configuration Files

### local.properties
```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key
```

### build.gradle.kts
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("supabase.url")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("supabase.anon.key")}\"")
    }
    buildFeatures {
        buildConfig = true
    }
}
```

## Monitoring and Logging

### Logging Strategy

```kotlin
object AppLogger {
    fun logNetworkRequest(endpoint: String, params: Map<String, Any>)
    fun logNetworkResponse(endpoint: String, statusCode: Int, duration: Long)
    fun logError(error: AppError, context: String)
}
```

### Analytics Events

- User registration
- Donation created
- Donation status changed
- Orphanage viewed
- Search performed

## Future Enhancements

1. **Push Notifications**: Integrate Firebase Cloud Messaging
2. **Offline Mode**: Full offline support with sync
3. **Map Integration**: Google Maps for orphanage locations
4. **In-App Messaging**: Direct chat between donors and orphanages
5. **Payment Integration**: Support monetary donations
6. **Admin Dashboard**: Web-based admin panel for verification
7. **Multi-language Support**: Internationalization
8. **Advanced Analytics**: Machine learning for donation recommendations
