package dev.movi.pikastats.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.elements.OptionCategory;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigPageButton;
import cc.polyfrost.oneconfig.gui.pages.ModConfigPage;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import dev.movi.pikastats.hud.StatsHud;
import dev.movi.pikastats.hud.TabHud;
import dev.movi.pikastats.model.BedWarsMode;
import dev.movi.pikastats.model.StatsPeriod;
import dev.movi.pikastats.model.TabSortStat;

public final class PikaConfig extends Config {
    public static PikaConfig INSTANCE;

    // Legacy values retained only for migration from 1.2.0.
    public static boolean overlayEnabled = true;
    @Switch(name = "Only on PikaNetwork", category = "General", subcategory = "Core")
    public static boolean onlyOnPika = true;
    @Switch(name = "BedWars only", category = "General", subcategory = "Core")
    public static boolean bedWarsOnly = true;
    @Info(text = "Denicking may be bannable on Pika. Avoid sharing screenshots or bragging about it.",
          type = InfoType.ERROR, size = 2, category = "Denick", subcategory = "Warning")
    public static boolean denickWarning;
    @Switch(name = "Enable denicking", category = "Denick", subcategory = "Detection")
    public static boolean denicking = false;
    @Switch(name = "Show real name in overlay", category = "Denick", subcategory = "Detection")
    public static boolean showDenickedName = false;
    @Switch(name = "Show HP only in game", category = "General", subcategory = "Stats")
    public static boolean hpOnlyInGame = true;
    @Switch(name = "Low performance mode", category = "General", subcategory = "Performance")
    public static boolean lowPerformanceMode = false;
    @Dropdown(name = "BedWars mode", options = {"Overall", "Solo", "Doubles", "Quads"},
              category = "General", subcategory = "Stats")
    public static int mode = 0;
    @Dropdown(name = "Stats period", options = {"Lifetime", "Weekly", "Monthly", "Yearly"},
              category = "General", subcategory = "Stats")
    public static int statsPeriod = 0;
    @Slider(name = "Max API players", min = 1, max = 40, step = 1, category = "General",
            subcategory = "Stats")
    public static int maxPlayersToFetch = 16;
    @Slider(name = "Cache TTL (seconds)", min = 120, max = 600, step = 10, category = "Advanced",
            subcategory = "Advanced")
    public static int cacheTtlSeconds = 300;
    @Slider(name = "Ping refresh (ms)", min = 250, max = 3000, step = 50, category = "Advanced",
            subcategory = "Advanced")
    public static int pingUpdateIntervalMs = 1000;
    @Switch(name = "Debug logging", category = "Advanced", subcategory = "Advanced")
    public static boolean debugLogging = false;
    @Switch(name = "Highlight party members", category = "Party", subcategory = "Your party")
    public static boolean partyHighlightEnabled = true;
    @Switch(name = "Party members first", category = "Party", subcategory = "Your party")
    public static boolean partySortFirst = true;
    @Switch(name = "Detect other parties (experimental)", category = "Party", subcategory = "Other parties")
    public static boolean detectOtherParties = false;
    @Slider(name = "Join window (ms)", min = 50, max = 2000, step = 50, category = "Party", subcategory = "Other parties")
    public static int otherPartyWindowMs = 350;
    // Legacy single-color setting retained to load older profiles.
    public static int otherPartyColor = 1;
    @Switch(name = "Highlight friends", category = "Friends", subcategory = "Highlight")
    public static boolean highlightFriends = true;
    @Switch(name = "Friends after party", category = "Friends", subcategory = "Sorting")
    public static boolean friendsAfterParty = true;
    @Switch(name = "Gray regular names", category = "General", subcategory = "Appearance")
    public static boolean grayNames = true;
    @Dropdown(name = "Column spacing", options = {"Comfy", "Compact"}, category = "General",
              subcategory = "Appearance")
    public static int columnSpacing = 0;
    @KeyBind(name = "Open PikaStats settings", category = "General", subcategory = "Hotkeys")
    public static OneKeyBind openConfigKey = new OneKeyBind(UKeyboard.KEY_O);
    @KeyBind(name = "Toggle PikaStats", category = "General", subcategory = "Hotkeys")
    public static OneKeyBind toggleOverlayKey = new OneKeyBind(0);

    public static final class TabPositionPage {
        @Button(name = "Edit TAB position", text = "Open", category = "Position", subcategory = "Controls")
        public static Runnable edit = dev.movi.pikastats.hud.HudEditor::open;
    }
    public static final class HudPositionPage {
        @Button(name = "Edit HUD position", text = "Open", category = "Position", subcategory = "Controls")
        public static Runnable edit = dev.movi.pikastats.hud.HudEditor::open;
    }
    @Page(name = "Position", location = PageLocation.TOP, category = "TAB")
    public TabPositionPage tabPositionPage = new TabPositionPage();
    @HUD(name = "TAB", category = "TAB", subcategory = "Position")
    public TabHud tabHud = new TabHud();

    // Legacy setting, migrated into the OneConfig TAB HUD's enable switch.
    public static boolean tabEnabled = true;
    @Switch(name = "Always show", category = "TAB", subcategory = "Core")
    public static boolean tabAlwaysShow = true;
    @Switch(name = "Show in waiting lobby", category = "TAB", subcategory = "Core")
    public static boolean tabShowWaiting = true;
    @Switch(name = "Show in game", category = "TAB", subcategory = "Core")
    public static boolean tabShowInGame = true;

    // Retained for migration into OneConfig's native TAB HUD scale.
    public static int tabScalePercent = 100;
    @Slider(name = "Max TAB players", min = 1, max = 40, step = 1, category = "TAB",
            subcategory = "Core")
    public static int tabMaxPlayers = 20;
    @Switch(name = "Combine rank with name", category = "TAB", subcategory = "Appearance")
    public static boolean combineRankWithName = true;
    @Switch(name = "Header", category = "TAB", subcategory = "Appearance")
    public static boolean tabShowHeader = true;
    @Switch(name = "Player heads", category = "TAB", subcategory = "Appearance")
    public static boolean tabShowPlayerHeads = true;
    @Switch(name = "Loading skeleton", category = "TAB", subcategory = "Appearance")
    public static boolean tabLoadingSkeleton = true;
    @Switch(name = "Match overview", category = "TAB", subcategory = "Appearance")
    public static boolean tabMatchOverview = true;
    @Switch(name = "Alternating rows", category = "TAB", subcategory = "Appearance")
    public static boolean tabAlternatingRows = true;
    @Switch(name = "Column dividers", category = "TAB", subcategory = "Appearance")
    public static boolean tabColumnDividers = true;
    @Slider(name = "Background opacity", min = 0, max = 100, step = 1, category = "TAB",
            subcategory = "Appearance")
    public static int tabBackgroundOpacity = 48;

    @Switch(name = "Glass background", category = "TAB", subcategory = "Appearance")
    public static boolean tabGlass = true;
    @Dropdown(name = "Popup animation", options = {"None", "Slide", "Zoom", "Bounce", "Pop"},
              category = "TAB", subcategory = "Appearance")
    public static int tabAnimation = 1;
    @Slider(name = "Popup duration (ms)", min = 40, max = 500, step = 10,
            category = "TAB", subcategory = "Appearance")
    public static int tabAnimationDuration = 120;
    @Dropdown(name = "Hide animation", options = {"None", "Slide", "Zoom", "Bounce", "Pop"},
              category = "TAB", subcategory = "Appearance")
    public static int tabHideAnimation = 4;
    @Slider(name = "Hide duration (ms)", min = 40, max = 500, step = 10,
            category = "TAB", subcategory = "Appearance")
    public static int tabHideDuration = 120;
    @Slider(name = "Resize duration (ms)", min = 40, max = 500, step = 10,
            category = "TAB", subcategory = "Appearance")
    public static int tabResizeDuration = 150;
    @Dropdown(name = "New player animation", options = {"None", "Slide left", "Slide right", "Slide up"},
              category = "TAB", subcategory = "Appearance")
    public static int tabEntryAnimation = 1;
    @Slider(name = "New player duration (ms)", min = 40, max = 500, step = 10,
            category = "TAB", subcategory = "Appearance")
    public static int tabEntryDuration = 160;
    @Dropdown(name = "Disconnect animation", options = {"None", "Slide left", "Slide right", "Slide down"},
              category = "TAB", subcategory = "Appearance")
    public static int tabExitAnimation = 1;
    @Slider(name = "Disconnect duration (ms)", min = 40, max = 500, step = 10,
            category = "TAB", subcategory = "Appearance")
    public static int tabExitDuration = 160;
    @Switch(name = "Level", category = "TAB", subcategory = "Columns")
    public static boolean tabShowLevel = true;
    @Switch(name = "Rank", category = "TAB", subcategory = "Columns")
    public static boolean tabShowRank = true;
    @Switch(name = "Name", category = "TAB", subcategory = "Columns")
    public static boolean tabShowName = true;
    @Switch(name = "Highest winstreak", category = "TAB", subcategory = "Columns")
    public static boolean tabShowWinstreak = true;
    @Switch(name = "FKDR", category = "TAB", subcategory = "Columns")
    public static boolean tabShowFkdr = true;
    @Switch(name = "WLR", category = "TAB", subcategory = "Columns")
    public static boolean tabShowWlr = true;
    @Switch(name = "Final kills", category = "TAB", subcategory = "Columns")
    public static boolean tabShowFinalKills = false;
    @Switch(name = "Wins", category = "TAB", subcategory = "Columns")
    public static boolean tabShowWins = false;
    @Switch(name = "Beds", category = "TAB", subcategory = "Columns")
    public static boolean tabShowBeds = false;
    @Switch(name = "Ping", category = "TAB", subcategory = "Columns")
    public static boolean tabShowPing = false;
    @Switch(name = "Guild", category = "TAB", subcategory = "Columns")
    public static boolean tabShowGuild = false;
    @Switch(name = "HP", category = "TAB", subcategory = "Columns")
    public static boolean tabShowHp = true;
    @Text(name = "Column order", placeholder = "NAME,FKDR,LV,WLR,HWS,FK,WINS,BEDS,GUILD,HP,PING",
          category = "TAB", subcategory = "Columns")
    public static String tabColumnOrder = "NAME,FKDR,LV,WLR,HWS,FK,WINS,BEDS,GUILD,HP,PING";

    @Switch(name = "Sort waiting-lobby TAB", category = "TAB", subcategory = "Sorting")
    public static boolean tabSortEnabled = true;
    @Dropdown(
        name = "Sort statistic",
        options = {"FKDR", "WLR", "Highest Winstreak", "Final Kills", "Wins", "Beds", "Level"},
        category = "TAB", subcategory = "Sorting")
    public static int tabSortStat = 0;

    @Switch(name = "Custom background image", category = "TAB", subcategory = "Image")
    public static boolean tabImageEnabled = false;
    @Text(name = "PNG file in config/pikastats/backgrounds", placeholder = "background.png", category = "TAB",
          subcategory = "Image")
    public static String tabImageFile = "background.png";
    @Switch(name = "Random background from folder", category = "TAB", subcategory = "Image")
    public static boolean tabRandomBackground = false;
    @Switch(name = "Fetch a random waifu (Catbox)", category = "TAB", subcategory = "Image")
    public static boolean tabRandomWaifu = false;
    @Button(name = "Request new waifu image", text = "Fetch", category = "TAB", subcategory = "Image")
    public static Runnable tabNewWaifu = () -> dev.movi.pikastats.render.BackgroundImage.TAB.requestNewWaifu();
    @Slider(name = "Image opacity", min = 10, max = 100, step = 1, category = "TAB",
            subcategory = "Image")
    public static int tabImageOpacity = 60;
    @Dropdown(name = "Image position", options = {"Left", "Center", "Right"}, category = "TAB",
              subcategory = "Image")
    public static int tabImagePosition = 0;
    @Slider(name = "Image size (%)", min = 10, max = 200, step = 5, category = "TAB",
            subcategory = "Image")
    public static int tabImageSize = 100;
    @Page(name = "Position", location = PageLocation.TOP, category = "HUD")
    public HudPositionPage hudPositionPage = new HudPositionPage();
    @HUD(name = "Player stats", category = "HUD", subcategory = "Position")
    public StatsHud statsHud = new StatsHud();

    @Switch(name = "Always show", category = "HUD", subcategory = "Core")
    public static boolean hudAlwaysShow = false;
    @Switch(name = "Show in waiting lobby", category = "HUD", subcategory = "Core")
    public static boolean hudShowWaiting = true;
    @Switch(name = "Show in game", category = "HUD", subcategory = "Core")
    public static boolean hudShowInGame = false;
    @Switch(name = "Hide while TAB is held", category = "HUD", subcategory = "Core")
    public static boolean hideHudWhileTab = true;
    @Slider(name = "Max HUD players", min = 1, max = 40, step = 1, category = "HUD",
            subcategory = "Core")
    public static int hudMaxPlayers = 16;

    @Switch(name = "Combine rank with name", category = "HUD", subcategory = "Appearance")
    public static boolean hudCombineRankWithName = true;
    @Switch(name = "Header", category = "HUD", subcategory = "Appearance")
    public static boolean hudShowHeader = true;
    @Switch(name = "Player heads", category = "HUD", subcategory = "Appearance")
    public static boolean hudShowPlayerHeads = true;
    @Switch(name = "Loading skeleton", category = "HUD", subcategory = "Appearance")
    public static boolean hudLoadingSkeleton = true;
    @Switch(name = "Alternating rows", category = "HUD", subcategory = "Appearance")
    public static boolean hudAlternatingRows = true;
    @Switch(name = "Column dividers", category = "HUD", subcategory = "Appearance")
    public static boolean hudColumnDividers = true;
    @Slider(name = "Background opacity", min = 0, max = 100, step = 1, category = "HUD",
            subcategory = "Appearance")
    public static int hudBackgroundOpacity = 48;
    @Switch(name = "Glass background", category = "HUD", subcategory = "Appearance")
    public static boolean hudGlass = true;
    @Dropdown(name = "Popup animation", options = {"None", "Slide", "Zoom", "Bounce", "Pop"},
              category = "HUD", subcategory = "Appearance")
    public static int hudAnimation = 1;
    @Slider(name = "Popup duration (ms)", min = 40, max = 500, step = 10,
            category = "HUD", subcategory = "Appearance")
    public static int hudAnimationDuration = 120;
    @Dropdown(name = "Hide animation", options = {"None", "Slide", "Zoom", "Bounce", "Pop"},
              category = "HUD", subcategory = "Appearance")
    public static int hudHideAnimation = 4;
    @Slider(name = "Hide duration (ms)", min = 40, max = 500, step = 10,
            category = "HUD", subcategory = "Appearance")
    public static int hudHideDuration = 120;
    @Slider(name = "Resize duration (ms)", min = 40, max = 500, step = 10,
            category = "HUD", subcategory = "Appearance")
    public static int hudResizeDuration = 150;
    @Dropdown(name = "New player animation", options = {"None", "Slide left", "Slide right", "Slide up"},
              category = "HUD", subcategory = "Appearance")
    public static int hudEntryAnimation = 1;
    @Slider(name = "New player duration (ms)", min = 40, max = 500, step = 10,
            category = "HUD", subcategory = "Appearance")
    public static int hudEntryDuration = 160;
    @Dropdown(name = "Disconnect animation", options = {"None", "Slide left", "Slide right", "Slide down"},
              category = "HUD", subcategory = "Appearance")
    public static int hudExitAnimation = 1;
    @Slider(name = "Disconnect duration (ms)", min = 40, max = 500, step = 10,
            category = "HUD", subcategory = "Appearance")
    public static int hudExitDuration = 160;
    @Switch(name = "Level", category = "HUD", subcategory = "Columns")
    public static boolean hudShowLevel = true;
    @Switch(name = "Rank", category = "HUD", subcategory = "Columns")
    public static boolean hudShowRank = true;
    @Switch(name = "Name", category = "HUD", subcategory = "Columns")
    public static boolean hudShowName = true;
    @Switch(name = "Highest winstreak", category = "HUD", subcategory = "Columns")
    public static boolean hudShowWinstreak = true;
    @Switch(name = "FKDR", category = "HUD", subcategory = "Columns")
    public static boolean hudShowFkdr = true;
    @Switch(name = "WLR", category = "HUD", subcategory = "Columns")
    public static boolean hudShowWlr = true;
    @Switch(name = "Final kills", category = "HUD", subcategory = "Columns")
    public static boolean hudShowFinalKills = false;
    @Switch(name = "Wins", category = "HUD", subcategory = "Columns")
    public static boolean hudShowWins = false;
    @Switch(name = "Beds", category = "HUD", subcategory = "Columns")
    public static boolean hudShowBeds = false;
    @Switch(name = "Ping", category = "HUD", subcategory = "Columns")
    public static boolean hudShowPing = false;
    @Switch(name = "Guild", category = "HUD", subcategory = "Columns")
    public static boolean hudShowGuild = false;
    @Text(name = "Column order", placeholder = "NAME,FKDR,LV,WLR,HWS,FK,WINS,BEDS,GUILD,PING",
          category = "HUD", subcategory = "Columns")
    public static String hudColumnOrder = "NAME,FKDR,LV,WLR,HWS,FK,WINS,BEDS,GUILD,PING";

    @Switch(name = "Sort waiting-lobby HUD", category = "HUD", subcategory = "Sorting")
    public static boolean hudSortEnabled = true;
    @Dropdown(name = "Sort statistic",
        options = {"FKDR", "WLR", "Highest Winstreak", "Final Kills", "Wins", "Beds", "Level"},
        category = "HUD", subcategory = "Sorting")
    public static int hudSortStat = 0;

    @Switch(name = "Custom background image", category = "HUD", subcategory = "Image")
    public static boolean hudImageEnabled = false;
    @Text(name = "PNG file in config/pikastats/backgrounds", placeholder = "background.png", category = "HUD",
          subcategory = "Image")
    public static String hudImageFile = "background.png";
    @Switch(name = "Random background from folder", category = "HUD", subcategory = "Image")
    public static boolean hudRandomBackground = false;
    @Switch(name = "Fetch a random waifu (Catbox)", category = "HUD", subcategory = "Image")
    public static boolean hudRandomWaifu = false;
    @Button(name = "Request new waifu image", text = "Fetch", category = "HUD", subcategory = "Image")
    public static Runnable hudNewWaifu = () -> dev.movi.pikastats.render.BackgroundImage.HUD.requestNewWaifu();
    @Slider(name = "Image opacity", min = 10, max = 100, step = 1, category = "HUD",
            subcategory = "Image")
    public static int hudImageOpacity = 60;
    @Dropdown(name = "Image position", options = {"Left", "Center", "Right"}, category = "HUD",
              subcategory = "Image")
    public static int hudImagePosition = 0;
    @Slider(name = "Image size (%)", min = 10, max = 200, step = 5, category = "HUD",
            subcategory = "Image")
    public static int hudImageSize = 100;
    @Dropdown(name = "Font", options = {"Minecraft", "Poppins", "Custom TTF"}, category = "General",
              subcategory = "Appearance")
    public static int fontMode = 0;
    @Text(name = "TTF in config/pikastats/fonts", placeholder = "custom.ttf", category = "General",
          subcategory = "Appearance")
    public static String customFont = "custom.ttf";
    @Button(name = "Reload background images", text = "Reload", category = "General",
            subcategory = "Assets")
    public static Runnable reloadAssets = () -> {
        dev.movi.pikastats.render.BackgroundImage.reload();
        dev.movi.pikastats.render.OverlayText.reload();
    };
    @Switch(name = "Keep image size", category = "General", subcategory = "Assets")
    public static boolean staticImageSize = false;
    @Switch(name = "Enable nametag stats", category = "Nametags", subcategory = "Stats")
    public static boolean nametagsEnabled = true;
    @Switch(name = "Gray regular names", category = "Nametags", subcategory = "Appearance")
    public static boolean grayNametagNames = true;
    @Dropdown(name = "Nametag display mode", options = {"Above username", "With username"},
              category = "Nametags", subcategory = "Stats")
    public static int nametagDisplayMode = 0;
    @Switch(name = "Always show", category = "Nametags", subcategory = "Visibility")
    public static boolean nametagsAlwaysShow = true;
    @Switch(name = "Show in waiting lobby", category = "Nametags", subcategory = "Visibility")
    public static boolean nametagsShowWaiting = false;
    @Switch(name = "Show in game", category = "Nametags", subcategory = "Visibility")
    public static boolean nametagsShowInGame = false;
    @Dropdown(
        name = "Statistic",
        options = {"FKDR", "Level", "WLR", "Highest winstreak", "Final kills", "Wins", "Beds"},
        category = "Nametags", subcategory = "Stats")
    public static int nametagStat = 0;
    public int settingsVersion = 0;
    public static boolean imageEnabled = false;
    public static String imageFile = "background.png";
    public static boolean randomWaifu = false;
    public static int imageOpacity = 60;
    public static int imagePosition = 0;
    @Button(name = "Join us on Discord", text = "Join", category = "General",
            subcategory = "Community")
    public static Runnable discord =
        () -> dev.movi.pikastats.util.Links.open("https://discord.gg/22EXF28uCb");
    @Button(name = "Star the repo on github", text = "Star", category = "General",
            subcategory = "Community")
    public static Runnable github =
        () -> dev.movi.pikastats.util.Links.open("https://github.com/liwwyy/PikaStats");

    public PikaConfig() {
        super(new Mod("PikaStats", ModType.UTIL_QOL, "/assets/pikastats/icon.png", 56, 56),
              "pikastats.json");
        INSTANCE = this;
        initialize();
        movePositionControls("TAB");
        movePositionControls("HUD");
        orderSections();
        migrateSettings();
        sanitizeLoadedValues();
        save();
        registerKeyBind(openConfigKey, new Runnable() {
            @Override
            public void run() {
                openOneConfig();
            }
        });
        registerKeyBind(toggleOverlayKey, new Runnable() {
            @Override
            public void run() {
                enabled = !enabled;
                persist();
                dev.movi.pikastats.hud.StatsHudRenderer.refresh();
            }
        });
    }
    private void movePositionControls(String category) {
        OptionCategory group = mod.defaultPage.categories.get(category);
        if (group == null) return;
        OptionSubcategory controls = ConfigUtils.getSubCategory(mod.defaultPage, category, "Position");
        ConfigPageButton button = null;
        for (OptionSubcategory section : group.subcategories)
            for (ConfigPageButton candidate : section.topButtons)
                if ("Position".equals(candidate.name)) button = candidate;
        if (button == null || !(button.page instanceof ModConfigPage)) return;
        ModConfigPage page = (ModConfigPage) button.page;
        OptionSubcategory target = ConfigUtils.getSubCategory(page.getPage(), "Position", "Controls");
        target.options.addAll(controls.options);
        controls.options.clear();
        group.subcategories.remove(controls);
    }
    private void orderSections() {
        java.util.LinkedHashMap<String, OptionCategory> categories = mod.defaultPage.categories;
        java.util.LinkedHashMap<String, OptionCategory> ordered =
            new java.util.LinkedHashMap<String, OptionCategory>();
        for (String name : new String[] {"General", "TAB", "HUD", "Nametags"}) {
            OptionCategory category = categories.get(name);
            if (category != null) ordered.put(name, category);
        }
        ordered.putAll(categories);
        categories.clear();
        categories.putAll(ordered);
    }
    @Override
    public void load() {
        // A profile without this field must migrate even after switching profiles.
        settingsVersion = 0;
        super.load();
        migrateSettings();
        sanitizeLoadedValues();
    }

    private void migrateSettings() {
        if (settingsVersion >= 8)
            return;
        if (settingsVersion == 7) {
            tabAlwaysShow = true;
            tabShowHp = true;
            nametagsEnabled = true;
            settingsVersion = 8;
            return;
        }
        if (settingsVersion == 6) {
            if ("LV,RANK,NAME,FKDR,WLR,HWS,FK,WINS,BEDS,GUILD,HP,PING".equalsIgnoreCase(tabColumnOrder)) {
                tabColumnOrder = "NAME,FKDR,LV,WLR,HWS,FK,WINS,BEDS,GUILD,HP,PING";
                tabShowFinalKills = tabShowWins = tabShowPing = tabShowHp = false;
            }
            if ("LV,RANK,NAME,FKDR,WLR,HWS,FK,WINS,BEDS,GUILD,PING".equalsIgnoreCase(hudColumnOrder)) {
                hudColumnOrder = "NAME,FKDR,LV,WLR,HWS,FK,WINS,BEDS,GUILD,PING";
                hudShowFinalKills = hudShowWins = hudShowPing = false;
            }
            settingsVersion = 7;
            migrateSettings();
            return;
        }
        if (settingsVersion == 5) {
            denicking = false;
            showDenickedName = false;
            if (!tabColumnOrder.toUpperCase(java.util.Locale.ROOT).contains("HP"))
                tabColumnOrder = tabColumnOrder.replace("PING", "GUILD,HP,PING");
            settingsVersion = 6;
            migrateSettings();
            return;
        }
        if (settingsVersion == 4) {
            if (tabAnimationDuration == 180) tabAnimationDuration = 120;
            if (hudAnimationDuration == 180) hudAnimationDuration = 120;
            settingsVersion = 5;
            migrateSettings();
            return;
        }
        if (settingsVersion == 3) {
            // Poppins was the previous default. Prefer Minecraft's proven
            // renderer for existing profiles while retaining custom TTF.
            if (fontMode == 1) fontMode = 0;
            settingsVersion = 4;
            migrateSettings();
            return;
        }
        if (settingsVersion == 2) {
            // 1.2.2 used index 1 for a user-provided TTF.
            if (fontMode == 1) fontMode = 2;
            if ("custom.tff".equalsIgnoreCase(customFont)) customFont = "custom.ttf";
            hudCombineRankWithName = combineRankWithName;
            hudSortEnabled = tabSortEnabled;
            hudSortStat = tabSortStat;
            if (tabHud != null) {
                tabHud.setScale(tabScalePercent / 100f, true);
                tabHud.setLegacyEnabled(tabEnabled);
            }
            settingsVersion = 3;
            migrateSettings();
            return;
        }
        if (settingsVersion == 1) {
            if (tabImagePosition == 1)
                tabImagePosition = 2;
            if (hudImagePosition == 1)
                hudImagePosition = 2;
            if (nametagsEnabled && !nametagsShowWaiting && !nametagsShowInGame)
                nametagsAlwaysShow = true;
            settingsVersion = 2;
            migrateSettings();
            fontMode = 0;
            return;
        }
        // The old General switch controlled TAB; OneConfig's hidden second switch
        // could independently suppress the HUD. Keep the user's effective choice.
        enabled = overlayEnabled;
        tabImageEnabled = hudImageEnabled = imageEnabled;
        tabImageFile = hudImageFile = imageFile;
        tabRandomWaifu = hudRandomWaifu = randomWaifu;
        tabImageOpacity = hudImageOpacity = imageOpacity;
        tabImagePosition = hudImagePosition = imagePosition == 1 ? 2 : 0;
        settingsVersion = 2;
        migrateSettings();
        fontMode = 0;
    }

    public static boolean isModEnabled() { return INSTANCE != null && INSTANCE.enabled; }

    public static BedWarsMode bedWarsMode() { return BedWarsMode.fromIndex(mode); }
    public static StatsPeriod statsPeriodValue() { return StatsPeriod.fromIndex(statsPeriod); }
    public static TabSortStat sortStat() { return TabSortStat.fromIndex(tabSortStat); }
    public static void openOneConfig() {
        if (INSTANCE != null)
            INSTANCE.openGui();
    }
    public static void persist() {
        if (INSTANCE == null)
            return;
        INSTANCE.save();
    }

    private static void sanitizeLoadedValues() {
        mode = clamp(mode, 0, 3);
        statsPeriod = clamp(statsPeriod, 0, 3);
        tabSortStat = clamp(tabSortStat, 0, 6);
        hudSortStat = clamp(hudSortStat, 0, 6);
        nametagDisplayMode = clamp(nametagDisplayMode, 0, 1);
        maxPlayersToFetch = clamp(maxPlayersToFetch, 1, 40);
        cacheTtlSeconds = clamp(cacheTtlSeconds, 120, 600);
        pingUpdateIntervalMs = clamp(pingUpdateIntervalMs, 250, 3000);
        tabScalePercent = clamp(tabScalePercent, 50, 150);
        tabMaxPlayers = clamp(tabMaxPlayers, 1, 40);
        tabAnimation = clamp(tabAnimation, 0, 4);
        tabAnimationDuration = clamp(tabAnimationDuration, 40, 500);
        tabHideAnimation = clamp(tabHideAnimation, 0, 4);
        tabHideDuration = clamp(tabHideDuration, 40, 500);
        tabResizeDuration = clamp(tabResizeDuration, 40, 500);
        tabEntryAnimation = clamp(tabEntryAnimation, 0, 3);
        tabEntryDuration = clamp(tabEntryDuration, 40, 500);
        tabExitAnimation = clamp(tabExitAnimation, 0, 3);
        tabExitDuration = clamp(tabExitDuration, 40, 500);
        hudAnimation = clamp(hudAnimation, 0, 4);
        hudAnimationDuration = clamp(hudAnimationDuration, 40, 500);
        hudHideAnimation = clamp(hudHideAnimation, 0, 4);
        hudHideDuration = clamp(hudHideDuration, 40, 500);
        hudResizeDuration = clamp(hudResizeDuration, 40, 500);
        hudEntryAnimation = clamp(hudEntryAnimation, 0, 3);
        hudEntryDuration = clamp(hudEntryDuration, 40, 500);
        hudExitAnimation = clamp(hudExitAnimation, 0, 3);
        hudExitDuration = clamp(hudExitDuration, 40, 500);
        tabBackgroundOpacity = clamp(tabBackgroundOpacity, 0, 100);
        hudMaxPlayers = clamp(hudMaxPlayers, 1, 40);
        hudBackgroundOpacity = clamp(hudBackgroundOpacity, 0, 100);
        tabImagePosition = clamp(tabImagePosition, 0, 2);
        hudImagePosition = clamp(hudImagePosition, 0, 2);
        tabImageSize = clamp(tabImageSize, 10, 200);
        hudImageSize = clamp(hudImageSize, 10, 200);
        otherPartyWindowMs = clamp(otherPartyWindowMs, 50, 2000);
        otherPartyColor = clamp(otherPartyColor, 0, 7);
        columnSpacing = clamp(columnSpacing, 0, 1);
        tabColumnOrder = sanitizeOrder(tabColumnOrder);
        hudColumnOrder = sanitizeOrder(hudColumnOrder);
    }

    private static String sanitizeOrder(String raw) {
        if (raw == null || raw.trim().isEmpty())
            return "NAME,FKDR,LV,WLR,HWS,FK,WINS,BEDS,GUILD,HP,PING";
        // Accept the old PikaStats order and retain the current HP column.
        String normalized = raw.toUpperCase(java.util.Locale.ROOT)
                                .replace("HEALTH", "HP")
                                .replaceAll(",+", ",")
                                .replaceAll("^,|,$", "");
        if (normalized.equals("LV,RANK,NAME,WS,FKDR,WLR,FK,WINS"))
            normalized = "LV,RANK,NAME,FKDR,WLR,HWS,FK,WINS,BEDS,PING";
        return normalized;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
