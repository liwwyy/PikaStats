# PikaStats

![Powered by OneConfig](https://polyfrost.org/media/branding/badges/badge_1.svg)

**For those who just wanna get the mod, go to the [latest release](https://github.com/liwwyy/PikaStats/releases/latest).**

PikaStats adds PikaNetwork BedWars statistics to Minecraft Forge 1.8.9. It provides two independently configured OneConfig tables: a TAB replacement that opens with the player-list key and a movable HUD. Made by **liwwyy and movi**.

# Gallery
![tab](https://github.com/user-attachments/assets/d58cf951-6866-465f-9220-1239072a6972)
![hud](https://github.com/user-attachments/assets/823a3fbd-08c0-4cad-a5e0-b6d6fcef3075)

## Installation

1. Install **Minecraft Java Edition 1.8.9** and the [Forge 1.8.9 installer](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html). Select the Forge 1.8.9 profile in your launcher and run it once. Use Java 8 to run this Minecraft version.
2. Download the **Forge 1.8.9** build of [OneConfig](https://modrinth.com/mod/oneconfig?version=1.8.9&loader=forge#download). Put its `.jar` in that profile's `mods` folder.
3. From [the latest PikaStats release](https://github.com/liwwyy/PikaStats/releases/latest), download the file named `PikaStats-1.8.9-forge-X.X.X.jar` and put it in the same `mods` folder. Remove older PikaStats jars so only one version remains.
4. Launch Forge 1.8.9. Press **O** or run `/pikastats` to open settings. Join PikaNetwork and hold the player-list key (normally **Tab**) for the TAB table. The HUD appears in the waiting lobby by default; position both tables in OneConfig's HUD editor.

Only the release jar and OneConfig are needed. The release contains Poppins and the OneConfig loader wrapper.

## Features and settings

<details>
<summary>General</summary>

- **OneConfig mod switch / toggle hotkey:** turns all PikaStats features on or off. **Open PikaStats settings** defaults to O; **Toggle PikaStats** can be assigned another key.
- **Only on PikaNetwork:** restricts tables and nametag stats to PikaNetwork servers.
- **BedWars mode:** choose Overall, Solo, Doubles, or Quads. **Stats period:** Lifetime, Weekly, Monthly, or Yearly.
- **Max API players, cache TTL, and ping refresh:** control request volume, how long results remain cached, and ping update frequency.
- **Debug logging:** records denick mappings and explains missed or unresolved denick attempts in the game console. Enable it before entering a waiting room when diagnosing a player.
- **Highlight party members / Party members first:** mark party members and move them ahead of other waiting-lobby players.
- **Font:** Minecraft, bundled Poppins, or Custom TTF. For Custom TTF, place a `.ttf` in `.minecraft/config/pikastats/fonts/` and enter its exact filename. Files in the old `config/pikastats/` location still work. A missing or invalid font falls back to Minecraft's font.
- **Low performance mode:** skips animation, glass effects, custom images, player heads, and custom fonts while keeping the table readable.
- **Reload background images:** reloads local images and fonts after replacing files. **Community:** links to Discord and the source repository.

</details>

<details>
<summary>TAB</summary>

- **TAB enable switch:** enables the duplicate HUD at the top center, where vanilla TAB normally opens. Holding the player-list key shows it and hides vanilla TAB.
- **Always show / Show in waiting lobby / Show in game:** choose the BedWars states in which TAB can open. The player-list key is still required.
- **Combine rank with name:** on by default. **Sort waiting-lobby TAB / Sort statistic:** order players by FKDR, WLR, highest winstreak, final kills, wins, beds, or level.
- In pre-game waiting rooms, PikaStats recognizes the server's ordered original-name replacement packets. A resolved nick shows its original username in grey italics and uses the original account for stats. Unresolved nicks remain marked NICKED.
- **Columns and column order:** show or hide level, rank, name, highest winstreak, FKDR, WLR, final kills, wins, beds, and ping. Set their order with the comma-separated field.
- **Loading skeleton:** enabled by default. Names, player heads, and ping appear immediately; pending API stat cells use animated placeholders. Low performance mode keeps the placeholders static.
- **Match overview:** enabled by default. Beds Destroyed, Kills, and Final Kills from Pika's vanilla player-list data appear in a separate rounded pill four pixels below TAB during a match.
- **Appearance:** max TAB players, header, player heads, alternating rows, column dividers, glass background, and background opacity. Show and hide animations each offer None, Slide, Zoom, Bounce, and Pop with separate duration controls. Resize, new-player, and disconnect animations also have separate controls.
- **Position and scale:** the first TAB section contains the OneConfig position, reset, lock, scale, alignment, and editor controls. The editor lets you drag and resize TAB, including outside BedWars.
- **Image:** choose a PNG from `config/pikastats/backgrounds/`, a random PNG from that folder, or an optional random Catbox image, plus opacity, size, and left/center/right placement. TAB and HUD choices are independent. Use **Request new waifu image** to fetch another selection.

</details>

<details>
<summary>HUD</summary>

- **Always show / Show in waiting lobby / Show in game:** choose when the persistent HUD appears. **Hide while TAB is held** prevents overlap.
- **Combine rank with name:** on by default. **Sort waiting-lobby HUD / Sort statistic:** control HUD ordering separately from TAB. **Max HUD players** limits the number of rows.
- **Columns and column order:** independently show, hide, and reorder the same statistics available in TAB.
- **Loading skeleton:** independently controls pending-stat placeholders for HUD while keeping player identity visible immediately.
- **Appearance:** header, player heads, alternating rows, column dividers, glass background, background opacity, and separate popup, resize, new-player, and disconnect animations.
- **Position and scale:** the first HUD section contains the OneConfig position, reset, lock, scale, alignment, and editor controls. The editor lets you drag and resize **Player stats**.
- **Image:** independently choose a PNG from `config/pikastats/backgrounds/`, a random PNG from that folder, or random Catbox image, plus opacity, size, and left/center/right placement.

</details>

<details>
<summary>Nametags</summary>

- **Enable nametag stats:** off by default. Choose FKDR, level, WLR, highest winstreak, final kills, wins, or beds beside player names.
- **Show in waiting lobby / Show in game:** control BedWars state visibility. Waiting-lobby visibility is preselected. **Always show** ignores the BedWars state check, while the master switch and **Only on PikaNetwork** still apply.
- The value appears as `Name [value]` with white brackets and statistic colors. Vanilla visibility, distance, and sneaking rules remain in charge.

</details>

<details>
<summary>Images, results, and commands</summary>

- Put local PNGs in `.minecraft/config/pikastats/backgrounds/` and TTF files in `.minecraft/config/pikastats/fonts/`. Existing files directly in `config/pikastats/` still work. Images preserve transparency and aspect ratio. Files are limited to 8 MiB, 4096 pixels on a side, and eight million pixels. Random Catbox images are downloaded only when enabled; their temporary cache is cleared when Minecraft exits.
- **NO STATS** appears grey when a player has no records; **API DISABLED** appears red and **NICKED** purple. Spectator names are grey and italic in both tables.
- `/pikastats` and `/pikaoverlay` open settings. `/stats <player>` fetches a player's stats in chat.

</details>

## Build and development

Install JDK 21 and JDK 8, then run `./gradlew :1.8.9-forge:build` from this directory with JDK 21 selected for Gradle. The release jar appears in `versions/1.8.9-forge/build/libs/` without `-dev` or `-sources` in its name. The [validation notes](BUILD_VALIDATION.md) and [release checklist](RELEASE_CHECKLIST.md) describe the remaining in-game checks.

Poppins is distributed under the [SIL Open Font License](src/main/resources/assets/pikastats/fonts/OFL.txt). See the [changelog](CHANGELOG.md) for release details.

<picture><source media="(prefers-color-scheme: dark)" srcset="https://shieldcn.dev/discord/members/22EXF28uCb.svg?mode=dark"><img alt="badge" src="https://shieldcn.dev/discord/members/22EXF28uCb.svg?mode=light"></picture>

[Source](https://github.com/liwwyy/PikaStats) · [Discord](https://discord.gg/22EXF28uCb)
