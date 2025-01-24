package at.hannibal2.skyhanni.mixins.transformers.gui;

import at.hannibal2.skyhanni.data.ToolTipData;
import at.hannibal2.skyhanni.data.model.TextInput;
import at.hannibal2.skyhanni.mixins.hooks.GuiScreenHookKt;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "renderToolTip", at = @At("TAIL"))
    public void renderToolTip(ItemStack stack, int x, int y, CallbackInfo ci) {
        GuiScreenHookKt.renderToolTip(stack);
    }

    @Inject(method = "renderToolTip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getRarity()Lnet/minecraft/item/EnumRarity;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void getTooltip(ItemStack stack, int x, int y, CallbackInfo ci, List<String> list) {
        ToolTipData.getTooltip(stack, list);
        if (list.isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleKeyboardInput", at = @At("HEAD"), cancellable = true)
    public void handleKeyboardInput(CallbackInfo ci) {
        TextInput.Companion.onGuiInput(ci);
    }

    @Redirect(method = "handleComponentClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;isShiftKeyDown()Z"))
    public boolean handleComponentClick() {
        return false;
    }
}
