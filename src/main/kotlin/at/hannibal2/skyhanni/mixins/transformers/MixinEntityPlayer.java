package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.data.EntityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {

    @ModifyVariable(
        method = "getDisplayName",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ChatStyle;setInsertion(Ljava/lang/String;)Lnet/minecraft/util/ChatStyle;", shift = At.Shift.AFTER)
    )
    public IChatComponent getDisplayName(IChatComponent value) {
        return EntityData.getDisplayName((EntityPlayer) (Object) this, (ChatComponentText) value);
    }
}
