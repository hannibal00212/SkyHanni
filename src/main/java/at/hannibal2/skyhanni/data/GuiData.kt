package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.NEURenderEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.features.inventory.wardrobe.CustomWardrobeKeybinds
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import io.github.moulberry.notenoughupdates.NEUApi
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@SkyHanniModule
object GuiData {

    var preDrawEventCancelled = false

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onNeuRenderEvent(event: NEURenderEvent) {
        if (preDrawEventCancelled) event.cancel()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onClick(event: GuiContainerEvent.SlotClickEvent) {
        if (preDrawEventCancelled) event.cancel()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGuiClick(event: GuiScreenEvent.MouseInputEvent.Pre) {

        if (CustomWardrobeKeybinds.allowMouseClick()) return

        if (preDrawEventCancelled) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiKeyPress(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val allowedKeys = Minecraft.getMinecraft().gameSettings.let {
            listOf(
                Keyboard.KEY_ESCAPE,
                it.keyBindInventory.keyCode,
                it.keyBindScreenshot.keyCode,
                it.keyBindFullscreen.keyCode,
            )
        }
        if (allowedKeys.any { it.isKeyHeld() }) return

        if (CustomWardrobeKeybinds.allowKeyboardClick()) return

        if (preDrawEventCancelled) event.isCanceled = true
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        DelayedRun.runNextTick {
            if (Minecraft.getMinecraft().currentScreen !is GuiChest) {
                preDrawEventCancelled = false
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        preDrawEventCancelled = false
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        preDrawEventCancelled = false
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onGuiOpen(event: GuiOpenEvent) {
        if (preDrawEventCancelled) {
            NEUApi.setInventoryButtonsToDisabled()
        }
    }
}
