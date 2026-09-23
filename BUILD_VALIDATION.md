# Build validation

Validated on 2026-09-23 for PikaStats 1.5.3, Forge 1.8.9.

- The clean Java 21 build passed with warnings treated as errors and Java 8 output: `./gradlew :1.8.9-forge:clean :1.8.9-forge:build --offline --warning-mode fail`.
- All 48 headless regression checks passed, including early and queued denick pairing, match-overview parsing, vertical row movement, smooth panel retargeting, new-player and disconnect timing, show/hide timing, Catbox cache cleanup, profile migration, and independent TAB/HUD settings.
- The optional offscreen LWJGL 2 test passed 500 frames on an AMD Radeon RX 550. It verified caller matrix/texture state and scissor restoration, compiled the glass shader, and uploaded formatted TTF text without GL errors.
- The jar includes the waiting-room team and vanilla player-list data hooks, optional VanillaHUD TAB background hook, shared TAB/HUD renderer, OneConfig loader wrapper, and mixin refmap. Regression classes are excluded.

## Locally built artifact

`versions/1.8.9-forge/build/libs/PikaStats-1.8.9-forge-1.5.3.jar`

482,403 bytes; SHA-256 `f25dd051bb86c0054d2abc9cd72abae8c8c4a8070060ba1203168cde4d62797f`.

## Client validation

The user confirmed that 1.5.3 works in Minecraft, including the restored match overview. The automated and offscreen checks above were run locally; the remaining detailed scenarios in [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) were not independently exercised here.
