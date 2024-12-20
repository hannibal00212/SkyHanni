package at.hannibal2.skyhanni.mixins.transformers.renderer;

import at.hannibal2.skyhanni.mixins.hooks.RendererLivingEntityHook;
import at.hannibal2.skyhanni.utils.FakePlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RendererLivingEntity.class, priority = 500)
public class MixinContributorRendererEntityLiving<T extends EntityLivingBase> {

    @ModifyVariable(
        method = "rotateCorpse",
        at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BEFORE)
    )
    private String checkNameForUpsideDown(String displayName) {
        if (RendererLivingEntityHook.shouldBeUpsideDown(displayName))
            return "Grumm";
        return displayName;
    }

    @Redirect(
        method = "rotateCorpse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z"))
    private boolean alwaysMarkAsHavingCape(EntityPlayer instance, EnumPlayerModelParts enumPlayerModelParts) {
        // Always returning true here ensures maximal compatibility with other mods. This will no longer block other mods from implementing this same mixin.
        return true;
    }

    @Inject(method = "rotateCorpse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumChatFormatting;getTextWithoutFormattingCodes(Ljava/lang/String;)Ljava/lang/String;", shift = At.Shift.AFTER), cancellable = true)
    private void rotateThePlayer(T bat, float p_77043_2_, float p_77043_3_, float partialTicks, CallbackInfo ci) {
        if (bat instanceof FakePlayer) ci.cancel();
        if (bat instanceof EntityPlayer) {
            RendererLivingEntityHook.rotatePlayer((EntityPlayer) bat);
        }
    }
}
