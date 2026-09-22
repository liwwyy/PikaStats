# Build validation

Validated on 2026-09-23 for PikaStats 1.5.2, Forge 1.8.9.

- The clean Java 21 build passed with warnings treated as errors and Java 8 output: `./gradlew :1.8.9-forge:clean :1.8.9-forge:build --offline --warning-mode fail`.
- All 48 headless regression checks passed, including early and queued denick pairing, match-overview parsing, vertical row movement, smooth panel retargeting, new-player and disconnect timing, show/hide timing, Catbox cache cleanup, profile migration, and independent TAB/HUD settings.
- The optional offscreen LWJGL 2 test passed 500 frames on an AMD Radeon RX 550. It verified caller matrix/texture state and scissor restoration, compiled the glass shader, and uploaded formatted TTF text without GL errors.
- The release jar includes the waiting-room team and vanilla player-list data hooks, optional VanillaHUD TAB background hook, shared TAB/HUD renderer, OneConfig loader wrapper, and mixin refmap. Regression classes are excluded.

## Release artifact

`versions/1.8.9-forge/build/libs/PikaStats-1.8.9-forge-1.5.2.jar`

Local validation build: 481,187 bytes; SHA-256 `a316f9eb2c0f518ece67175af6fa8f2e801e0f5bed618afa571e5cd766445cd6`.
GitHub Actions release asset: 476,260 bytes; SHA-256 `b80ff7489b598b29785684fddbcefc003ec70ad9f89e9fe6c581de56c936a605`. All jar entries have identical contents; the archive size differs because of build environment packaging.

## Client validation still required

The user confirmed 1.3.2 runs without crashes in the full modpack. Version 1.5.2 was not launched in Minecraft here. Validate world-change visibility, BedWars-only gating, both nametag modes, self-join party refresh, denick debug stages, the match-overview pill, low performance mode, and VanillaHUD compatibility. See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).
