package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.NEURenderEvent;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(NEUOverlay.class)
public class MixinNEUOverlay {

    @Shadow(remap = false)
    private boolean disabled;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void render(boolean hoverInv, CallbackInfo ci) {
        System.out.println("NEUOverlay.render disabled: " + disabled);
        if (new NEURenderEvent().post()) {
            System.out.println("NEUOverlay.render cancelled");
            try {
                new Exception("NEUOverlay.render cancelled").printStackTrace();
            } catch (Exception ignored) {
            }
            ci.cancel();
        }
    }
}
