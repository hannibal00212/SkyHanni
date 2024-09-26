package at.hannibal2.skyhanni.features.inventory.wardrobe

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.features.inventory.wardrobe.CustomWardrobe.clickSlot
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object CustomWardrobeKeybinds {

    private val config get() = SkyHanniMod.feature.inventory.customWardrobe
    private val keybinds
        get() = listOf(
            config.keybinds.slot1,
            config.keybinds.slot2,
            config.keybinds.slot3,
            config.keybinds.slot4,
            config.keybinds.slot5,
            config.keybinds.slot6,
            config.keybinds.slot7,
            config.keybinds.slot8,
            config.keybinds.slot9,
        )
    var lastClick = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onGui(event: GuiKeyPressEvent) {
        if (handlePress()) event.cancel()
    }

    @SubscribeEvent
    fun onMouse(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (handlePress()) event.isCanceled = true
    }

    private fun handlePress(): Boolean {
        if (!isEnabled()) return false
        val slots = WardrobeAPI.slots.filter { it.isInCurrentPage() }.filterNot { config.onlyFavorites && !it.favorite }

        for ((index, key) in keybinds.withIndex()) {
            if (!key.isKeyHeld()) continue
            if (lastClick.passedSince() < 200.milliseconds) break
            val slot = slots.getOrNull(index) ?: continue

            slot.clickSlot()
            lastClick = SimpleTimeMark.now()
            return true
        }

        return false
    }

    fun allowMouseClick() = isEnabled() && keybinds.filter { it < 0 }.any { it.isKeyHeld() }
    fun allowKeyboardClick() = isEnabled() && keybinds.filter { it > 0 }.any { it.isKeyHeld() }

    private fun isEnabled() = LorenzUtils.inSkyBlock && WardrobeAPI.inCustomWardrobe && config.keybinds.slotKeybindsToggle && config.enabled
}
