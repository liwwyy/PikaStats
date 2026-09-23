package dev.movi.pikastats.mixin;

import dev.movi.pikastats.api.StatsManager;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.model.PlayerStats;
import dev.movi.pikastats.tab.TabFormat;
import dev.movi.pikastats.util.ScoreboardUtil;
import net.minecraft.client.Minecraft;
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
            x, y + pikastats$statHeight(entity), z, entity.isSneaking() ? 32 : 64);
    }

    private static double pikastats$statHeight(EntityLivingBase entity) {
        Minecraft mc = Minecraft.getMinecraft();
        // RenderPlayer draws the below-name objective at the original height,
        // then moves the username up by one font line. Reserve a second line
        // above it whenever that objective is visible at vanilla's 10-block range.
        boolean belowName = !entity.isSneaking() && mc.theWorld != null
            && mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(2) != null
            && mc.getRenderManager().livingPlayer != null
            && entity.getDistanceSqToEntity(mc.getRenderManager().livingPlayer) < 100D;
        int lines = belowName ? 2 : 1;
        return mc.fontRendererObj.FONT_HEIGHT * 1.15D * 0.02666667D * lines;
    }
}
