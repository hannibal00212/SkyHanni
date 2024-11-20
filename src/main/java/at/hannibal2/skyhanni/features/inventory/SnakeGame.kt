package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object SnakeGame {

    private val pattern by RepoPattern.pattern("abiphone.snake.name", "Snake")
    private val config get() = SkyHanniMod.feature.inventory
    private var lastClick = SimpleTimeMark.farPast()

    private var inInventory = false

    private val keys
        get() = with(Minecraft.getMinecraft().gameSettings) {
            mapOf(
                keyBindLeft?.keyCode to 50,
                keyBindForward?.keyCode to 51,
                keyBindRight?.keyCode to 52,
                keyBindBack?.keyCode to 53,
            )
        }

    @SubscribeEvent
    fun onGui(event: GuiKeyPressEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        val chest = event.guiContainer as? GuiChest ?: return

        if (lastClick.passedSince() < 100.milliseconds) return

        for ((key, slot) in keys) {
            if (key?.isKeyHeld() == false) continue
            event.cancel()

            InventoryUtils.clickSlot(slot, chest.inventorySlots.windowId, 2, 3)

            lastClick = SimpleTimeMark.now()
            break
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        inInventory = pattern.matches(event.inventoryName)
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.snakeGameKeybinds
}
