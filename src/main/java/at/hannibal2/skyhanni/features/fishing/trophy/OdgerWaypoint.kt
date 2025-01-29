package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.fishing.FishingApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled

@SkyHanniModule
object OdgerWaypoint {

    private val config get() = SkyHanniMod.feature.fishing.trophyFishing
    private val odgerLocation = LorenzVec(-373, 207, -808)

    private var trophyFishInInventory = false

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled() || !event.isMod(10)) return
        trophyFishInInventory = InventoryUtils.getItemsInOwnInventory()
            .any { it.getItemCategoryOrNull() == ItemCategory.TROPHY_FISH }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (FishingApi.holdingLavaRod) return
        if (!trophyFishInInventory) return

        event.drawWaypointFilled(odgerLocation, LorenzColor.WHITE.toColor())
        event.drawDynamicText(odgerLocation, "Odger", 1.5)
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "fishing.odgerLocation", "fishing.trophyFishing.odgerLocation")
    }

    fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.odgerLocation
}
