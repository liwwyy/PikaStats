# Build validation

Validated on 2026-09-21 for PikaStats 1.4.2, Forge 1.8.9.

- The clean Java 21 build passed with warnings treated as errors and Java 8 output: `./gradlew :1.8.9-forge:clean :1.8.9-forge:build --offline --warning-mode fail`.
- All 46 headless regression checks passed, including smooth panel retargeting, new-player and disconnect timing, show/hide timing, Catbox cache cleanup, profile migration, and independent TAB/HUD settings.
- The optional offscreen LWJGL 2 test passed 500 frames on an AMD Radeon RX 550. It verified caller matrix/texture state and scissor restoration, compiled the glass shader, and uploaded formatted TTF text without GL errors.
- The release jar includes the optional VanillaHUD TAB background hook, shared TAB/HUD renderer, OneConfig loader wrapper, and mixin refmap. Regression classes are excluded.

## Release artifact

`versions/1.8.9-forge/build/libs/PikaStats-1.8.9-forge-1.4.2.jar`

Size: 471,566 bytes. SHA-256: `b90fa0e86ab3251ffcda71f3b87ff4bae8e058e1ac7005a2d0a3dfbe0332cf60`.

## Client validation still required

The user confirmed 1.3.2 runs without crashes in the full modpack. Version 1.4.2 was not launched in Minecraft here. Check that TAB and HUD Position pages contain Edit and Reset, show/hide animations (especially Pop), separate new-waifu buttons, Catbox cache deletion on exit, low performance mode, and VanillaHUD compatibility. See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).
