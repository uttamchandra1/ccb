# Quick Fix Guide - Gmail API 403 Error

## ✅ What Was Fixed

I've implemented a comprehensive fix for the Gmail API scope issue. Here's what changed:

### 1. **New Configuration Files**
- `OAuth2ClientConfig.java` - Manages OAuth2 tokens properly
- `CustomOAuth2AuthorizationRequestResolver.java` - Forces consent screen and requests all scopes

### 2. **Updated Files**
- `SecurityConfig.java` - Now uses custom OAuth2 configuration
- `CardDetectionController.java` - Added scope debugging information

## 🚀 How to Fix Your Issue

### **CRITICAL: You MUST Re-authenticate**

The 403 error occurs because your current access token doesn't have the Gmail scope. Follow these steps:

### Step 1: Verify Google Cloud Console

1. Go to https://console.cloud.google.com/
2. Navigate to **APIs & Services** → **Enabled APIs**
3. **Ensure Gmail API is ENABLED** ← Most common issue!
   - If not enabled, click "+ ENABLE APIS AND SERVICES"
   - Search for "Gmail API"
   - Click ENABLE

4. Navigate to **OAuth consent screen**
5. Under **Scopes**, verify `https://www.googleapis.com/auth/gmail.readonly` is listed
   - If not, click "ADD OR REMOVE SCOPES"
   - Search for "Gmail API"
   - Check the "gmail.readonly" scope
   - Click UPDATE

### Step 2: Restart Application

```bash
cd /Users/uttamkumar/Desktop/ccbapp
./mvnw spring-boot:run
```

### Step 3: Clear Browser and Re-login

**Option A: Use Incognito/Private Window (Easiest)**
1. Open an incognito/private browser window
2. Go to http://localhost:8080
3. Click "Login with Google"
4. **You should now see Gmail permission in the consent screen**
5. Accept all permissions

**Option B: Clear Browser Cookies**
1. Clear cookies for `localhost:8080`
2. Go to http://localhost:8080
3. Login again

### Step 4: Verify Scopes

After logging in, go to:
```
http://localhost:8080/api/card-detection/status
```

You should see:
```json
{
  "hasGmailScope": true,
  "scopes": [...includes "gmail.readonly"...]
}
```

### Step 5: Test Gmail Scan

Click "Scan Gmail" button in the test interface or use:
```bash
curl -X POST http://localhost:8080/api/card-detection/scan
```

## 🔍 Troubleshooting

### Still Getting 403?

1. **Check Gmail API is enabled** in Google Cloud Console
2. **Verify the consent screen** shows Gmail permission when you login
3. **Check the status endpoint** - does `hasGmailScope` show `true`?
4. **Try a different Google account** to rule out account-specific issues

### Not Seeing Gmail Permission in Consent Screen?

This means the scope isn't being requested. Check:
1. `application.properties` has the Gmail scope (should already be there)
2. Application was restarted after code changes
3. Google Cloud Console has Gmail scope configured

### Redirect URI Mismatch?

Add this to Google Cloud Console → Credentials → OAuth 2.0 Client:
```
http://localhost:8080/login/oauth2/code/google
```

## 📋 What the Fix Does

1. **Forces Consent Screen**: Every login will show the consent screen, ensuring you grant Gmail permission
2. **Requests Offline Access**: Gets refresh tokens for long-term access
3. **Proper Scope Management**: Ensures all configured scopes are requested
4. **Debugging Info**: Status endpoint now shows what scopes you actually have

## ⚡ Quick Test

After restarting and re-logging in:

1. Open http://localhost:8080/test.html
2. Click "Login with Google"
3. **Accept Gmail permission** in consent screen
4. Click "Check Status" - should show `hasGmailScope: true`
5. Click "Scan Gmail" - should work without 403 error

## 📚 More Details

See `docs/GMAIL_SCOPE_FIX.md` for comprehensive troubleshooting guide.

---

**The key issue**: Your existing access token was created before the Gmail scope was configured. You MUST re-authenticate to get a new token with Gmail permissions.
