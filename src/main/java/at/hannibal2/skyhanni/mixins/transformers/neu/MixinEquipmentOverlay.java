package at.hannibal2.skyhanni.mixins.transformers.neu;

import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue;
import io.github.moulberry.notenoughupdates.overlays.EquipmentOverlay;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(value = EquipmentOverlay.class, remap = false)
public class MixinEquipmentOverlay {

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getTooltip(Lnet/minecraft/entity/player/EntityPlayer;Z)Ljava/util/List;"))
    public void drawSlot(ItemStack stack, int x, int y, int mouseX, int mouseY, List<String> tooltip, CallbackInfo ci) {
        EstimatedItemValue.INSTANCE.onNeuDrawEquipment(stack);
    }

}
