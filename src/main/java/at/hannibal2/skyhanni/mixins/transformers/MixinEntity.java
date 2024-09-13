package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.data.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @ModifyVariable(
        method = "getDisplayName",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ChatStyle;setInsertion(Ljava/lang/String;)Lnet/minecraft/util/ChatStyle;", shift = At.Shift.AFTER)
    )
    public ChatComponentText getDisplayName(ChatComponentText value) {
        return EntityData.getDisplayName((Entity) (Object) this, value);
    }
}
