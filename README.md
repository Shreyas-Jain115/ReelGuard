# ReelGuard (Android)
Open this folder in Android Studio (it creates the Gradle wrapper), run on device, then:
1. Open app -> "Enable accessibility service" -> turn on ReelGuard.
2. Per app choose Track / Limit (reels or minutes per day) / Block, then Save.
A live overlay "N scrolled • mm:ss" appears top-right only while inside Reels/Shorts.

## Privacy design
- No INTERNET permission -> cannot transmit anything.
- Service scoped to Instagram + YouTube only; not exported; only the OS can bind it.
- Reads view IDs only, never text/messages/content. No logging.
- Data in MODE_PRIVATE prefs; allowBackup off; cloud/device-transfer excluded.
- Zero third-party libraries.
