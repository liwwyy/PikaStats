package dev.movi.pikastats.command;

import dev.movi.pikastats.PikaStatsMod;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

/** /pikastats and /pikaoverlay open the OneConfig page. */
public final class PikaStatsCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "pikastats";
    }
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/pikastats";
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("pikaoverlay");
    }
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        PikaStatsMod.requestConfigOpen();
    }
}
