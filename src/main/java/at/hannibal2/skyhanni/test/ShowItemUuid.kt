package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemUuid

@SkyHanniModule
object ShowItemUuid {

    @HandleEvent
    fun onToolTip(event: ToolTipEvent) {
        if (!SkyHanniMod.feature.dev.debug.showItemUuid) return
        event.itemStack.getItemUuid()?.let {
            event.toolTip.add("§7Item UUID: '$it'")
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "dev.showItemUuid", "dev.debug.showItemUuid")
    }
}
