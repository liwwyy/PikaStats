# Build validation

Validated on 2026-09-22 for PikaStats 1.5.1, Forge 1.8.9.

- The clean Java 21 build passed with warnings treated as errors and Java 8 output: `./gradlew :1.8.9-forge:clean :1.8.9-forge:build --offline --warning-mode fail`.
- All 48 headless regression checks passed, including early and queued denick pairing, match-overview parsing, vertical row movement, smooth panel retargeting, new-player and disconnect timing, show/hide timing, Catbox cache cleanup, profile migration, and independent TAB/HUD settings.
- The optional offscreen LWJGL 2 test passed 500 frames on an AMD Radeon RX 550. It verified caller matrix/texture state and scissor restoration, compiled the glass shader, and uploaded formatted TTF text without GL errors.
- The release jar includes the waiting-room team and vanilla player-list data hooks, optional VanillaHUD TAB background hook, shared TAB/HUD renderer, OneConfig loader wrapper, and mixin refmap. Regression classes are excluded.

## Release artifact

`versions/1.8.9-forge/build/libs/PikaStats-1.8.9-forge-1.5.1.jar`

Size: 478,488 bytes. SHA-256: `b3c60636233e9ac597eaa1e818290cfc602ef217e03b6edb16d3b3b07c49bfb0`.

## Client validation still required

The user confirmed 1.3.2 runs without crashes in the full modpack. Version 1.5.1 was not launched in Minecraft here. Validate improved denick capture and debug output against live PikaNetwork packets, immediate player identity with TAB/HUD skeleton toggles, the match-overview pill and live counter updates, low performance mode, and VanillaHUD compatibility. See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).
