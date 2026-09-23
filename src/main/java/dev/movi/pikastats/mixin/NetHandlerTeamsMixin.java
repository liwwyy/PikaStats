package dev.movi.pikastats.mixin;

import dev.movi.pikastats.denick.DenickRegistry;
import dev.movi.pikastats.tab.MatchOverview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerTeamsMixin {
    @Inject(method = "handleTeams", at = @At("HEAD"))
    private void pikastats$captureWaitingRoster(S3EPacketTeams packet, CallbackInfo ci) {
        // The handler is entered once on Netty and again after Minecraft queues
        // the packet on its client thread. Observe only the applied pass.
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread())
            DenickRegistry.observe(packet);
    }

    @Inject(method = "handlePlayerListHeaderFooter", at = @At("HEAD"))
    private void pikastats$captureMatchOverview(S47PacketPlayerListHeaderFooter packet,
                                                CallbackInfo ci) {
        // Unlike handleTeams, this 1.8.9 handler does not requeue on the client
        // thread. MatchOverview publishes its parsed value through a volatile field.
        MatchOverview.update(packet);
    }
}
