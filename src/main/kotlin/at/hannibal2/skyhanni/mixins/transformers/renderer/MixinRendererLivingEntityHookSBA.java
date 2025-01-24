package at.hannibal2.skyhanni.mixins.transformers.renderer;

import at.hannibal2.skyhanni.mixins.hooks.RendererLivingEntityHook;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "codes/biscuit/skyblockaddons/asm/hooks/RendererLivingEntityHook")
public class MixinRendererLivingEntityHookSBA<T extends EntityLivingBase> {

    @Inject(method = "equals", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onEquals(String displayName, Object otherString, CallbackInfoReturnable<Boolean> cir) {
        if (RendererLivingEntityHook.shouldBeUpsideDown(displayName)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isWearing", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsWearing(EntityPlayer player, EnumPlayerModelParts p_175148_1_, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

}
