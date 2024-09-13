package at.hannibal2.skyhanni.features.event.lobby.waypoints.christmas

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.WinterAPI
import at.hannibal2.skyhanni.data.jsonobjects.repo.EventWaypointsJson
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.event.lobby.waypoints.EventWaypoint
import at.hannibal2.skyhanni.features.event.lobby.waypoints.loadEventWaypoints
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// todo: create abstract class for this and BasketWaypoints
@SkyHanniModule
object PresentWaypoints {

    private val config get() = SkyHanniMod.feature.event.lobbyWaypoints.christmasPresent
    private var presentLocations = mapOf<String, MutableSet<EventWaypoint>>()
    private var presentEntranceLocations = mapOf<String, MutableSet<EventWaypoint>>()
    private var closest: EventWaypoint? = null

    private val presentSet get() = presentLocations[HypixelData.lobbyType]
    private val presentEntranceSet get() = presentEntranceLocations[HypixelData.lobbyType]

    private val patternGroup = RepoPattern.group("event.lobby.waypoint.presents")
    private val presentAlreadyFoundPattern by patternGroup.pattern(
        "foundalready",
        "§cYou have already found this present!",
    )
    private val presentFoundPattern by patternGroup.pattern(
        "found",
        "§aYou found a.*present! §r§e\\(§r§b\\d+§r§e/§r§b\\d+§r§e\\)",
    )
    private val allFoundPattern by patternGroup.pattern(
        "foundall",
        "§aCongratulations! You found all the presents in every lobby!",
    )

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (!isEnabled()) return
        closest = null
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        processChatMessage(event.message)
    }

    // <editor-fold desc = "Chat Message Processing">
    private fun processChatMessage(message: String) {
        when {
            presentFoundPattern.matches(message) || presentAlreadyFoundPattern.matches(message) -> handlePresentFound()
            allFoundPattern.matches(message) -> handleAllPresentsFound()
        }
    }

    private fun handlePresentFound() {
        presentSet?.minByOrNull { it.position.distanceSqToPlayer() }?.let { present ->
            present.isFound = true
            markEntranceAsFound(present)
            if (closest == present) closest = null
        }
    }

    private fun markEntranceAsFound(present: EventWaypoint) {
        presentEntranceSet?.find { present.name == it.name }?.let { it.isFound = true }
    }

    private fun handleAllPresentsFound() {
        // If all presents are found, disable the feature
        ChatUtils.chat("Congratulations! As all presents are found, we are disabling the Christmas Present Waypoints feature.")
        config.allWaypoints = false
        config.allEntranceWaypoints = false
    }

    // </editor-fold>

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled() && config.onlyClosest && HypixelData.locrawData != null && closest == null) return
        val notFoundPresents = presentSet?.filterNot { it.isFound }
        if (notFoundPresents?.isEmpty() == true) return
        closest = notFoundPresents?.minByOrNull { it.position.distanceSqToPlayer() } ?: return
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        presentSet?.let { event.drawWaypoints(it, config.allWaypoints, LorenzColor.GOLD, "§6") }
        presentEntranceSet?.let { event.drawWaypoints(it, config.allEntranceWaypoints, LorenzColor.YELLOW, "§e") }
    }

    private fun LorenzRenderWorldEvent.drawWaypoints(
        waypoints: Set<EventWaypoint>, shouldDraw: Boolean, color: LorenzColor, prefix: String,
    ) {
        if (!shouldDraw) return
        for (waypoint in waypoints) {
            if (!waypoint.shouldShow()) continue
            this.drawWaypointFilled(waypoint.position, color.toColor())
            this.drawDynamicText(waypoint.position, "$prefix${waypoint.name}", 1.5)
        }
    }

    private fun EventWaypoint.shouldShow(): Boolean = !isFound && (!config.onlyClosest || closest == this)

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<EventWaypointsJson>("EventWaypoints")
        presentLocations = loadEventWaypoints(data.presents)
        presentEntranceLocations = loadEventWaypoints(data.presentsEntrances)
    }

    private fun isEnabled(): Boolean =
        LorenzUtils.inHypixelLobby && (config.allWaypoints || config.allEntranceWaypoints) && WinterAPI.isDecember()
}
