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
- **BedWars only:** on by default; requires “BedWars” in the sidebar scoreboard, even when a table or nametag is set to Always show.
- **Show HP only in game:** in General → Stats, on by default; controls the optional TAB heart column.
- **Gray regular names:** on by default in waiting rooms. In games, usernames keep their team colors. Rank labels keep their own colors.
- **Column spacing:** Comfy restores the original padding and is the default; Compact uses the tighter spacing.
- **Level colors:** level numbers change color every ten levels from gray at 0–9 through dark red at 100. When a rank appears beside a name, its text follows the server's tab-list rank color when available.
- **BedWars mode:** choose Overall, Solo, Doubles, or Quads. **Stats period:** Lifetime, Weekly, Monthly, or Yearly.
- **Max API players:** controls request volume. The separate **Advanced** section contains cache TTL, ping refresh, and debug logging.
- **Font:** Minecraft, bundled Poppins, or Custom TTF. For Custom TTF, place a `.ttf` in `.minecraft/config/pikastats/fonts/` and enter its exact filename. Files in the old `config/pikastats/` location still work. A missing or invalid font falls back to Minecraft's font.
- **Low performance mode:** skips animation, glass effects, custom images, player heads, and custom fonts while keeping the table readable.
- **Reload background images:** reloads local images and fonts after replacing files. **Community:** links to Discord and the source repository.

</details>

<details>
<summary>Party, Friends, and Denick</summary>

- **Party:** your party is highlighted and sorted first. Party joins and leaves are recognized from Pika chat messages, with `/party list` used to reconcile membership. An optional, default-off experiment groups other waiting-lobby arrivals within a configurable millisecond window. Each group of at least two receives the next color: red, blue, green, yellow, aqua, white, pink, or gray. Simultaneous arrivals alone are only a hint, not proof of party membership or team destination.
- **Friends:** on by default. Friends from your public Pika profile are highlighted orange and sorted after your party in waiting rooms.
- **Denick:** off by default. Enable detection and, separately, original-name display in this section. Ambiguous simultaneous same-team rewrites stay unresolved rather than showing a possibly wrong identity. The settings include a red warning about Pika's denicker rules.

</details>

<details>
<summary>TAB</summary>

- **TAB enable switch:** enables the duplicate HUD at the top center, where vanilla TAB normally opens. Holding the player-list key shows it and hides vanilla TAB.
- **Always show / Show in waiting lobby / Show in game:** Always show is on by default, so TAB can open throughout BedWars. The player-list key is still required.
- **Combine rank with name:** on by default. **Sort waiting-lobby TAB / Sort statistic:** order players by FKDR, WLR, highest winstreak, final kills, wins, beds, or level.
- When Denick detection is enabled, unambiguous pre-game team rewrites can use the original account for stats. The original name appears beside the nick only when its separate display switch is on.
- **Columns and column order:** by default show heads, name with its colored rank, FKDR, level, WLR, highest winstreak, and in-game HP. Final kills and wins start off in both tables. Other available fields are beds, guild, and ping. The guild name comes from the profile API's `clan.name`. HP is a TAB-only column with Minecraft's heart icon, beside ping unless reordered. Unknown or unavailable health appears as `?`.
- **Loading skeleton:** enabled by default. Names, player heads, and ping appear immediately; pending API stat cells use animated placeholders. Low performance mode keeps the placeholders static.
- **Match overview:** enabled by default. Beds Destroyed, Kills, and Final Kills from Pika's vanilla player-list data appear in a separate rounded pill four pixels below TAB during a match.
- **Appearance:** max TAB players, header, player heads, alternating rows, column dividers, glass background, and background opacity. Show and hide animations each offer None, Slide, Zoom, Bounce, and Pop with separate duration controls. Resize, new-player, and disconnect animations also have separate controls.
- **Position and scale:** the Position submenu contains OneConfig's position, reset, lock, scale, alignment, and editor controls.
- **Image:** choose a PNG from `config/pikastats/backgrounds/`, a random PNG from that folder, or an optional random Catbox image, plus opacity, size, and left/center/right placement. TAB and HUD choices are independent. Use **Request new waifu image** to fetch another selection.

</details>

<details>
<summary>HUD</summary>

- **Always show / Show in waiting lobby / Show in game:** choose when the persistent HUD appears. **Hide while TAB is held** prevents overlap.
- **Combine rank with name:** on by default. **Sort waiting-lobby HUD / Sort statistic:** control HUD ordering separately from TAB. **Max HUD players** limits the number of rows.
- **Columns and column order:** the same default visible fields as TAB, independently configurable and including optional guild; HP is TAB-only.
- **Loading skeleton:** independently controls pending-stat placeholders for HUD while keeping player identity visible immediately.
- **Appearance:** header, player heads, alternating rows, column dividers, glass background, background opacity, and separate popup, resize, new-player, and disconnect animations.
- **Position and scale:** the Position submenu contains OneConfig's position, reset, lock, scale, alignment, and editor controls.
- **Image:** independently choose a PNG from `config/pikastats/backgrounds/`, a random PNG from that folder, or random Catbox image, plus opacity, size, and left/center/right placement.

</details>

<details>
<summary>Nametags</summary>

- **Enable nametag stats:** on by default. Choose FKDR, level, WLR, highest winstreak, final kills, wins, or beds for player names.
- **Always show / Show in waiting lobby / Show in game:** Always show is on by default; the two state switches are off. It bypasses the waiting/in-game state check. The master switch, **Only on PikaNetwork**, and **BedWars only** still apply.
- **Nametag display mode:** Above username is the default and shows an unbracketed statistic on its own line, above both the username and any below-name score such as hearts. With username shows `Name [value]`. Vanilla visibility, distance, and sneaking rules remain in charge.
- **Gray regular names:** on by default in waiting rooms. Colored rank prefixes remain colored, party/friend names retain their highlight, and in-game team colors remain visible.

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
