package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GuiNewChat.class)
public interface AccessorMixinGuiNewChat {

    @Accessor("chatLines")
    List<ChatLine> getChatLines_skyhanni();

    @Accessor("chatLines")
    void setChatLines_skyhanni(List<ChatLine> chatLines);

    @Accessor("drawnChatLines")
    List<ChatLine> getDrawnChatLines_skyhanni();

    @Accessor("drawnChatLines")
    void setDrawnChatLines_skyhanni(List<ChatLine> drawnChatLines);
}
