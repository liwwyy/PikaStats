package dev.movi.pikastats.mixin;

import dev.movi.pikastats.api.StatsManager;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.model.PlayerStats;
import dev.movi.pikastats.tab.TabFormat;
import dev.movi.pikastats.util.ScoreboardUtil;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Changes only the rendered label, preserving vanilla teams/visibility/sneaking. */
@Mixin(RendererLivingEntity.class)
public abstract class PlayerNametagMixin {
    @Redirect(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
        at = @At(value = "INVOKE",
            target =
                "Lnet/minecraft/entity/EntityLivingBase;getDisplayName()Lnet/minecraft/util/IChatComponent;"))
    private IChatComponent
    pikastats$label(EntityLivingBase entity) {
        IChatComponent original = entity.getDisplayName();
        String name = original.getFormattedText();
        if (!(entity instanceof EntityPlayer) || !ScoreboardUtil.shouldRenderNametags())
            return original;
        PlayerStats stats = StatsManager.peek(entity.getName());
        if (stats == null || stats.failed)
            return original;
        String value = TabFormat.status(stats);
        if (value == null)
            value = stats.nametagValue(PikaConfig.nametagStat);
        if (PikaConfig.nametagDisplayMode == 0)
            return original;
        return new ChatComponentText(name + " §r§f[" + value + "§r§f]");
    }

    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;getDisplayName()Lnet/minecraft/util/IChatComponent;"))
    private void pikastats$aboveName(EntityLivingBase entity, double x, double y, double z,
                                     CallbackInfo ci) {
        if (PikaConfig.nametagDisplayMode != 0 || !(entity instanceof EntityPlayer)
            || !ScoreboardUtil.shouldRenderNametags()) return;
        PlayerStats stats = StatsManager.peek(entity.getName());
        if (stats == null || stats.failed) return;
        String value = TabFormat.status(stats);
        if (value == null) value = stats.nametagValue(PikaConfig.nametagStat);
        ((RenderLabelInvoker) this).pikastats$renderLivingLabel(entity, "§f" + value,
            x, y + 0.25D, z, entity.isSneaking() ? 32 : 64);
    }
}
