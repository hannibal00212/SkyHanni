package at.hannibal2.skyhanni.mixins.transformers.neu;

import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = StorageOverlay.class, remap = false)
public class MixinStorageOverlay {

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderTail(CallbackInfo ci) {
        EstimatedItemValue.INSTANCE.renderInNeuStorageOverlay();
    }
}
