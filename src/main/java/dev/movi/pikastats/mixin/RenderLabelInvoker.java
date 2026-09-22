package dev.movi.pikastats.mixin;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Render.class)
public interface RenderLabelInvoker {
    @Invoker("renderLivingLabel")
    void pikastats$renderLivingLabel(Entity entity, String label, double x, double y, double z, int range);
}
