package dev.movi.pikastats.command;

import dev.movi.pikastats.api.StatsManager;
import dev.movi.pikastats.model.PlayerStats;
import java.util.concurrent.Future;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/** /stats <player> fetches the same Pika data used by TAB/HUD. */
public final class StatsCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "stats";
    }
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/stats <player>";
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(final ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1)
            throw new WrongUsageException(getCommandUsage(sender));
        final String username = args[0];
        sender.addChatMessage(new ChatComponentText("§aPikaStats §7• Fetching " + username + "…"));
        final Future<PlayerStats> future = StatsManager.fetchNow(username);
        Thread waiter = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final PlayerStats stats = future.get();
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            sendResult(sender, username, stats);
                        }
                    });
                } catch (Exception error) {
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            sender.addChatMessage(
                                new ChatComponentText("§cPikaStats • Failed to fetch " + username));
                        }
                    });
                }
            }
        }, "PikaStats-Pika-StatsCommand");
        waiter.setDaemon(true);
        waiter.start();
    }

    private static void sendResult(ICommandSender sender, String requested, PlayerStats s) {
        String display = s.username == null || s.username.trim().isEmpty() ? requested : s.username;
        String body;
        if (s.apiDisabled)
            body = "§7API DISABLED";
        else if (s.nicked)
            body = "§7NICKED";
        else if (s.noStats)
            body = "§7NO STATS";
        else if (s.failed)
            body = "§c"
                + (s.failureReason == null || s.failureReason.trim().isEmpty() ? "Stats unavailable"
                                                                               : s.failureReason);
        else {
            body = "§fLV " + stripColorFallback(s.levelText()) + " §7• " + s.rankText() + " §7• §fFKDR "
                + stripColorFallback(s.fkdrText()) + " §7• §fWLR " + stripColorFallback(s.wlrText())
                + " §7• §fHWS " + stripColorFallback(s.winstreakText()) + " §7• §fFK "
                + stripColorFallback(s.finalKillsText()) + " §7• §fWins " + stripColorFallback(s.winsText())
                + " §7• §fBeds " + stripColorFallback(s.bedsText());
        }
        sender.addChatMessage(new ChatComponentText("§aPikaStats §7• " + display + " §7• " + body));
    }

    private static String stripColorFallback(String value) {
        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(value);
        return stripped == null ? value : stripped;
    }
}
