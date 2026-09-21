package dev.movi.pikastats.mixin;

import dev.movi.pikastats.util.ScoreboardUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hide VanillaHUD's independent TAB background while our TAB HUD owns it. */
@Pseudo
@Mixin(targets = "org.polyfrost.vanillahud.hud.TabList$TabHud", remap = false)
public abstract class VanillaHudTabMixin {
    @Inject(method = "shouldRender()Z", at = @At("HEAD"), cancellable = true,
            require = 0, remap = false)
    private void pikastats$hideVanillaHudTab(CallbackInfoReturnable<Boolean> cir) {
        if (ScoreboardUtil.shouldRenderTab()) cir.setReturnValue(false);
    }
}
