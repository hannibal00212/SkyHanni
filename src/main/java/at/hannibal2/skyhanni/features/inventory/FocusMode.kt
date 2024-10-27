package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LorenzUtils

@SkyHanniModule
object FocusMode {

    private val config get() = SkyHanniMod.feature.inventory.focusMode

    private var active = false

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onLorenzToolTip(event: SkyHanniToolTipEvent) {
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

    @HandleEvent
    fun onLorenzTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        if (config.alwaysEnabled) return
        if (!config.toggleKey.isKeyClicked()) return
        active = !active
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && InventoryUtils.inContainer() && config.enabled
}
