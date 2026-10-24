# Agency Pilot — Android
Standalone **native Android** advertising-agency assistant (Java, Android SDK). An isolated branch in Clock18Android so its main app is untouched.

## Features
- AZ-language home dashboard, client management, selection and deletion
- Offline local strategy, content calendar, ad copy, SEO, competitor analysis and report **templates** (not AI)
- Campaign recordkeeping, manual metrics, CPC / CPL
- Saved output reading and Android sharing
- Optional live AI through user-configured OpenAI-compatible HTTPS chat-completions endpoint
- Offline persistent storage using Android SharedPreferences

## Build
JDK 17, Android SDK 35, Gradle 8.9
`gradle assembleDebug` -> `app/build/outputs/apk/debug/app-debug.apk`
GitHub Actions builds an installable debug APK under artifacts.

## Disclosure
Not connected to Google Ads, Meta Ads or any social network; all campaign numbers are manual. Real AI works only after providing a separately operated HTTPS API endpoint, model name and potentially API key. Store no sensitive production secrets in app preferences. This is a functional standalone MVP, **not** a production SaaS.
