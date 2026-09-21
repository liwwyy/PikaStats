package dev.movi.pikastats;

import dev.movi.pikastats.api.StatsManager;
import dev.movi.pikastats.command.PikaStatsCommand;
import dev.movi.pikastats.command.StatsCommand;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.hud.StatsHudRenderer;
import dev.movi.pikastats.party.PartyTracker;
import dev.movi.pikastats.util.PingDisplay;
import dev.movi.pikastats.util.PlayerListUtil;
import dev.movi.pikastats.util.ScoreboardUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/** Forge 1.8.9 client entry point for PikaStats. */
@Mod(modid = PikaStatsMod.MOD_ID, name = PikaStatsMod.MOD_NAME, version = PikaStatsMod.VERSION,
    clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public final class PikaStatsMod {
    public static final String MOD_ID = "pikastats";
    public static final String MOD_NAME = "PikaStats";
    public static final String VERSION = BuildInfo.VERSION;

    @Mod.Instance(MOD_ID) public static PikaStatsMod INSTANCE;

    private final PartyTracker partyTracker = new PartyTracker();
    private int ticks;
    private int configOpenTicks = -1;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        try {
            dev.movi.pikastats.render.AssetFiles.directory();
        } catch (java.io.IOException e) {
            org.apache.logging.log4j.LogManager.getLogger("PikaStats")
                .warn("Could not create asset folder", e);
        }
        new PikaConfig();
        try {
            dev.movi.pikastats.render.AssetFiles.prepare();
        } catch (java.io.IOException e) {
            org.apache.logging.log4j.LogManager.getLogger("PikaStats")
                .warn("Could not create asset folders", e);
        }
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(partyTracker);
        ClientCommandHandler.instance.registerCommand(new PikaStatsCommand());
        ClientCommandHandler.instance.registerCommand(new StatsCommand());

        // Forge 1.8.9 does not provide a dependable client-stopping mod event.
        // A daemon-safe shutdown hook prevents the API pool from outliving the JVM.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                StatsManager.shutdown();
                dev.movi.pikastats.render.BackgroundImage.shutdown();
            }
        }, "PikaStats-Pika-Shutdown"));
    }

    public static void requestConfigOpen() {
        if (INSTANCE != null)
            INSTANCE.configOpenTicks = 2;
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        dev.movi.pikastats.denick.DenickRegistry.clear();
        dev.movi.pikastats.tab.MatchOverview.clear();
    }

    @SubscribeEvent
    public void onPlayerListPre(RenderGameOverlayEvent.Pre event) {
        // Cancel before GuiPlayerTabOverlay.renderPlayerlist is entered. Other
        // HUD mods can wrap that method in a matrix push/pop; cancelling it at
        // HEAD skips their matching TAIL pop and overflows the GL stack.
        if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST
            && ScoreboardUtil.shouldRenderTab())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        partyTracker.tick();

        if (configOpenTicks >= 0) {
            if (configOpenTicks-- == 0) {
                configOpenTicks = -1;
                if (PikaConfig.INSTANCE != null)
                    PikaConfig.INSTANCE.openGui();
            }
        }

        ticks++;
        if (ticks % 5 == 0)
            StatsHudRenderer.refresh();
        if (ticks >= 20) {
            ticks = 0;
            try {
                if (ScoreboardUtil.shouldFetchStats()) {
                    List<String> names = new ArrayList<String>();
                    for (NetworkPlayerInfo info : PlayerListUtil.listedPlayers()) {
                        names.add(PlayerListUtil.profileName(info));
                    }
                    StatsManager.prime(names);
                }
                StatsManager.cleanUp();
                PingDisplay.cleanUp();
            } catch (Throwable ignored) {
                // Client-state failures must never take down the render loop.
            }
        }
    }

}
