package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.mixins.hooks.ChatLineData;
import at.hannibal2.skyhanni.mixins.hooks.GuiChatHook;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatLine.class)
public class MixinChatLine implements ChatLineData {

    @Unique
    private IChatComponent skyHanni$fullComponent;

    @Unique
    @NotNull
    @Override
    public IChatComponent getSkyHanni_fullComponent() {
        return skyHanni$fullComponent;
    }

    @Unique
    @Override
    public void setSkyHanni_fullComponent(@NotNull IChatComponent fullComponent) {
        skyHanni$fullComponent = fullComponent;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(int updateCounterCreated, IChatComponent line, int chatLineID, CallbackInfo ci) {
        skyHanni$fullComponent = GuiChatHook.getCurrentComponent();
    }

}
