# Cədvəl Studio Mobile

Android application to operate Cədvəl Studio Windows Smart Print over a private Tailscale network. The mobile app has no embedded financial records.

**Usage:** Start the Windows server, activate Tailscale on both devices, open the app and enter the Windows Tailscale IP URL (typically http://100.x.y.z:8765). Windows admin can generate a one-time pairing code in Cihazlar və giriş. Paired phone role has print-only access; admin password enables administrative screens.

**Security:** Tailscale VPN traffic is encrypted. Never expose port 8765 publicly. Google sign-in is not implemented in this APK. The Windows machine and printer must remain powered on.

**Build:** cd android && gradle assembleDebug (JDK 17, Android SDK 35, Gradle 8.7).
