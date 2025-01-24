package at.hannibal2.skyhanni.mixins.transformers.gui;

import at.hannibal2.skyhanni.features.misc.visualwords.ModifyVisualWords;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiUtilRenderComponents.class)
public class MixinGuiUtilRenderComponents {

    @Redirect(method = "splitText", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/IChatComponent;getUnformattedTextForChat()Ljava/lang/String;"))
    private static String onSplitText(IChatComponent instance) {
        return ModifyVisualWords.INSTANCE.modifyText(instance.getUnformattedTextForChat());
    }
}
