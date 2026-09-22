package dev.movi.pikastats.party;

import dev.movi.pikastats.util.LegacyText;
import dev.movi.pikastats.util.ScoreboardUtil;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public final class PartyTracker {
    private static volatile boolean inParty;
    private static final Set<String> MEMBERS =
        java.util.Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,16}"),
                                 BRACKET = Pattern.compile("\\[[^\\]]*]"),
                                 LEADING_COUNT = Pattern.compile("^\\(\\d+\\)\\s*");
    private static final Pattern JOIN = Pattern.compile(
        "Party\\s*▏\\s*✚\\s*([A-Za-z0-9_]{3,16})\\s+joined the party!?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SELF_JOIN = Pattern.compile(
        "^(?:Party\\s*▏\\s*)?(?:You (?:have )?joined (?:the |[A-Za-z0-9_]{3,16}(?:'s)? )?party|You are now in (?:the |[A-Za-z0-9_]{3,16}(?:'s)? )?party)",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern LEAVE =
        Pattern.compile("Party\\s*▏\\s*▬\\s*([A-Za-z0-9_]{3,16})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern YOUR = Pattern.compile("▏\\s*Your Party\\b", Pattern.CASE_INSENSITIVE),
                                 OWNER = Pattern.compile("▏\\s*Owner:\\s*(.+)$", Pattern.CASE_INSENSITIVE),
                                 MEMBER_LINE =
                                     Pattern.compile("▏\\s*Members:\\s*(.*)$", Pattern.CASE_INSENSITIVE);
    private int pending = -1, captureTicks;
    private boolean capture;
    private final LinkedHashSet<String> captureMembers = new LinkedHashSet<String>();
    private String last = "";
    private long lastAt;

    public static boolean isInParty() {
        return inParty;
    }
    public static boolean isMember(String username) {
        return username != null && MEMBERS.contains(key(username));
    }
    public void tick() {
        if (pending >= 0) {
            if (pending == 0) {
                pending = -1;
                requestList();
            } else
                pending--;
        }
        if (capture && ++captureTicks > 40) {
            if (!captureMembers.isEmpty())
                commit();
            else
                cancel();
        }
    }
    @SubscribeEvent
    public void chat(ClientChatReceivedEvent e) {
        if (e.message != null)
            handle(e.message.getUnformattedText());
    }
    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        resetState();
    }
    private void handle(String raw) {
        if (!ScoreboardUtil.isPikaNetwork())
            return;
        String text = normalize(raw);
        if (text.isEmpty())
            return;
        long now = System.currentTimeMillis();
        if (text.equals(last) && now - lastAt < 150L)
            return;
        last = text;
        lastAt = now;
        if (PartyMessages.isDisband(text)) {
            resetState();
            return;
        }
        if (SELF_JOIN.matcher(text).find()) {
            enter();
            pending = 20;
            return;
        }
        Matcher m = JOIN.matcher(text);
        if (m.find()) {
            enter();
            MEMBERS.add(key(m.group(1)));
            pending = 20;
            return;
        }
        m = LEAVE.matcher(text);
        if (m.find()) {
            String u = m.group(1);
            if (isLocal(u))
                resetState();
            else {
                MEMBERS.remove(key(u));
                captureMembers.remove(key(u));
            }
            return;
        }
        if (YOUR.matcher(text).find()) {
            enter();
            capture = true;
            captureTicks = 0;
            captureMembers.clear();
            String local = local();
            if (local != null)
                captureMembers.add(key(local));
            return;
        }
        if (!capture)
            return;
        m = OWNER.matcher(text);
        if (m.find()) {
            String u = extract(m.group(1));
            if (u != null)
                captureMembers.add(key(u));
            captureTicks = 0;
            return;
        }
        m = MEMBER_LINE.matcher(text);
        if (m.find()) {
            String body = LEADING_COUNT.matcher(m.group(1).trim()).replaceFirst("");
            for (String part : body.split(",")) {
                String u = extract(part);
                if (u != null)
                    captureMembers.add(key(u));
            }
            commit();
        }
    }
    private void requestList() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!inParty || !ScoreboardUtil.isPikaNetwork() || mc.thePlayer == null)
            return;
        try {
            mc.thePlayer.sendChatMessage("/party list");
        } catch (Throwable ignored) {
        }
    }
    private void enter() {
        inParty = true;
        String u = local();
        if (u != null)
            MEMBERS.add(key(u));
    }
    private void commit() {
        MEMBERS.clear();
        MEMBERS.addAll(captureMembers);
        String u = local();
        if (u != null)
            MEMBERS.add(key(u));
        inParty = true;
        cancel();
    }
    private void cancel() {
        capture = false;
        captureTicks = 0;
        captureMembers.clear();
    }
    public static void reset() {
        inParty = false;
        MEMBERS.clear();
    }
    private void resetState() {
        reset();
        last = "";
        lastAt = 0L;
        pending = -1;
        cancel();
    }
    private static String extract(String raw) {
        String clean = BRACKET.matcher(raw).replaceAll(" ");
        Matcher m = USERNAME.matcher(clean);
        String last = null;
        while (m.find()) last = m.group();
        return last;
    }
    private static boolean isLocal(String u) {
        String l = local();
        return l != null && l.equalsIgnoreCase(u);
    }
    private static String local() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer == null ? null : mc.thePlayer.getGameProfile().getName();
    }
    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
    private static String normalize(String s) {
        return LegacyText.plain(s)
            .replace('\u00A0', ' ')
            .replaceAll("\\s+", " ")
            .trim()
            .replaceFirst("^\\[CHAT]\\s*", "")
            .trim();
    }
}
