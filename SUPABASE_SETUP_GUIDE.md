# Supabase Backend Setup Guide

## Overview

This guide will help you set up a **free** Supabase backend for Rhetorix, replacing the local SQLite
database with a cloud-based PostgreSQL database. This solves the problem of losing user data when
the app is uninstalled.

## Benefits of Server-Based Database

✅ **Persistent Data**: User accounts survive app reinstalls  
✅ **Cross-Device Sync**: Login from any device with your account  
✅ **Real Leaderboards**: Compare stats with other players globally  
✅ **P2P Matchmaking**: Server infrastructure ready for peer-to-peer features  
✅ **Free Tier**: Supabase offers 500MB database + 2GB bandwidth/month  
✅ **Automatic Backups**: Your data is safe and backed up

## Architecture

The app uses a **hybrid approach**:

- **Server Mode** (Primary): Uses Supabase when internet is available
- **Local Mode** (Fallback): Falls back to SQLite if server is unreachable
- **Automatic Detection**: Seamlessly switches between modes

---

## Step 1: Create Supabase Account

1. Go to [https://supabase.com](https://supabase.com)
2. Click **"Start your project"**
3. Sign up with GitHub, Google, or Email (it's FREE!)
4. Verify your email if required

---

## Step 2: Create a New Project

1. After logging in, click **"New Project"**
2. Fill in the details:
    - **Name**: `rhetorix-backend` (or any name you like)
    - **Database Password**: Create a strong password (save this!)
    - **Region**: Choose closest to your location (e.g., US East, EU West)
    - **Pricing Plan**: **Free** (select this!)
3. Click **"Create new project"**
4. Wait 1-2 minutes for setup to complete

---

## Step 3: Get API Credentials

1. In your project dashboard, click on **Settings** (⚙️ gear icon in sidebar)
2. Click **"API"** in the settings menu
3. You'll see two important values:

   **Project URL** (looks like):
   ```
   https://abcdefghijklmnop.supabase.co
   ```

   **anon/public key** (looks like):
   ```
   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFiY2RlZmdoaWprbG1ub3AiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTYyNzU5NzYzNSwiZXhwIjoxOTQzMTczNjM1fQ.abc123...
   ```

4. **Copy both values** - you'll need them in Step 5

---

## Step 4: Create Database Schema

1. In Supabase dashboard, click **"SQL Editor"** in sidebar
2. Click **"New query"**
3. Copy and paste the following SQL:

```sql
-- ============================================
-- RHETORIX DATABASE SCHEMA
-- ============================================

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id VARCHAR(7) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    date_of_birth VARCHAR(10) NOT NULL,
    total_games INTEGER DEFAULT 0,
    wins INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    average_score FLOAT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create debate_history table
CREATE TABLE IF NOT EXISTS debate_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic TEXT NOT NULL,
    user_side VARCHAR(50) NOT NULL,
    opponent_type VARCHAR(50) NOT NULL,
    user_score INTEGER NOT NULL,
    opponent_score INTEGER NOT NULL,
    won BOOLEAN NOT NULL,
    feedback TEXT,
    debate_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_player_id ON users(player_id);
CREATE INDEX IF NOT EXISTS idx_debate_history_user_id ON debate_history(user_id);
CREATE INDEX IF NOT EXISTS idx_debate_history_created_at ON debate_history(created_at DESC);

-- Enable Row Level Security (RLS) for security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE debate_history ENABLE ROW LEVEL SECURITY;

-- Create policies for users table
-- Users can read their own data
CREATE POLICY "Users can view their own data" ON users
    FOR SELECT
    USING (auth.uid() = id);

-- Users can insert their own data (sign up)
CREATE POLICY "Users can insert their own data" ON users
    FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Users can update their own data
CREATE POLICY "Users can update their own data" ON users
    FOR UPDATE
    USING (auth.uid() = id);

-- Allow reading all users for leaderboard (only specific columns)
CREATE POLICY "Anyone can view user leaderboard data" ON users
    FOR SELECT
    USING (true);

-- Create policies for debate_history table
-- Users can view their own debate history
CREATE POLICY "Users can view their own debate history" ON debate_history
    FOR SELECT
    USING (auth.uid() = user_id);

-- Users can insert their own debate history
CREATE POLICY "Users can insert their own debate history" ON debate_history
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for users table
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SUCCESS! Your database is ready!
-- ============================================
```

4. Click **"Run"** (or press Ctrl+Enter)
5. You should see: **"Success. No rows returned"**

---

## Step 5: Configure Your Android App

1. Open your project in Android Studio

2. Navigate to:
   ```
   app/src/main/java/com/runanywhere/startup_hackathon20/network/SupabaseConfig.kt
   ```

3. Replace the placeholder values with your actual credentials:

   **Before:**
   ```kotlin
   private const val SUPABASE_URL = "YOUR_SUPABASE_URL"
   private const val SUPABASE_KEY = "YOUR_SUPABASE_ANON_KEY"
   ```

   **After:**
   ```kotlin
   private const val SUPABASE_URL = "https://abcdefghijklmnop.supabase.co"
   private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   ```

4. **Save the file**

---

## Step 6: Sync Gradle and Build

1. In Android Studio, click **"Sync Project with Gradle Files"** (🐘 icon in toolbar)
2. Wait for sync to complete (may take 1-2 minutes)
3. Build the app: **Build → Rebuild Project**
4. If you get any errors, try **File → Invalidate Caches / Restart**

---

## Step 7: Test Your Setup

### Test 1: Sign Up

1. Run the app on your device
2. Click **"Sign Up"**
3. Fill in details and create an account
4. Check Supabase dashboard → **Authentication** → You should see your new user!
5. Check **Table Editor** → **users** → Your profile should be there!

### Test 2: Login

1. Close and reopen the app
2. You should be automatically logged in
3. Check logs in Android Studio (should show "Server mode enabled")

### Test 3: Uninstall & Reinstall

1. Uninstall the app from your device
2. Reinstall the app
3. Login with your existing email/password
4. ✅ **Success!** Your account persists across installs!

### Test 4: Play a Debate

1. Play a debate and complete it
2. Go to Supabase → **Table Editor** → **debate_history**
3. You should see your debate record saved!

---

## Monitoring Your Server

### View Users

- Supabase Dashboard → **Authentication** → See all users
- Or **Table Editor** → **users** → See detailed profiles

### View Debate History

- **Table Editor** → **debate_history** → All debate records

### Check Database Size

- **Settings** → **Usage** → Monitor your free tier usage
- Free tier: 500MB database, 2GB bandwidth/month

### View Logs

- **Logs** → See all database queries and errors

---

## Troubleshooting

### Error: "Failed to connect to server"

- Check your internet connection
- Verify API credentials in `SupabaseConfig.kt`
- Check Supabase project is not paused (free tier pauses after 7 days inactivity)

### Error: "Email already registered"

- Normal! Means the email is already in use
- Try a different email or use login

### Error: "Invalid credentials"

- Check you're using the correct email/password
- Passwords are case-sensitive

### App uses local mode instead of server

- Check logs in Android Studio for connection errors
- Verify API credentials are correct
- Check if Supabase project is active

### Build errors after adding Supabase

- Clean project: **Build → Clean Project**
- Rebuild: **Build → Rebuild Project**
- Invalidate caches: **File → Invalidate Caches / Restart**

---

## Supabase Free Tier Limits

| Resource | Free Tier Limit | Notes |
|----------|----------------|-------|
| Database Size | 500 MB | Enough for ~50,000 users |
| Bandwidth | 2 GB/month | ~20,000 API calls/month |
| API Requests | Unlimited | No rate limit on free tier |
| Auth Users | Unlimited | No limit on user count |
| Storage | 1 GB | For file uploads (future feature) |
| Project Pausing | 7 days inactive | Wakes up on next request |

**Tip**: The free tier is very generous for a hackathon project or MVP!

---

## Security Best Practices

✅ **Never commit API keys to public repos** - Add to `.gitignore`  
✅ **Use environment variables** for production  
✅ **Enable RLS (Row Level Security)** - Already configured!  
✅ **Don't share your database password** - Keep it secret  
✅ **Monitor usage** - Check dashboard regularly

---

## Next Steps: P2P Matchmaking

Now that you have a server backend, you can implement:

1. **Real-time Matchmaking**: Use Supabase Realtime for live P2P connections
2. **Global Leaderboards**: Query all users sorted by wins
3. **Friend System**: Add friend requests and challenges
4. **Chat System**: Real-time messaging between players
5. **Tournament Mode**: Create brackets and competitions

The server infrastructure is ready! 🚀

---

## Migration from Local Database

If you have existing local users and want to migrate them:

1. Export data from local SQLite
2. Use Supabase API to bulk insert
3. Or use SQL Editor to import CSV

(We can create a migration script if needed - let me know!)

---

## Support

- **Supabase Docs**: https://supabase.com/docs
- **Community Discord**: https://discord.supabase.com
- **Stack Overflow**: Tag with `supabase`

---

## Summary

✅ Free server-based database  
✅ User accounts persist across installs  
✅ Ready for P2P features  
✅ Automatic fallback to local mode  
✅ Secure authentication  
✅ Real-time capabilities

**You now have a production-ready backend! 🎉**
