package at.hannibal2.skyhanni.mixins.transformers.renderer;

import at.hannibal2.skyhanni.events.render.EntityRenderLayersEvent;
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RendererLivingEntity.class, priority = 1001)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> extends Render<T> {

    protected MixinRendererLivingEntity(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "getColorMultiplier", at = @At("HEAD"), cancellable = true)
    private void setColorMultiplier(T entity, float lightBrightness, float partialTickTime, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(RenderLivingEntityHelper.internalSetColorMultiplier(entity));
    }

    @Redirect(method = "setBrightness", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;hurtTime:I", opcode = Opcodes.GETFIELD))
    private int changeHurtTime(EntityLivingBase entity) {
        return RenderLivingEntityHelper.internalChangeHurtTime(entity);
    }

    @Inject(method = "renderLayers", at = @At("HEAD"), cancellable = true)
    private void onRenderLayersPre(T entity, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_, CallbackInfo ci) {
        if (new EntityRenderLayersEvent.Pre<>(entity).post()) {
            ci.cancel();
        }
    }
}
