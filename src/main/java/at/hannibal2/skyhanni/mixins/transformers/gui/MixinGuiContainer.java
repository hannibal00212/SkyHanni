package at.hannibal2.skyhanni.mixins.transformers.gui;

import at.hannibal2.skyhanni.data.ToolTipData;
import at.hannibal2.skyhanni.features.event.hoppity.HoppityRabbitTheFishChecker;
import at.hannibal2.skyhanni.mixins.hooks.GuiContainerHook;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Unique
    private final GuiContainerHook skyHanni$hook = new GuiContainerHook(this);

    @Shadow
    private Slot theSlot;

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V", shift = At.Shift.BEFORE), cancellable = true)
    private void closeWindowPressed(CallbackInfo ci) {
        skyHanni$hook.closeWindowPressed(ci);
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void onKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        if (!HoppityRabbitTheFishChecker.shouldContinueWithKeypress(keyCode)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 1))
    private void backgroundDrawn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        skyHanni$hook.backgroundDrawn(mouseX, mouseY, partialTicks);
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void preDraw(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        skyHanni$hook.preDraw(mouseX, mouseY, partialTicks, ci);
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void postDraw(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        skyHanni$hook.postDraw(mouseX, mouseY, partialTicks);
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V", shift = At.Shift.AFTER))
    private void onForegroundDraw(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        skyHanni$hook.foregroundDrawn(mouseX, mouseY, partialTicks);
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slot, CallbackInfo ci) {
        skyHanni$hook.onDrawSlot(slot, ci);
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void onDrawSlotPost(Slot slot, CallbackInfo ci) {
        skyHanni$hook.onDrawSlotPost(slot);
    }

    @Inject(method = "handleMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;windowClick(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        skyHanni$hook.onMouseClick(slot, slotId, clickedButton, clickType, ci);
    }

    @Inject(method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getItemStack()Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.BEFORE,
            ordinal = 1
        )
    )
    public void drawScreen_after(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        skyHanni$hook.onDrawScreenAfter(mouseX, mouseY, ci);
        ToolTipData.INSTANCE.setLastSlot(this.theSlot);
    }
}
