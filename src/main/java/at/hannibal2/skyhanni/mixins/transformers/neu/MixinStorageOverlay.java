package at.hannibal2.skyhanni.mixins.transformers.neu;

import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StorageOverlay.class, remap = false)
public class MixinStorageOverlay {

    @Shadow
    private int guiTop;

    @Shadow
    private int guiLeft;

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void renderHead(CallbackInfo ci) {
        EstimatedItemValue.INSTANCE.setNeuStorageOffset(new Pair<>(guiLeft, guiTop));

    }
    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderTail(CallbackInfo ci) {
        EstimatedItemValue.INSTANCE.setNeuStorageOffset(null);

    }
}
