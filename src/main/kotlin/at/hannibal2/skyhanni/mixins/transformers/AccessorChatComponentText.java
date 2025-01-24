package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.util.ChatComponentText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatComponentText.class)
public interface AccessorChatComponentText {
    @Accessor("text")
    void setText_skyhanni(String text);

    @Accessor("text")
    String text_skyhanni();
}