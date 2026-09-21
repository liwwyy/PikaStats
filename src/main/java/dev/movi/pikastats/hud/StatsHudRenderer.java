package dev.movi.pikastats.hud;

import dev.movi.pikastats.api.StatsManager;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.model.PlayerStats;
import dev.movi.pikastats.render.OverlayRenderState;
import dev.movi.pikastats.render.OverlayClip;
import dev.movi.pikastats.render.OverlayText;
import dev.movi.pikastats.render.PanelBackground;
import dev.movi.pikastats.render.RenderUtil;
import dev.movi.pikastats.tab.TabFormat;
import dev.movi.pikastats.tab.MatchOverview;
import dev.movi.pikastats.util.PlayerListUtil;
import dev.movi.pikastats.util.PlayerSorting;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.WorldSettings;

public final class StatsHudRenderer {
    private static final int ROW_H = 12, HEADER_H = 14, TITLE_H = 14, CELL_PAD = 4, HEAD_SLOT = 10,
                             OUTER = 5, COMBINED_GAP = 7, PILL_H = 14, PILL_GAP = 4;
    private StatsHudRenderer() {}
    private static final class Snapshot {
        final List<TabFormat.Column> cols;
        final int[] widths;
        final List<NetworkPlayerInfo> players;
        final List<NetworkPlayerInfo> departing;
        final List<NetworkPlayerInfo> display;
        final int head, w, h;
        final String overview;
        final boolean demo;
        Snapshot(List<TabFormat.Column> c, int[] wi, List<NetworkPlayerInfo> p, int head, int w,
                 int h, boolean d, String overview) {
            cols = c;
            widths = wi;
            players = p;
            departing = new ArrayList<NetworkPlayerInfo>();
            display = new ArrayList<NetworkPlayerInfo>(p);
            this.head = head;
            this.w = w;
            this.h = h;
            demo = d;
            this.overview = overview;
        }
    }
    private static volatile Snapshot cachedHud, cachedTab;
    private static final HudMotion HUD_MOTION = new HudMotion(), TAB_MOTION = new HudMotion();

    public static void refresh() { refresh(false); refresh(true); }
    public static void refresh(boolean tab) {
        Snapshot next = snapshot(false, tab);
        if (next != null) {
            List<String> names = new ArrayList<String>();
            for (NetworkPlayerInfo info : next.players)
                names.add(PlayerListUtil.profileName(info));
            long now = System.nanoTime();
            HudMotion motion = motion(tab);
            motion.update(next.w, next.h, names, now, duration(tab), exitDuration(tab));
            if (!PikaConfig.lowPerformanceMode && exitMode(tab) != 0) {
                Set<String> leaving = motion.leaving(now, exitDuration(tab));
                Snapshot old = tab ? cachedTab : cachedHud;
                if (old != null) {
                    for (NetworkPlayerInfo info : old.players)
                        if (leaving.remove(PlayerListUtil.profileName(info))) next.departing.add(info);
                    for (NetworkPlayerInfo info : old.departing)
                        if (leaving.remove(PlayerListUtil.profileName(info))) next.departing.add(info);
                    for (NetworkPlayerInfo info : next.departing) {
                        int previous = -1;
                        for (int i = 0; i < old.display.size(); i++)
                            if (PlayerListUtil.profileName(old.display.get(i))
                                    .equals(PlayerListUtil.profileName(info))) {
                                previous = i;
                                break;
                            }
                        next.display.add(Math.min(Math.max(0, previous), next.display.size()), info);
                    }
                }
            }
        }
        if (tab) cachedTab = next;
        else cachedHud = next;
    }
    public static float width(boolean example) { return width(example, false); }
    public static float width(boolean example, boolean tab) {
        Snapshot s = current(example, tab);
        return s == null ? 80 : (example ? s.w : motion(tab).width(System.nanoTime(), duration(tab)))
            + OUTER * 2;
    }
    public static float height(boolean example) { return height(example, false); }
    public static float height(boolean example, boolean tab) {
        Snapshot s = current(example, tab);
        return s == null ? 24 : (example ? s.h : motion(tab).height(System.nanoTime(), duration(tab)))
            + OUTER * 2;
    }
    private static HudMotion motion(boolean tab) { return tab ? TAB_MOTION : HUD_MOTION; }
    private static int duration(boolean tab) {
        return PikaConfig.lowPerformanceMode ? 0
            : tab ? PikaConfig.tabResizeDuration : PikaConfig.hudResizeDuration;
    }
    private static int exitDuration(boolean tab) {
        return PikaConfig.lowPerformanceMode ? 0
            : tab ? PikaConfig.tabExitDuration : PikaConfig.hudExitDuration;
    }
    private static int exitMode(boolean tab) {
        return PikaConfig.lowPerformanceMode ? 0
            : tab ? PikaConfig.tabExitAnimation : PikaConfig.hudExitAnimation;
    }
    private static Snapshot current(boolean example, boolean tab) {
        if (example) return snapshot(true, tab);
        if (tab) {
            if (cachedTab == null) refresh(true);
            return cachedTab;
        }
        if (cachedHud == null) refresh(false);
        return cachedHud;
    }
    public static void renderAt(float x, float y, float scale, boolean example) {
        renderAt(x, y, scale, example, false, 1f, 0);
    }
    public static void renderAt(float x, float y, float scale, boolean example,
                                boolean tab, float animationProgress, int animationMode) {
        Snapshot s = current(example, tab);
        if (s != null) draw(s, x, y, scale, tab, animationProgress, animationMode);
    }

    private static Snapshot snapshot(boolean demo, boolean tab) {
        List<TabFormat.Column> cols = tab ? TabFormat.tabColumns() : TabFormat.hudColumns();
        if (cols.isEmpty())
            return null;
        ArrayList<NetworkPlayerInfo> players = new ArrayList<NetworkPlayerInfo>();
        if (!demo) {
            players.addAll(PlayerSorting.sortPlayers(PlayerListUtil.listedPlayers(), tab));
            int limit = tab ? PikaConfig.tabMaxPlayers : PikaConfig.hudMaxPlayers;
            if (players.size() > limit)
                players = new ArrayList<NetworkPlayerInfo>(players.subList(0, limit));
        }
        OverlayText f = OverlayText.get();
        int[] widths = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++)
            widths[i] = f.getStringWidth(TabFormat.header(cols.get(i)));
        int statusNeed = 0;
        int[] span = TabFormat.statusSpan(cols);
        if (demo) {
            for (int row = 0; row < 2; row++)
                for (int i = 0; i < cols.size(); i++)
                    widths[i] = Math.max(widths[i],
                                         f.getStringWidth(TabFormat.demo(cols.get(i), row)));
        } else
            for (NetworkPlayerInfo info : players) {
                PlayerStats st = StatsManager.peek(PlayerListUtil.profileName(info));
                String status = TabFormat.status(st);
                if (status != null)
                    statusNeed = Math.max(statusNeed, f.getStringWidth(status) + CELL_PAD * 2);
                for (int i = 0; i < cols.size(); i++) {
                    if (status != null && span != null && i >= span[0] && i <= span[1])
                        continue;
                    int m = cols.get(i).id.equals("name") &&
                            (tab ? PikaConfig.combineRankWithName : PikaConfig.hudCombineRankWithName)
                                ? f.getStringWidth(TabFormat.name(info)) + COMBINED_GAP +
                                      f.getStringWidth(TabFormat.combinedSecondary(info, st))
                                : f.getStringWidth(TabFormat.cell(cols.get(i), info, st));
                    widths[i] = Math.max(widths[i], m);
                }
            }
        if (!demo && players.isEmpty()) {
            for (int i = 0; i < cols.size(); i++)
                if (cols.get(i).id.equals("name"))
                    widths[i] = Math.max(widths[i], f.getStringWidth("No players"));
        }
        for (int i = 0; i < widths.length; i++)
            widths[i] = Math.min(widths[i] + CELL_PAD * 2,
                                 cols.get(i).id.equals("name") ? 136 : 78);
        if (span != null && statusNeed > 0) {
            int cur = 0;
            for (int i = span[0]; i <= span[1]; i++)
                cur += widths[i];
            if (cur < statusNeed)
                widths[span[1]] += statusNeed - cur;
        }
        int head = !PikaConfig.lowPerformanceMode &&
            (tab ? PikaConfig.tabShowPlayerHeads : PikaConfig.hudShowPlayerHeads)
            ? HEAD_SLOT : 0, w = head;
        for (int x : widths)
            w += x;
        if (tab ? PikaConfig.tabShowHeader : PikaConfig.hudShowHeader) {
            int titleWidth = f.getStringWidth("PikaStats • " + PikaConfig.bedWarsMode().label +
                                              " • " + PikaConfig.statsPeriodValue().label);
            if (w < titleWidth) {
                widths[widths.length - 1] += titleWidth - w;
                w = titleWidth;
            }
        }
        int rows = demo ? 2 : players.size();
        String overview = tab && !demo && PikaConfig.tabMatchOverview ? MatchOverview.text() : null;
        int h = ((tab ? PikaConfig.tabShowHeader : PikaConfig.hudShowHeader)
            ? TITLE_H + HEADER_H : 0) + Math.max(1, rows) * ROW_H;
        if (overview != null) h += OUTER + PILL_GAP + PILL_H;
        return new Snapshot(cols, widths, players, head, Math.max(1, w), Math.max(1, h), demo,
                            overview);
    }
    private static void draw(Snapshot s, float screenX, float screenY, float scale,
                             boolean tab, float progress, int animationMode) {
        OverlayText f = OverlayText.get();
        float bx = screenX / scale + OUTER, by = screenY / scale + OUTER;
        HudMotion motion = motion(tab);
        long now = System.nanoTime();
        float panelW = s.demo ? s.w : motion.width(now, duration(tab));
        float panelH = s.demo ? s.h : motion.height(now, duration(tab));
        int overviewExtra = s.overview == null ? 0 : OUTER + PILL_GAP + PILL_H;
        float tableH = Math.max(1f, panelH - overviewExtra);
        try (OverlayRenderState state = new OverlayRenderState()) {
            GlStateManager.scale(scale, scale, 1f);
            if (!PikaConfig.lowPerformanceMode && progress < 1f)
                animate(s, bx, by, progress, animationMode);
            PanelBackground.draw(bx - OUTER, by - OUTER, bx + panelW + OUTER, by + tableH + OUTER,
                                 tab ? PikaConfig.tabBackgroundOpacity : PikaConfig.hudBackgroundOpacity,
                                 !PikaConfig.lowPerformanceMode &&
                                     (tab ? PikaConfig.tabGlass : PikaConfig.hudGlass),
                                 tab ? dev.movi.pikastats.render.BackgroundImage.TAB
                                     : dev.movi.pikastats.render.BackgroundImage.HUD);
            try (OverlayClip clip = OverlayClip.panel(screenX, screenY,
                    (panelW + OUTER * 2) * scale, (panelH + OUTER * 2) * scale,
                    !s.demo && progress >= 1f &&
                        (Math.abs(panelW - s.w) > .1f || Math.abs(panelH - s.h) > .1f ||
                         motion.entering(now, tab ? PikaConfig.tabEntryDuration : PikaConfig.hudEntryDuration)
                         || !s.departing.isEmpty()))) {
            if (s.overview != null) {
                int textW = f.getStringWidth(s.overview);
                float pillW = Math.min(panelW, textW + 16f);
                float pillX = bx + (panelW - pillW) / 2f;
                float pillY = by + tableH + OUTER + PILL_GAP;
                RenderUtil.roundedRect(pillX, pillY, pillX + pillW, pillY + PILL_H, 7,
                    RenderUtil.withAlpha(0x101217,
                        tab ? PikaConfig.tabBackgroundOpacity : PikaConfig.hudBackgroundOpacity));
                center(f, s.overview, Math.round(bx + panelW / 2f), Math.round(pillY),
                       Math.round(pillW), PILL_H);
            }
            int y = Math.round(by);
            if (tab ? PikaConfig.tabShowHeader : PikaConfig.hudShowHeader) {
                String title = "§aPikaStats§8 • §7" + PikaConfig.bedWarsMode().label + "§8 • §7" +
                               PikaConfig.statsPeriodValue().label;
                center(f, title, Math.round(bx) + s.w / 2, y, s.w, TITLE_H);
                y += TITLE_H;
                int x = Math.round(bx) + s.head;
                for (int i = 0; i < s.cols.size(); i++) {
                    cell(f, TabFormat.header(s.cols.get(i)), x, y, s.widths[i], HEADER_H,
                         s.cols.get(i).right);
                    x += s.widths[i];
                }
                if (tab ? PikaConfig.tabColumnDividers : PikaConfig.hudColumnDividers)
                    dividers(Math.round(bx), y, y + HEADER_H, s.head, s.widths, null);
                y += HEADER_H;
            }
            int count = s.demo ? 2 : s.display.size();
            if (count == 0)
                count = 1;
            for (int r = 0; r < count; r++) {
                NetworkPlayerInfo info = s.demo || s.display.isEmpty() ? null : s.display.get(r);
                boolean leaving = info != null && s.departing.contains(info);
                int entryMode = PikaConfig.lowPerformanceMode ? 0
                    : tab ? PikaConfig.tabEntryAnimation : PikaConfig.hudEntryAnimation;
                int entryMs = tab ? PikaConfig.tabEntryDuration : PikaConfig.hudEntryDuration;
                float entry = info == null || entryMode == 0 || leaving ? 1f
                    : motion.entry(PlayerListUtil.profileName(info), now, entryMs);
                int offset = Math.round((1f - entry) * 18);
                int exit = leaving ? Math.round(motion.exit(PlayerListUtil.profileName(info), now,
                                                  exitDuration(tab)) * 18) : 0;
                int exitMode = exitMode(tab);
                int rowX = Math.round(bx) + (entryMode == 1 ? -offset : entryMode == 2 ? offset : 0)
                    + (exitMode == 1 ? -exit : exitMode == 2 ? exit : 0);
                int rowY = y + (entryMode == 3 ? offset : 0) + (exitMode == 3 ? exit : 0);
                if (info != null && !leaving && !PikaConfig.lowPerformanceMode)
                    rowY += Math.round(motion.move(PlayerListUtil.profileName(info), now,
                                                   duration(tab)) * ROW_H);
                PlayerStats st =
                    info == null ? null : StatsManager.peek(PlayerListUtil.profileName(info));
                String status = info == null ? null : TabFormat.status(st);
                int[] span = status == null ? null : TabFormat.statusSpan(s.cols);
                if ((tab ? PikaConfig.tabAlternatingRows : PikaConfig.hudAlternatingRows)
                    && r % 2 == 0)
                    RenderUtil.rect(rowX, rowY, rowX + s.w, rowY + ROW_H, 0x11FFFFFF);
                boolean loadingSkeleton = tab ? PikaConfig.tabLoadingSkeleton
                                              : PikaConfig.hudLoadingSkeleton;
                if (info != null && st == null && loadingSkeleton) {
                    loadingRow(f, info, rowX, rowY, s, now,
                               tab ? PikaConfig.tabColumnDividers : PikaConfig.hudColumnDividers);
                    y += ROW_H;
                    continue;
                }
                if (info != null && s.head > 0)
                    head(info, rowX + 1, rowY + 2);
                int x = rowX + s.head, sx = -1, sw = 0;
                for (int i = 0; i < s.cols.size(); i++) {
                    if (span != null && i >= span[0] && i <= span[1]) {
                        if (sx < 0)
                            sx = x;
                        sw += s.widths[i];
                        x += s.widths[i];
                        continue;
                    }
                    String value;
                    if (info == null)
                        value = s.demo ? TabFormat.demo(s.cols.get(i), r)
                                       : (s.cols.get(i).id.equals("name") ? "§7No players" : "");
                    else if (s.cols.get(i).id.equals("name") &&
                            (tab ? PikaConfig.combineRankWithName : PikaConfig.hudCombineRankWithName)) {
                        String name = TabFormat.name(info), rank = TabFormat.combinedSecondary(info, st);
                        combined(f, name, rank, x, rowY, s.widths[i]);
                        x += s.widths[i];
                        continue;
                    } else
                        value = TabFormat.cell(s.cols.get(i), info, st);
                    cell(f, value, x, rowY, s.widths[i], ROW_H, s.cols.get(i).right);
                    x += s.widths[i];
                }
                if (status != null && sx >= 0)
                    center(f, status, sx + sw / 2, rowY, sw, ROW_H);
                if (tab ? PikaConfig.tabColumnDividers : PikaConfig.hudColumnDividers)
                    dividers(rowX, rowY, rowY + ROW_H, s.head, s.widths, span);
                y += ROW_H;
            }
            }
        }
    }
    private static void loadingRow(OverlayText f, NetworkPlayerInfo info, int rowX, int rowY,
                                   Snapshot s, long now, boolean showDividers) {
        double wave = PikaConfig.lowPerformanceMode ? .5
            : .5 + .5 * Math.sin(now / 180_000_000.0);
        int alpha = 30 + (int)(wave * 28);
        int color = RenderUtil.withAlpha(0xAEB5BF, alpha);
        if (s.head > 0) {
            head(info, rowX + 1, rowY + 2);
        }
        int x = rowX + s.head;
        for (int i = 0; i < s.widths.length; i++) {
            TabFormat.Column column = s.cols.get(i);
            if (column.id.equals("name"))
                cell(f, TabFormat.name(info), x, rowY, s.widths[i], ROW_H, false);
            else if (column.id.equals("ping"))
                cell(f, TabFormat.cell(column, info, null), x, rowY, s.widths[i], ROW_H, true);
            else {
                int width = Math.max(3, s.widths[i] - CELL_PAD * 2);
                int fill = Math.max(3, Math.round(width * (i % 3 == 0 ? .62f : i % 3 == 1 ? .78f : .48f)));
                RenderUtil.roundedRect(x + CELL_PAD, rowY + 4,
                                       x + CELL_PAD + fill, rowY + 8, 2, color);
            }
            x += s.widths[i];
        }
        if (showDividers) dividers(rowX, rowY, rowY + ROW_H, s.head, s.widths, null);
    }
    private static void animate(Snapshot s, float bx, float by, float progress, int animation) {
        float eased = 1f - (float)Math.pow(1f - progress, 3);
        if (animation == 1) {
            GlStateManager.translate(0, -18 * (1 - eased), 0);
        } else if (animation == 2 || animation == 3 || animation == 4) {
            float zoom = .85f + .15f * eased;
            if (animation == 3) {
                float t = progress - 1f;
                zoom = 1f + .22f * (2.70158f * t * t * t + 1.70158f * t * t);
            }
            if (animation == 4)
                zoom = progress < .8f ? .08f + 1.04f * (progress / .8f)
                    : 1.12f - .12f * ((progress - .8f) / .2f);
            float cx = bx + s.w / 2f, cy = by + s.h / 2f;
            GlStateManager.translate(cx, cy, 0);
            GlStateManager.scale(zoom, zoom, 1);
            GlStateManager.translate(-cx, -cy, 0);
        }
    }
    private static void cell(OverlayText f, String s, int x, int y, int w, int h, boolean right) {
        f.drawFitted(s, x + CELL_PAD, y, Math.max(1, w - CELL_PAD * 2), h, right);
    }
    private static void combined(OverlayText f, String name, String rank, int x, int y, int w) {
        int room = Math.max(2, w - CELL_PAD * 2 - COMBINED_GAP);
        int rankRoom = Math.min(room / 2, f.getStringWidth(rank));
        int nameRoom = room - rankRoom;
        f.drawFitted(name, x + CELL_PAD, y, nameRoom, ROW_H, false);
        f.drawFitted(rank, x + w - CELL_PAD - rankRoom, y, rankRoom, ROW_H, true);
    }
    private static void center(OverlayText f, String s, int cx, int y, int width, int height) {
        int textWidth = f.getStringWidth(s);
        int room = Math.min(width, textWidth);
        f.drawFitted(s, cx - room / 2f, y, room, height, false);
    }
    private static void dividers(int bx, int t, int b, int head, int[] ws, int[] span) {
        int x = bx + head;
        if (head > 0 && ws.length > 0)
            RenderUtil.rect(x, t, x + 1, b, 0x33FFFFFF);
        for (int i = 0; i < ws.length - 1; i++) {
            x += ws[i];
            if (span == null || i < span[0] || i >= span[1])
                RenderUtil.rect(x, t, x + 1, b, 0x33FFFFFF);
        }
    }
    private static void head(NetworkPlayerInfo info, int x, int y) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            mc.getTextureManager().bindTexture(info.getLocationSkin());
            GlStateManager.color(1, 1, 1, 1);
            Gui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, 8, 8, 64, 64);
            Gui.drawScaledCustomSizeModalRect(x, y, 40, 8, 8, 8, 8, 8, 64, 64);
        } catch (Throwable ignored) {
        }
    }
}
