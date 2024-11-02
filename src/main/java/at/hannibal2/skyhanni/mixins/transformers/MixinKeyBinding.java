package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.data.model.TextInput;
import at.hannibal2.skyhanni.features.garden.farming.GardenCustomKeybinds;
import at.hannibal2.skyhanni.test.graph.GraphEditor;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {
    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private static void noOnTick(int keyCode, CallbackInfo ci) {
        GardenCustomKeybinds.onTick(keyCode, ci);
    }

    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    public void noIsKeyDown(CallbackInfoReturnable<Boolean> cir) {
        GardenCustomKeybinds.isKeyDown((KeyBinding) (Object) this, cir);
        TextInput.Companion.onMinecraftInput((KeyBinding) (Object) this, cir);
        GraphEditor.INSTANCE.onMinecraftInput((KeyBinding) (Object) this, cir);
    }

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void noIsPressed(CallbackInfoReturnable<Boolean> cir) {
        TextInput.Companion.onMinecraftInput((KeyBinding) (Object) this, cir);
        GraphEditor.INSTANCE.onMinecraftInput((KeyBinding) (Object) this, cir);
    }
}
