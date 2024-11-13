package at.hannibal2.skyhanni.api.hypixelapi

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.hypixel.modapi.HypixelAPIJoinEvent
import at.hannibal2.skyhanni.events.hypixel.modapi.HypixelAPIServerChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardTitleUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.hypixel.data.type.GameType
import net.hypixel.data.type.ServerType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Suppress("MemberVisibilityCanBePrivate")
@SkyHanniModule
object HypixelLocationAPI {

    var inHypixel: Boolean = false
        private set

    var inSkyblock: Boolean = false
        private set

    var island: IslandType = IslandType.NONE
        private set

    var serverId: String? = null
        private set

    var inAlpha: Boolean = false
        private set

    var serverType: ServerType? = null
        private set

    var mode: String? = null
        private set

    var map: String? = null
        private set

    var isGuest: Boolean = false
        private set

    val config = SkyHanniMod.feature.dev.hypixelModApi

    private val logger = LorenzLogger("debug/hypixel_api")

    private var sentIslandEvent = false
    private var internalIsland = IslandType.NONE

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onHypixelJoin(event: HypixelAPIJoinEvent) {
        logger.log(event.toString())
        logger.log("Connected to Hypixel")
        inAlpha = event.alpha
        inHypixel = true
    }

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onServerChange(event: HypixelAPIServerChangeEvent) {
        logger.log(event.toString())
        inHypixel = true
        inSkyblock = event.serverType == GameType.SKYBLOCK
        serverType = event.serverType
        mode = event.mode
        map = event.map
        serverId = event.serverName

        // Set island to NONE when you leave skyblock
        if (!inSkyblock) {
            internalIsland = IslandType.NONE
            changeIsland()
            return
        }
        val mode = event.mode ?: return

        val newIsland = IslandType.getByIdOrUnknown(mode)
        if (newIsland == IslandType.UNKNOWN) {
            ChatUtils.debug("Unknown island detected: '$newIsland'")
            logger.log("Unknown Island detected: '$newIsland'")
        } else {
            logger.log("Island detected: '$newIsland'")
        }
        internalIsland = newIsland

        // If the island has a guest variant, we wait for the scoreboard packet to confirm if it's a guest island or not
        if (internalIsland.hasGuestVariant()) {
            sentIslandEvent = false
        } else {
            sentIslandEvent = true
            changeIsland()
        }
    }

    @HandleEvent
    fun onScoreboardTitle(event: ScoreboardTitleUpdateEvent) {
        if (!inHypixel || !inSkyblock || sentIslandEvent || !event.isSkyblock) return
        isGuest = event.title.trim().removeColor().endsWith("GUEST")
        sentIslandEvent = true

        if (internalIsland.hasGuestVariant() && isGuest) {
            internalIsland = internalIsland.guestVariant()
        }

        changeIsland()
    }

    private fun changeIsland() {
        if (internalIsland == island) return
        val oldIsland = island
        island = internalIsland
        logger.log("Island change: '$oldIsland' -> '$island'")
        // TODO: post island change event
        return
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Hypixel Mod API")
        event.addIrrelevant {
            addAll(debugData.map(::dataToString))
        }
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) = reset()

    private fun reset() {
        logger.log("Disconnected")
        inHypixel = false
        inSkyblock = false
        island = IslandType.NONE
        serverId = null
        inAlpha = false
        serverType = null
        mode = null
        map = null
        isGuest = false
        sentIslandEvent = false
        internalIsland = IslandType.NONE
    }

    fun checkHypixel(hypixel: Boolean) {
        if (hypixel == inHypixel) return
        sendError("Hypixel")
    }

    fun checkSkyblock(skyblock: Boolean) {
        if (skyblock == inSkyblock) return
        sendError("SkyBlock")
    }

    fun checkIsland(otherIsland: IslandType) {
        if (otherIsland == island) return
        if (otherIsland == IslandType.NONE) return
        sendError("Island")
    }

    fun checkServerId(otherId: String?) {
        if (serverId == otherId) return
        sendError("ServerId")
    }

    private fun sendError(message: String) {
        if (!config) return
        val data = debugData
        logger.log("ERROR: ${data.joinToString(transform = ::dataToString)}")
        @Suppress("SpreadOperator")
        ErrorManager.logErrorStateWithData(
            "$message check comparison with HypixelModAPI failed. Please report in discord.",
            "$message comparison failed",
            *data,
            betaOnly = true,
            noStackTrace = true,
        )
    }

    private val debugData
        get() = arrayOf(
            "HypixelData.skyBlock" to HypixelData.skyBlock,
            "inSkyblock" to inSkyblock,
            "HypixelData.hypixelLive" to HypixelData.hypixelLive,
            "inHypixel" to inHypixel,
            "HypixelData.skyBlockIsland" to HypixelData.skyBlockIsland,
            "island" to island,
            "HypixelData.serverId" to HypixelData.serverId,
            "serverId" to serverId,
            "serverType" to serverType,
            "map" to map,
        )

    private fun dataToString(pair: Pair<String, Any?>) = "${pair.first}: ${pair.second}"

}
