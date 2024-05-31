package at.hannibal2.skyhanni.features.event.lobby.waypoints.halloween

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.find
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BasketWaypoints {

    private val config get() = SkyHanniMod.feature.event.lobbyWaypoints.halloweenBasket
    private var closest: Basket? = null
    private var isHalloween: Boolean = false

    private val patternGroup = RepoPattern.group("basketwaypoints")
    private val messagePattern by patternGroup.pattern(
        "message",
        "^§a§lYou found a Candy Basket! §r|^§cYou already found this Candy Basket!$"
    )
    private val levelScoreboardPattern by patternGroup.pattern(
        "level",
        "Hypixel Level"
    )
    private val halloweenScoreboardPattern by patternGroup.pattern(
        "halloween",
        "Halloween"
    )
    private val basketsScoreboardPattern by patternGroup.pattern(
        "baskets",
        "Baskets"
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!config.allWaypoints && !config.allEntranceWaypoints) return
        if (!isHalloween) return

        if (!isEnabled()) return

        if (messagePattern.find(event.message)) {
            val basket = Basket.entries.minByOrNull { it.waypoint.distanceSqToPlayer() }!!
            basket.found = true
            if (closest == basket) {
                closest = null
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!config.allWaypoints && !config.allEntranceWaypoints) return
        if (!isEnabled()) return

        if (event.repeatSeconds(1)) {
            isHalloween = checkScoreboardHalloweenSpecific()
        }

        if (isHalloween) {
            if (config.onlyClosest) {
                if (closest == null) {
                    val notFoundBaskets = Basket.entries.filter { !it.found }
                    if (notFoundBaskets.isEmpty()) return
                    closest = notFoundBaskets.minByOrNull { it.waypoint.distanceSqToPlayer() }!!
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (!isHalloween) return

        if (config.allWaypoints) {
            for (basket in Basket.entries) {
                if (!basket.shouldShow()) continue
                event.drawWaypointFilled(basket.waypoint, LorenzColor.GOLD.toColor())
                event.drawDynamicText(basket.waypoint, "§6" + basket.basketName, 1.5)
            }
        }

        if (config.allEntranceWaypoints) {
            for (basketEntrance in BasketEntrances.entries) {
                if (!basketEntrance.basket.any { it.shouldShow() }) continue
                event.drawWaypointFilled(basketEntrance.waypoint, LorenzColor.YELLOW.toColor())
                event.drawDynamicText(basketEntrance.waypoint, "§e" + basketEntrance.basketEntranceName, 1.5)
            }
            return
        }
    }

    private fun Basket.shouldShow(): Boolean {
        if (found) {
            return false
        }

        return if (config.onlyClosest) closest == this else true
    }

    // TODO use regex with the help of knowing the original lore. Will most likely need to wait until next halloween event
    private fun checkScoreboardHalloweenSpecific(): Boolean {
        val a = ScoreboardData.sidebarLinesFormatted.any { levelScoreboardPattern.find(it) }
        val b = ScoreboardData.sidebarLinesFormatted.any { halloweenScoreboardPattern.find(it) }
        val c = ScoreboardData.sidebarLinesFormatted.any { basketsScoreboardPattern.find(it) }
        return a && b && c
    }

    private fun isEnabled() = HypixelData.hypixelLive && !LorenzUtils.inSkyBlock

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(13, "event.halloweenBasket", "event.lobbyWaypoints.halloweenBasket")
    }
}
