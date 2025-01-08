package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.ChatHoverEvent;
import at.hannibal2.skyhanni.events.chat.TabCompletionEvent;
import at.hannibal2.skyhanni.mixins.hooks.GuiChatHook;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Shadow
    protected GuiTextField inputField;

    @ModifyVariable(
        method = "onAutocompleteResponse",
        at = @At(
            value = "SKYHANNI_FORLOOP_LOCAL_VAR",
            shift = At.Shift.BEFORE,
            args = "lvIndex=1"
        ),
        index = 1,
        argsOnly = true
    )
    private String[] renderItemOverlayPost(String[] originalArray) {
        String inputFieldText = this.inputField.getText();
        String beforeCursor = inputFieldText.substring(0, this.inputField.getCursorPosition());
        TabCompletionEvent tabCompletionEvent = new TabCompletionEvent(beforeCursor, inputFieldText, Arrays.asList(originalArray));
        tabCompletionEvent.post();
        String[] newSuggestions = tabCompletionEvent.intoSuggestionArray();
        if (newSuggestions == null)
            newSuggestions = originalArray;
        return newSuggestions;
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;handleComponentHover(Lnet/minecraft/util/IChatComponent;II)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void chatHoverEvent(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, IChatComponent component) {
        // Only ChatComponentText components can make it to this point

        // Always set the replacement, so if someone is no longer editing the replacement
        // we get the original component back
        GuiChatHook.INSTANCE.setReplacement((ChatComponentText) component);

        new ChatHoverEvent((ChatComponentText) component).post();
    }

    @ModifyArg(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;handleComponentHover(Lnet/minecraft/util/IChatComponent;II)V"), index = 0)
    public IChatComponent replaceWithNewComponent(IChatComponent originalComponent) {
        return GuiChatHook.INSTANCE.getReplacementAsIChatComponent();
    }
}
