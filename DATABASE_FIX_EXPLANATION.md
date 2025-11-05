# Database Persistence Fix

## Problem

Your app was losing all user data (login credentials, user profiles, debate history) whenever:

- The device was disconnected
- The app was reinstalled
- The app was updated
- The database schema changed

Users would login, disconnect their device, and then couldn't log back in because the database
said "email and password mismatch" - their data was completely gone!

## Root Cause

The issue was in `RhetorixDatabase.kt` at line 27:

```kotlin
.fallbackToDestructiveMigration() // This was DELETING all data!
```

### What `.fallbackToDestructiveMigration()` Does:

This Room Database configuration tells Android to **DELETE THE ENTIRE DATABASE** whenever:

- The database version changes
- The schema is modified
- There's any migration issue
- The app is reinstalled or updated

This is useful during development when you're constantly changing the database structure, but it's *
*TERRIBLE for production** because it wipes out all user data!

## The Solution

I removed the `.fallbackToDestructiveMigration()` call from the database configuration. Now:

✅ **Data persists permanently** in the device's internal storage
✅ **Users can disconnect and reconnect** without losing their accounts
✅ **Login credentials are preserved** between app sessions
✅ **Debate history and stats are saved** permanently

## How Room Database Works with Persistence

### Before (WITH destructive migration):

```
User signs up → Data stored → App closed/device disconnected 
→ Database deleted → User data GONE ❌
```

### After (WITHOUT destructive migration):

```
User signs up → Data stored → App closed/device disconnected 
→ Data remains safe → User can login again ✅
```

## Storage Location

The database is stored at:

```
/data/data/com.runanywhere.startup_hackathon20/databases/rhetorix_database
```

This location:

- Is **private** to your app (secure)
- **Persists** across app restarts
- **Survives** device disconnects
- Is **automatically backed up** (if backup is enabled in manifest)
- Only gets deleted when the user uninstalls the app or clears app data

## What About Future Database Changes?

If you need to change the database schema in the future (add new tables, modify columns, etc.), you
should:

1. **Increment the database version**:
   ```kotlin
   @Database(
       entities = [UserEntity::class, DebateHistoryEntity::class],
       version = 2, // Changed from 1 to 2
       exportSchema = false
   )
   ```

2. **Provide a migration strategy**:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           // SQL to update schema without losing data
           database.execSQL("ALTER TABLE users ADD COLUMN profile_picture TEXT")
       }
   }
   
   Room.databaseBuilder(...)
       .addMigrations(MIGRATION_1_2)
       .build()
   ```

## Testing the Fix

To verify the fix works:

1. **Sign up** with a new account
2. **Disconnect** the device from your computer
3. **Close** the app completely
4. **Reconnect** the device
5. **Open** the app
6. **Try logging in** with the same credentials
7. ✅ **Success!** You should be able to login

You can also check the Debug screen to see all users persisted in the database.

## Security Note

Currently, passwords are stored in **plain text** (not hashed). The comment in `UserEntity.kt`
acknowledges this:

```kotlin
val password: String, // In production, this should be hashed!
```

For a production app, you should:

- Hash passwords using **BCrypt** or **Argon2**
- Never store plain text passwords
- Use secure authentication tokens
- Consider adding encryption at rest

## Summary

✅ **Fixed**: Removed `.fallbackToDestructiveMigration()`
✅ **Result**: Database now persists permanently
✅ **Benefit**: Users can disconnect/reconnect without losing data
✅ **Status**: Ready for testing!
