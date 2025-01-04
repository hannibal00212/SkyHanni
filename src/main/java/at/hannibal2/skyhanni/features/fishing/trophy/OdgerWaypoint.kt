package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object OdgerWaypoint {

    private val config get() = SkyHanniMod.feature.fishing.trophyFishing
    private val odgerLocation = LorenzVec(-373, 207, -808)

    private var trophyFishInInventory = false

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled() || !event.isMod(10)) return
        trophyFishInInventory = InventoryUtils.getItemsInOwnInventory()
            .any { it.getItemCategoryOrNull() == ItemCategory.TROPHY_FISH }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (FishingAPI.holdingLavaRod) return
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
