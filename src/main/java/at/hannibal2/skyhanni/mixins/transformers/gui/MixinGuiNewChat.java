package at.hannibal2.skyhanni.mixins.transformers.gui;

import at.hannibal2.skyhanni.features.chat.ChatPeek;
import at.hannibal2.skyhanni.mixins.hooks.FontRendererHook;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    @Inject(method = "getChatOpen", at = @At("HEAD"), cancellable = true)
    public void onIsOpen(CallbackInfoReturnable<Boolean> cir) {
        if (ChatPeek.peek()) cir.setReturnValue(true);
    }

    @Inject(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;enableBlend()V", shift = At.Shift.AFTER))
    private void setTextRenderIsFromChat(int updateCounter, CallbackInfo ci) {
        FontRendererHook.INSTANCE.setCameFromChat(true);
    }

    @Inject(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;disableAlpha()V", shift = At.Shift.BEFORE))
    private void setTextRenderIsntFromChat(int updateCounter, CallbackInfo ci) {
        FontRendererHook.INSTANCE.setCameFromChat(false);
    }
}
