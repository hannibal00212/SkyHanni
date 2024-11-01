package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.NEURenderEvent;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NEUOverlay.class)
public class MixinNEUOverlay {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void render(boolean hoverInv, CallbackInfo ci) {
        if (new NEURenderEvent().post()) {
            ci.cancel();
        }
    }

}
