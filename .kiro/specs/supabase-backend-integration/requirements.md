# Requirements Document

## Introduction

This specification defines the requirements for integrating Supabase as the backend service for the Donate Easy Android application. The Donate Easy App connects donors with orphanages, enabling donors to browse orphanages, offer donations, and track their contributions, while orphanages can manage their needs lists and coordinate with donors. The integration will provide authentication, real-time database operations, file storage for images, and real-time notifications.

## Glossary

- **Donate Easy App**: The Android mobile application that facilitates donations between donors and orphanages
- **Supabase Client**: The Kotlin/Android SDK that communicates with Supabase backend services
- **Authentication Service**: Supabase Auth module that handles user registration, login, and session management
- **Database Service**: Supabase PostgreSQL database accessed via REST API for storing application data
- **Storage Service**: Supabase Storage for managing uploaded images and documents
- **Realtime Service**: Supabase Realtime for push notifications and live data updates
- **Donor User**: A registered user who offers donations to orphanages
- **Orphanage User**: A verified representative of an orphanage who manages needs and receives donations
- **Donation Record**: A database entry representing an offered donation with status tracking
- **Needs List**: A collection of items that an orphanage currently requires
- **Verification Status**: The approval state of an orphanage (Pending, Verified, Rejected, Suspended)

## Requirements

### Requirement 1: Supabase Project Setup and Configuration

**User Story:** As a developer, I want to set up and configure a Supabase project, so that the Android app can connect to backend services securely

#### Acceptance Criteria

1. THE Donate Easy App SHALL initialize the Supabase Client with project URL and anonymous key during application startup
2. THE Donate Easy App SHALL store Supabase configuration credentials securely using Android BuildConfig or local.properties
3. THE Donate Easy App SHALL verify connectivity to the Supabase Client before allowing user operations
4. THE Donate Easy App SHALL handle network connectivity errors with appropriate user feedback messages
5. THE Donate Easy App SHALL implement connection retry logic with exponential backoff when the Supabase Client connection fails

### Requirement 2: User Authentication Implementation

**User Story:** As a user, I want to register and log in securely, so that I can access the app with my personal account

#### Acceptance Criteria

1. WHEN a new user submits registration details, THE Authentication Service SHALL create a user account with email and password
2. WHEN a user registers, THE Authentication Service SHALL send an email verification link to the provided email address
3. WHEN a user submits valid login credentials, THE Authentication Service SHALL authenticate the user and return a session token
4. WHEN a user requests password reset, THE Authentication Service SHALL send a password reset link via email
5. THE Authentication Service SHALL enforce password requirements of minimum 8 characters including special characters
6. WHEN a user session expires after 30 days of inactivity, THE Authentication Service SHALL automatically log out the user
7. THE Donate Easy App SHALL store the session token securely using Android EncryptedSharedPreferences

### Requirement 3: User Profile Management

**User Story:** As a user, I want to create and update my profile information, so that orphanages or donors can identify me

#### Acceptance Criteria

1. WHEN a Donor User completes registration, THE Database Service SHALL store profile data including name, email, phone, address, and preferences
2. WHEN an Orphanage User completes registration, THE Database Service SHALL store profile data including organization name, registration documents reference, photos reference, location, capacity, and admin contact
3. WHEN a user updates profile information, THE Database Service SHALL persist the changes with timestamp tracking
4. THE Database Service SHALL link user profiles to authentication user IDs using foreign key relationships
5. THE Database Service SHALL enforce data validation rules for required fields before persisting profile data

### Requirement 4: Orphanage Verification Workflow

**User Story:** As an administrator, I want to verify orphanage registrations, so that only legitimate organizations appear in the app

#### Acceptance Criteria

1. WHEN an Orphanage User registers, THE Database Service SHALL set the initial Verification Status to "Pending"
2. THE Database Service SHALL store uploaded verification documents using the Storage Service with secure access controls
3. WHEN an administrator approves an orphanage, THE Database Service SHALL update the Verification Status to "Verified"
4. WHEN an administrator rejects an orphanage, THE Database Service SHALL update the Verification Status to "Rejected" with reason
5. WHERE an orphanage is verified, THE Donate Easy App SHALL display a "Verified" badge on the orphanage profile

### Requirement 5: Orphanage Discovery and Browsing

**User Story:** As a donor, I want to browse and search for orphanages, so that I can find organizations to support

#### Acceptance Criteria

1. WHEN a Donor User opens the browse screen, THE Database Service SHALL retrieve all orphanages with Verification Status "Verified"
2. THE Database Service SHALL return orphanage data including name, location, description, photos, and current needs summary
3. WHEN a Donor User enters search text, THE Database Service SHALL filter orphanages by name, location, or needed items using full-text search
4. WHEN a Donor User applies category filters, THE Database Service SHALL return orphanages that have matching items in their Needs List
5. THE Database Service SHALL order search results by relevance score and last updated timestamp

### Requirement 6: Donation Creation and Management

**User Story:** As a donor, I want to create and submit donation offers, so that orphanages can review and accept my contributions

#### Acceptance Criteria

1. WHEN a Donor User submits a donation offer, THE Database Service SHALL create a Donation Record with status "Pending"
2. THE Database Service SHALL store donation details including item category, sub-category, size, condition, quantity, description, and delivery method
3. WHEN a Donor User uploads donation photos, THE Storage Service SHALL store up to 5 images per Donation Record with compression
4. THE Database Service SHALL link each Donation Record to the donor user ID and target orphanage ID using foreign keys
5. WHEN a Donor User saves a draft donation, THE Database Service SHALL persist the incomplete Donation Record with status "Draft"
6. WHEN a Donor User cancels a pending donation, THE Database Service SHALL update the Donation Record status to "Cancelled"

### Requirement 7: Donation Status Tracking

**User Story:** As a donor, I want to track the status of my donations, so that I know what happens with my contributions

#### Acceptance Criteria

1. WHEN a Donor User opens donation history, THE Database Service SHALL retrieve all Donation Records associated with the donor user ID
2. THE Database Service SHALL return donation status values: "Draft", "Pending", "Accepted", "Scheduled for Pickup", "Received", "Declined", "Cancelled"
3. WHEN an orphanage updates a Donation Record status, THE Realtime Service SHALL push a notification to the Donor User
4. THE Database Service SHALL record status change timestamps for audit trail purposes
5. THE Donate Easy App SHALL display donation history ordered by most recent status update first

### Requirement 8: Orphanage Needs List Management

**User Story:** As an orphanage representative, I want to maintain a list of current needs, so that donors know what items we require

#### Acceptance Criteria

1. WHEN an Orphanage User creates a need item, THE Database Service SHALL store the item with category, description, quantity needed, priority level, and urgency flag
2. WHEN an Orphanage User updates a need item, THE Database Service SHALL persist the changes with last modified timestamp
3. WHEN an Orphanage User deletes a need item, THE Database Service SHALL remove the record from the Needs List
4. THE Database Service SHALL link each need item to the orphanage ID using foreign key relationship
5. WHEN an Orphanage User marks the orphanage as "paused", THE Database Service SHALL set a flag preventing new donation offers

### Requirement 9: Donation Review and Acceptance

**User Story:** As an orphanage representative, I want to review and respond to donation offers, so that I can coordinate with donors

#### Acceptance Criteria

1. WHEN a new Donation Record is created for an orphanage, THE Realtime Service SHALL send a real-time notification to the Orphanage User
2. WHEN an Orphanage User opens pending donations, THE Database Service SHALL retrieve all Donation Records with status "Pending" for that orphanage
3. WHEN an Orphanage User accepts a donation, THE Database Service SHALL update the Donation Record status to "Accepted"
4. WHEN an Orphanage User declines a donation, THE Database Service SHALL update the Donation Record status to "Declined" with optional reason message
5. WHEN an Orphanage User marks a donation as received, THE Database Service SHALL update the Donation Record status to "Received" with timestamp

### Requirement 10: Real-time Notifications

**User Story:** As a user, I want to receive instant notifications about important events, so that I can respond promptly

#### Acceptance Criteria

1. WHEN a Donation Record status changes, THE Realtime Service SHALL broadcast the change to subscribed clients
2. THE Donate Easy App SHALL subscribe to Realtime Service channels for user-specific notifications during active sessions
3. WHEN a notification is received, THE Donate Easy App SHALL display an in-app notification banner with relevant details
4. THE Database Service SHALL store notification records for retrieval when users are offline
5. WHEN a user opens the notifications screen, THE Database Service SHALL retrieve unread notifications ordered by timestamp descending

### Requirement 11: Image Upload and Storage

**User Story:** As a user, I want to upload photos for donations and orphanage profiles, so that others can see visual information

#### Acceptance Criteria

1. WHEN a user selects an image for upload, THE Donate Easy App SHALL compress the image to reduce file size while maintaining acceptable quality
2. WHEN a user uploads an image, THE Storage Service SHALL store the file in a secure bucket with access control policies
3. THE Storage Service SHALL generate a public URL for each uploaded image that can be accessed by authenticated users
4. THE Database Service SHALL store image URLs as references in the relevant records (Donation Record or orphanage profile)
5. WHEN a Donation Record or profile is deleted, THE Storage Service SHALL remove associated image files to prevent orphaned data

### Requirement 12: Data Security and Access Control

**User Story:** As a user, I want my data to be secure and private, so that unauthorized users cannot access my information

#### Acceptance Criteria

1. THE Database Service SHALL implement Row Level Security policies that restrict data access based on authenticated user ID
2. THE Database Service SHALL ensure Donor Users can only read their own Donation Records and public orphanage data
3. THE Database Service SHALL ensure Orphanage Users can only modify their own profile and Needs List data
4. THE Storage Service SHALL enforce access policies that prevent unauthorized users from accessing uploaded files
5. THE Authentication Service SHALL use encrypted connections for all data transmission between the Donate Easy App and Supabase Client

### Requirement 13: Offline Support and Data Synchronization

**User Story:** As a user, I want to draft donations offline, so that I can work without internet connectivity

#### Acceptance Criteria

1. WHEN network connectivity is unavailable, THE Donate Easy App SHALL allow users to create draft Donation Records stored locally
2. WHEN network connectivity is restored, THE Donate Easy App SHALL synchronize local draft Donation Records to the Database Service
3. THE Donate Easy App SHALL cache recently viewed orphanage data for offline browsing
4. WHEN synchronization conflicts occur, THE Donate Easy App SHALL prioritize server data and notify the user of conflicts
5. THE Donate Easy App SHALL display connectivity status indicators to inform users of online/offline state

### Requirement 14: Analytics and Impact Tracking

**User Story:** As a user, I want to see my donation impact, so that I feel motivated to continue contributing

#### Acceptance Criteria

1. WHEN a Donor User opens their impact dashboard, THE Database Service SHALL calculate total items donated by counting completed Donation Records
2. THE Database Service SHALL calculate the number of unique orphanages helped by the Donor User
3. WHEN an Orphanage User opens analytics, THE Database Service SHALL aggregate donations received by category and time period
4. THE Database Service SHALL track monthly donation trends using timestamp-based queries
5. THE Donate Easy App SHALL display impact metrics with visual charts and summary statistics
