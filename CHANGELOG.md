# Changelog

## 1.5.2

- Clear TAB and HUD visibility and cached rows on world changes, and require an active world and player before rendering.
- Add default-on BedWars scoreboard and denicking switches in General. The BedWars switch requires the sidebar to contain “BedWars,” including when an overlay is set to Always show.
- Add nametag display modes: stats above the username by default, or beside it in brackets.
- Default nametag visibility to Always show, with waiting-lobby and in-game switches off; the nametag feature itself remains opt-in.
- Report the last observed denick stage for unresolved API names, including missing roster evidence, ignored removals, and unmatched team replacements.
- Request `/party list` after the local player joins a party. Correct the match-overview pill’s text alignment.

## 1.5.1

- Add default-on loading-skeleton toggles for TAB and HUD. Player names, heads, and ping appear immediately while only pending stat cells shimmer; low performance mode uses static placeholders.
- Improve waiting-room denicking by retaining team-create and early roster packets, allowing queued same-team replacements, and widening the tightly bounded replacement window. Add General → Advanced → Debug logging for successful mappings, expired candidates, missing roster evidence, and unresolved API nicks.
- Add a default-on TAB match overview pill, separated from the table by four pixels, that mirrors Pika's Beds Destroyed, Kills, and Final Kills counters from the vanilla player-list data.

## 1.5.0

- Detect high-confidence original-name to nick replacements from PikaNetwork's ordered waiting-room team rewrite. Denicked rows fetch the original account's stats and show its username in grey italics beside the nick.
- Replace rows whose stats have not arrived with an animated skeleton placeholder.
- Animate existing rows vertically when stat sorting changes their position.
- Center player heads within their rows, render final kills, wins, and beds in grey, and apply stat sorting only in pre-game waiting lobbies.
- Restore Position as the first inline section in TAB and HUD settings instead of a submenu.

## 1.4.2

- Add separate hide animations for TAB and HUD. Pop now also works as a hide effect, with independent hide style and duration controls.
- Put OneConfig's generated position controls, including Reset, inside dedicated Position pages for TAB and HUD.
- Add Request new waifu image buttons for each table and clear downloaded Catbox images when Minecraft exits.
- Correct the background-file setting label to `config/pikastats/backgrounds`.

## 1.4.1

- Add General → Performance → Low performance mode. It skips popup, row and resize animations, glass shader, background images, player heads, and custom font rendering while retaining the basic table.
- Add Pop popup animation and configurable disconnect card slides to TAB and HUD.
- Keep editor and reset controls together in each Position section.
- Create `config/pikastats/backgrounds` and `config/pikastats/fonts` for user assets, retain legacy file paths, and add a random local background option for each table.

## 1.4.0

- Hide VanillaHUD's separate TAB background only while PikaStats TAB is active, without changing VanillaHUD's saved toggle or entering its player-list renderer.
- Fit text to available cells and center it vertically. Cap oversized columns so long player names and custom fonts shrink within the table.
- Animate TAB and HUD size changes and new player rows with separate slide direction and duration controls. Speed up the default popup animation and add timing sliders.
- Add General → Assets → Keep image size, which holds an image's render scale until the image is reloaded.
- Remove the misplaced installation message from OneConfig and arrange TAB/HUD settings in the same section order.

## 1.3.2

- Suppress vanilla TAB at Forge's PLAYER_LIST pre-render event, before VanillaHUD enters its player-list renderer. Its 1.8.9 mixin pushes a GL matrix at method entry and pops it at method exit; the earlier PikaStats HEAD cancellation could skip that pop and trigger the reported stack overflow.

## 1.3.1

- Restore the caller's active texture unit and matrix mode after drawing so optimization mods can balance their own matrix pushes and pops. The supplied 1.3.0 crash showed OpenGL stack overflow 1283 in a modded client.
- Add slide, zoom, and bounce animations to the HUD as well as TAB.
- Default to Minecraft's font and migrate previous default-Poppins profiles. Optional TTF fonts now render at double resolution with linear texture filtering.
- Show `liywy` and `movi6287` in the OneConfig previews. Clarify that the jar installs OneConfig automatically when it is missing.

## 1.3.0

- Replaced the standalone custom TAB overlay and compatibility path with a second OneConfig HUD entry, sharing the working HUD table renderer while keeping TAB and HUD settings, sorting, images, and positions separate. The duplicate starts top-centered; vanilla TAB is hidden whenever it is active.
- Restored slide, zoom, and bounce TAB animations on the shared HUD renderer.
- Bundled Poppins Regular, Bold, Italic, and Bold Italic with the SIL Open Font License; added Poppins as the default font and kept custom TTF support.
- Added an Always show nametag option and made rank-with-name enabled by default for both tables.
- Added a tag-triggered GitHub Actions build and release workflow that publishes only the release jar.
- Expanded the README with an installation tutorial and setting-by-setting dropdowns.

## 1.2.2

- Removed TAB popup animation and leave the GUI on its normal matrix and texture unit after custom rendering to prevent later HUD renderers from overflowing the texture stack.
- Added center positioning and size controls for TAB and HUD images, preserving existing right-aligned choices.
- Made waiting-lobby nametags the preselected visibility option and migrated enabled profiles with neither visibility option selected.
- Restored red API DISABLED and purple NICKED while NO STATS stays grey.
- Render spectator names grey and italic in both tables and compatibility TAB.
- Added an optional custom TTF renderer with whole-string GPU caching and native-font fallback.

## 1.2.1

- Removed the custom font atlas; HUD and TAB use Minecraft's font renderer.
- Isolated overlay matrices, texture bindings, shaders and render flags without adding matrix-stack pushes.
- Unified the master enable switch with OneConfig and migrated conflicting legacy settings so an active overlay can show its HUD.
- Put General first and corrected the OneConfig icon to a 56-pixel display size with a 128-pixel asset.
- Split background image controls and loading state between TAB and HUD; migrate existing shared choices once.
- Added separate waiting-lobby and in-game nametag visibility switches, both disabled by default.

## 1.2.0

- Renamed the mod, package, commands, resources and release artifacts to PikaStats.
- Fixed overlapping translucent panel fills and the empty-player-list HUD crash.
- Added single rounded HUD/TAB panels, frosted glass and TAB popup animations.
- Added Poppins Regular and configurable local fonts for mod tables.
- Registered a native OneConfig HUD with persisted positioning, scaling and editor examples.
- Added configurable player nametag stats and explicit grey NO STATS results.
- Added transparent PNG backgrounds and optional Catbox downloads with a bounded local cache.
- Updated metadata, icon, community buttons and documentation.
- Removed the separate HUD editor, obsolete migration and unused helpers; simplified Java-only packaging.
