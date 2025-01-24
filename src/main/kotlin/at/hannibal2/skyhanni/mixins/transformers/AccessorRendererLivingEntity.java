package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RendererLivingEntity.class)
public interface AccessorRendererLivingEntity<T extends EntityLivingBase>
    extends AccessorRender<T> {
    @Invoker("setBrightness")
    boolean setBrightness_skyhanni(T entityLivingBaseIn, float partialTicks, boolean combineTextures);

    @Invoker("unsetBrightness")
    void setBrightness_skyhanni();
}
