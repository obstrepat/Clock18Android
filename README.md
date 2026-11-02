# Fokus Qalxanı (Android)
Azərbaycanca açar ifadə əsaslı, klaviaturanı dəyişməyən, tam ekran xəbərdarlıq tətbiqi.

- Min Android: 8.0 (API 26).
- Qaydalar və ifadələr SQLite-də saxlanılır; heç bir internet icazəsi yoxdur.
- AccessibilityService yalnız istifadəçinin açıq razılığından sonra aktivləşdirilir.
- Yazılan mətnlər saxlanılmır, serverə göndərilmir; parol sahələri istisnadır.
- Ekran xəbərdarlığı Accessibility overlay vasitəsilə göstərilir.
- Hər qayda üçün çoxsaylı açar ifadə, sərbəst xəbərdarlıq mətni, aktiv/deaktiv/sil.
- İfadələrin tanınması tətbiqin və Android-in əlçatanlıq məhdudiyyətlərindən asılıdır.

Build: `gradle :app:assembleDebug`.

Bu ayrıca layihə budağıdır; Clock18Android main budağı dəyişdirilmir.
