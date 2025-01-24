package at.hannibal2.skyhanni.mixins.transformers.gui;

import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryBasic.class)
public class MixinInventoryBasic {

    @Shadow
    private ItemStack[] inventoryContents;

    @Inject(method = "getStackInSlot", at = @At("HEAD"), cancellable = true)
    public void getStackInSlot(int index, CallbackInfoReturnable<ItemStack> cir) {
        ReplaceItemEvent.postEvent((InventoryBasic) (Object) this, this.inventoryContents, index, cir);
    }
}
