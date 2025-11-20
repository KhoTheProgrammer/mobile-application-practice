# Admin Module Setup Guide

## Problem: Infinite Recursion in RLS Policies

The infinite recursion error occurs when RLS policies reference the same table they're protecting, creating a circular dependency.

## Solution: Run Scripts in Order

### Step 1: Run the Main Admin Module Setup

Run `admin_module_setup.sql` in your Supabase SQL Editor. This creates:
- Admin tables (admin_profiles, admin_activity_logs, system_statistics)
- Adds admin user type support
- Creates initial RLS policies

### Step 2: Fix the RLS Policies

Run `fix_admin_policies.sql` to fix the infinite recursion issue. This script:
- Drops conflicting policies
- Recreates policies using `admin_profiles` table for checks (avoiding recursion)
- Ensures admin access works correctly

### Step 3: Create Your First Admin User

**Option A: Using Supabase Dashboard (Recommended)**

1. **Create Auth User**:
   - Go to Supabase Dashboard
   - Navigate to **Authentication** → **Users**
   - Click **"Add user"**
   - Enter email: `admin@donateeasy.com`
   - Enter a secure password
   - Click **"Create user"**
   - **Copy the UUID** from the user list

2. **Create Admin Profile**:
   - Open `create_admin_user_simple.sql`
   - Replace `'YOUR-ADMIN-UUID-HERE'` with the UUID you copied
   - Replace `'admin@donateeasy.com'` with your admin email
   - Run the script in SQL Editor

**Option B: Convert Existing User to Admin**

If you already have a user account:

```sql
-- 1. Find your user ID
SELECT id, email, user_type FROM profiles WHERE email = 'your-email@example.com';

-- 2. Convert to admin (replace the UUID)
DO $$
DECLARE
    existing_user_uuid UUID := 'YOUR-USER-UUID-HERE';
BEGIN
    UPDATE profiles 
    SET user_type = 'admin', verified = true, status = 'active'
    WHERE id = existing_user_uuid;

    INSERT INTO admin_profiles (id, role, permissions)
    VALUES (existing_user_uuid, 'super_admin', ARRAY['all'])
    ON CONFLICT (id) DO UPDATE 
    SET role = 'super_admin', permissions = ARRAY['all'];

    RAISE NOTICE 'User converted to admin successfully!';
END $$;
```

### Step 4: Verify Setup

Run this query to verify everything is set up correctly:

```sql
-- Check admin user
SELECT 
    p.id,
    p.email,
    p.user_type,
    p.status,
    p.verified,
    ap.role,
    ap.permissions
FROM profiles p
LEFT JOIN admin_profiles ap ON p.id = ap.id
WHERE p.user_type = 'admin';

-- Check policies
SELECT tablename, policyname, cmd
FROM pg_policies
WHERE tablename IN ('profiles', 'admin_profiles', 'donations')
ORDER BY tablename, policyname;
```

### Step 5: Test Admin Login

1. Build and run your Android app
2. Sign in with your admin credentials
3. You should be redirected to the Admin Dashboard
4. Test the features:
   - View dashboard statistics
   - Navigate to User Management
   - Navigate to Orphanage Verifications

## Troubleshooting

### Error: "infinite recursion detected in policy"

**Solution**: Run `fix_admin_policies.sql` to fix the RLS policies.

### Error: "Key (id)=(...) is not present in table users"

**Solution**: You must create the user in Supabase Auth first (via Dashboard), then create the profile.

### Error: "User not found after sign in"

**Solution**: Make sure the profile exists in the `profiles` table with `user_type = 'admin'`.

### Admin user can't access admin features

**Solution**: Verify the admin_profiles entry exists:

```sql
SELECT * FROM admin_profiles WHERE id = 'YOUR-ADMIN-UUID';
```

If it doesn't exist, create it:

```sql
INSERT INTO admin_profiles (id, role, permissions)
VALUES ('YOUR-ADMIN-UUID', 'super_admin', ARRAY['all']);
```

### Can't see any data in admin dashboard

**Solution**: The dashboard uses the `system_statistics` table. For now, it returns default values. To populate real statistics, you can create a function or manually insert data:

```sql
-- Insert current statistics
INSERT INTO system_statistics (
    stat_date,
    total_users,
    total_donors,
    total_orphanages,
    total_donations,
    total_donations_count,
    active_needs
)
SELECT
    CURRENT_DATE,
    (SELECT COUNT(*) FROM profiles),
    (SELECT COUNT(*) FROM profiles WHERE user_type = 'donor'),
    (SELECT COUNT(*) FROM profiles WHERE user_type = 'orphanage'),
    (SELECT COALESCE(SUM(amount), 0) FROM donations WHERE status = 'complicts
onfs for conicy definitiolw the ps
5. Revieall tableled on nabS is eck that RL. Che`
4files` and `prorsh `auth.useists in bote user exe th. Ensuries exist
3ics and polbleta all rify Ves
2.geror messailed ergs for deta Supabase loheCheck there:

1. t covered issues nocounter ou en
If y# Support
ons

# and donatistem usageports on syerate re- Geneports** in rate admts
5. **Creenortant evf imps oify adminions** - Not notificat **Add emaility logs
4.admin activ to view creen- Create a s** ogs UI audit lementImpl etc.
3. **m settings,tegement, sysory manateg- Cares** min featue addd mor
2. **An rolesadmion ons based ar permissient granullem Imp* -ermissions*ustomize pg:

1. **C is workin moduleminOnce your adSteps


## Next  scripts
QLg Sore runninabase befdatckup your ys balwas** - A changeoreup befd
5. **Backecteas expworking re ies a policLS Ensure Rgularly** -recies *Review poli_logs`
4. *ctivityadmin_aogged in ` ls aremin action- All adctivity**  ar admin3. **Monitods
orasswg, unique phave stronld  shouuntscoAdmin ac - asswords**rong p2. **Use strs
trusted usets for ccounate admin are* - Only ce powerful*ccounts ar **Admin ates

1.ty No## Securis

aturet admin fe[ ] Tes login
-  admin- [ ] Test app
d run thed an [ ] Buil
-stsr exiuseify admin `
- [ ] Vermple.sqlsidmin_user_create_and run `te a- [ ] UpdaUUID
e user  th Copy- [ ]d
ashboarse D in Supabaserte uCrea [ ] 
-sql`in_policies. `fix_adm [ ] Runp.sql`
-tusee_min_modul ] Run `ad

- [cklistSetup Che
## Quick 
eds;
```ve_neED.actieds = EXCLUDve_net,
    actiions_counonatD.total_dXCLUDE = Entations_coual_dontot,
    l_donationsXCLUDED.tota= E_donations otalges,
    torphanatotal_UDED.s = EXCLal_orphanage    tots,
.total_donorCLUDED= EXtal_donors     total_users,
CLUDED.toers = EX total_usE SET
    DO UPDATat_date)NFLICT (stive')
ON COtatus = 'acteds WHERE s FROM ne COUNT(*)CT
    (SELEons),FROM donatiNT(*) SELECT COU (  ,
 ed')let