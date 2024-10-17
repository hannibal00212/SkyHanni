package at.hannibal2.skyhanni.features.inventory.tiarelay

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.SkyhanniChatEvent
import at.hannibal2.skyhanni.events.SkyhanniRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TiaRelayWaypoints {

    private val config get() = SkyHanniMod.feature.inventory.helper.tiaRelay
    private var waypoint: LorenzVec? = null
    private var waypointName: String? = null
    private var island = IslandType.NONE

    init {
        Relay.entries.forEach { it.chatPattern }
    }

    @SubscribeEvent
    fun onChat(event: SkyhanniChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.nextWaypoint) return

        val message = event.message
        Relay.entries.firstOrNull { it.checkChatMessage(message) }?.let { relay ->
            waypoint = relay.waypoint
            waypointName = relay.relayName
            island = relay.island
            return
        }

        if (message == "§aYou completed the maintenance on the relay!") {
            waypoint = null
            island = IslandType.NONE
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: SkyhanniRenderWorldEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (config.allWaypoints) {
            for (relay in Relay.entries) {
                if (relay.island == LorenzUtils.skyBlockIsland) {
                    event.drawWaypointFilled(relay.waypoint, LorenzColor.LIGHT_PURPLE.toColor())
                    event.drawDynamicText(relay.waypoint, "§d" + relay.relayName, 1.5)
                }
            }
            return
        }

        if (!config.nextWaypoint) return
        if (LorenzUtils.skyBlockIsland != island) return

        waypoint?.let {
            event.drawWaypointFilled(it, LorenzColor.LIGHT_PURPLE.toColor())
            event.drawDynamicText(it, "§d" + waypointName!!, 1.5)
        }
    }
}
