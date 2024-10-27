package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FocusMode {

    private val config get() = SkyHanniMod.feature.inventory.focusMode

    private var active = false

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLorenzToolTip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (event.toolTip.isEmpty()) return
        val keyName = KeyboardManager.getKeyName(config.toggleKey)

        val hint = !config.disableHint && !config.alwaysEnabled && keyName != "NONE"
        if (active || config.alwaysEnabled) {
            event.toolTip = buildList {
                add(event.toolTip.first())
                if (hint) {
                    add("§7Focus Mode from SkyHanni active!")
                    add("Press $keyName to disable!")
                }
            }.toMutableList()
        } else {
            if (hint) {
                event.toolTip.add(1, "§7Press $keyName to enable Focus Mode from SkyHanni!")
            }
        }
    }

    @SubscribeEvent
    fun onLorenzTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (config.alwaysEnabled) return
        if (!config.toggleKey.isKeyClicked()) return
        active = !active
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && InventoryUtils.inContainer() && config.enabled
}
