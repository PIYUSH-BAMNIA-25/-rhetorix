# Testing Database Persistence - Quick Guide

## Before Testing

⚠️ **IMPORTANT**: If you already have the app installed, you need to **uninstall it first** to clear
the old broken database. Then reinstall the app with the fix.

## Test Scenario 1: Basic Persistence

1. Install and open the app
2. **Sign up** with test credentials:
    - Name: Test User
    - Email: test@example.com
    - Password: test123
    - Date of Birth: 01/01/2000
3. You should be automatically logged in and see the Main Menu
4. **Close the app completely** (swipe it away from recent apps)
5. **Reopen the app**
6. ✅ **Expected**: You should still be logged in (Main Menu appears)

## Test Scenario 2: Device Disconnect

1. With the app open and logged in
2. **Disconnect your device** from the computer
3. **Close the app completely**
4. **Wait 30 seconds**
5. **Open the app again**
6. ✅ **Expected**: You should still be logged in

## Test Scenario 3: Logout and Login

1. From Main Menu, tap **Logout**
2. You should see the Auth screen
3. **Close the app completely**
4. **Disconnect the device** (optional)
5. **Reconnect and open the app**
6. **Login** with the same credentials:
    - Email: test@example.com
    - Password: test123
7. ✅ **Expected**: Login should work! No "email and password mismatch" error

## Test Scenario 4: Debug Screen Verification

1. Login to the app
2. Go to Main Menu
3. Tap the **Debug** button (if available)
4. You should see a list of all users in the database
5. **Disconnect device**
6. **Close app**
7. **Reconnect and reopen app**
8. Go back to Debug screen
9. ✅ **Expected**: The same users should still be there

## Test Scenario 5: Multiple Users

1. Create first user account and login
2. Logout
3. **Sign up** a second user:
    - Name: Second User
    - Email: user2@example.com
    - Password: pass456
    - Date of Birth: 02/02/1995
4. Logout
5. **Close app completely**
6. **Disconnect device**
7. **Reopen app**
8. Try logging in as **first user** (test@example.com)
9. ✅ **Expected**: Should work!
10. Logout and try logging in as **second user** (user2@example.com)
11. ✅ **Expected**: Should work!

## Common Issues and Solutions

### Issue: Still seeing "email and password mismatch"

**Solution**: The old database with destructive migration might still be there.

1. Uninstall the app completely
2. Reinstall the app with the fix
3. Try again

### Issue: App crashes on startup

**Solution**: There might be a database migration issue.

1. Uninstall the app
2. Clear app data: Settings → Apps → Your App → Storage → Clear Data
3. Reinstall and test again

### Issue: Data disappears after app update

**Solution**: If you're updating the app via Android Studio:

1. Use "Run" instead of "Apply Changes"
2. Or uninstall first, then install the new version

## Using ADB to Verify Database Exists

If you want to verify the database file exists on the device:

```bash
# Connect to device
adb shell

# Navigate to your app's database directory
cd /data/data/com.runanywhere.startup_hackathon20/databases/

# List database files
ls -la

# You should see:
# - rhetorix_database
# - rhetorix_database-shm
# - rhetorix_database-wal
```

## Viewing Database Contents (Advanced)

If you want to inspect the database directly:

```bash
# Pull the database to your computer
adb pull /data/data/com.runanywhere.startup_hackathon20/databases/rhetorix_database .

# Open with SQLite browser or command line
sqlite3 rhetorix_database

# View users table
SELECT * FROM users;

# Exit
.quit
```

## Success Criteria ✅

Your database persistence is working correctly if:

- ✅ Users can signup and their data is saved
- ✅ Users can logout and login again with the same credentials
- ✅ Data persists after closing the app
- ✅ Data persists after disconnecting the device
- ✅ Multiple users can be created and all persist
- ✅ Debate history is saved and persists
- ✅ Debug screen shows all users consistently

## What If It Doesn't Work?

If persistence still isn't working after the fix:

1. Check if you uninstalled the old version first
2. Verify you're testing on a real device or emulator (not a simulator)
3. Check Android Studio logs for any database errors
4. Make sure you're not clearing app data between tests
5. Verify the fix was applied correctly in `RhetorixDatabase.kt`

---

**Remember**: The key change was removing `.fallbackToDestructiveMigration()` from the database
builder. This single line was causing all your data loss issues!
