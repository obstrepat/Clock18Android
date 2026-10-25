# Əhval havası — Android

Native Java Android activity + home screen widget, not HTML.
Sources: original "Gün işığı.html" phrases, 18 phrases kept verbatim in Azerbaijani.
Weather API: MET Norway Locationforecast 2.0; cached 30 minutes; device may default to Baku until location button pressed.
Seven fully localized languages from prior prototype: az/en/ru/tr/es/fr/de.
100 locale identifiers selectable in test build; the other 93 currently fall back to English until quality-reviewed translations are added.
This branch is independent of Clock18 main. Workflow builds an unsigned-by-publisher **debug-signed** sideloadable test APK.
It is NOT a production-ready Google Play release.
