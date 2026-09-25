# Build validation

Validated on 2026-09-25 for PikaStats 1.6.0, Forge 1.8.9.

- The clean Java 21 build passed with warnings treated as errors and Java 8 output: `./gradlew :1.8.9-forge:clean :1.8.9-forge:build --offline --warning-mode fail`.
- All 59 headless regression checks passed, including the new default-on TAB visibility, HP, and nametag status, live-prefix rank colors, ten-level color bands, default-off FK/Wins columns, the default-layout migration, distinct experimental party colors, guild/friend profile parsing, ambiguous denick handling, and independent TAB/HUD settings.
- The optional offscreen LWJGL 2 test passed previously for 1.5.3; it has not been rerun for these local changes.
- The jar includes the waiting-room team and vanilla player-list data hooks, optional VanillaHUD TAB background hook, shared TAB/HUD renderer, OneConfig loader wrapper, and mixin refmap. Regression classes are excluded.

## Locally built artifact

`versions/1.8.9-forge/build/libs/PikaStats-1.8.9-forge-1.6.0.jar`

496,311 bytes; SHA-256 `e8b17594dc59bbeefbf5ba433b161d440b13319b130a5343ae391a79f95f4b62`.

## Client validation

The user confirmed the released 1.5.3 works in Minecraft, including the restored match overview. The new local changes have passed automated checks but have not yet been exercised in a Minecraft client; the detailed scenarios in [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) remain to be checked for this build.
