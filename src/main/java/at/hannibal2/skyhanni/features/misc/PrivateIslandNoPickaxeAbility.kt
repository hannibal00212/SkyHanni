package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.WorldClickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull

@SkyHanniModule
object PrivateIslandNoPickaxeAbility {

    private val config get() = SkyHanniMod.feature.mining

    @HandleEvent(onlyOnIsland = IslandType.PRIVATE_ISLAND)
    fun onClick(event: WorldClickEvent) {
        if (!config.privateIslandNoPickaxeAbility) return
        if (event.clickType != ClickType.RIGHT_CLICK) return

        if (event.itemInHand?.getItemCategoryOrNull()?.let {
                ItemCategory.miningTools.contains(it)
            } == true
        ) {
            event.cancel()
        }
    }
}
