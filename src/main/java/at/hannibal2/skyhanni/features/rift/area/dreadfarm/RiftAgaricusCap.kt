package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.skyblock.GraphAreaChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RiftAgaricusCap {

    private val config get() = RiftAPI.config.area.dreadfarm
    private var startTime = SimpleTimeMark.farPast()
    private var location: LorenzVec? = null
    private var inArea: Boolean = false

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        location = updateLocation()
    }

    @HandleEvent
    fun onAreaChange(event: GraphAreaChangeEvent) {
        if (!RiftAPI.inRift()) return
        inArea = event.area == "Dreadfarm" || event.area == "West Village"
    }

    private fun updateLocation(): LorenzVec? {
        if (InventoryUtils.getItemInHand()?.getInternalName() != RiftAPI.farmingTool) return null
        val currentLocation = BlockUtils.getBlockLookingAt() ?: return null

        when (currentLocation.getBlockAt()) {
            Blocks.brown_mushroom -> {
                return if (location != currentLocation) {
                    startTime = SimpleTimeMark.now()
                    currentLocation
                } else {
                    if (startTime.isFarFuture()) {
                        startTime = SimpleTimeMark.now()
                    }
                    location
                }
            }

            Blocks.red_mushroom -> {
                if (location == currentLocation) {
                    startTime = SimpleTimeMark.farFuture()
                    return location
                }
            }
        }
        return null
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) = reset()

    private fun reset() {
        startTime = SimpleTimeMark.farPast()
        location = null
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        val location = location?.up(0.6) ?: return

        if (startTime.isFarFuture()) {
            event.drawDynamicText(location, "§cClick!", 1.5)
            return
        }

        val format = startTime.passedSince().format(showMilliSeconds = true)
        event.drawDynamicText(location, "§b$format", 1.5)
    }

    fun isEnabled() = RiftAPI.inRift() && inArea && config.agaricusCap

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(9, "rift.area.dreadfarmConfig", "rift.area.dreadfarm")
    }
}
