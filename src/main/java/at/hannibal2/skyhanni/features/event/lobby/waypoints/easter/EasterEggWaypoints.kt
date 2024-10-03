package at.hannibal2.skyhanni.features.event.lobby.waypoints.easter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object EasterEggWaypoints {

    private val config get() = SkyHanniMod.feature.event.lobbyWaypoints.easterEgg
    private var closest: EasterEgg? = null
    private var isEgg: Boolean = false

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!config.allWaypoints && !config.allEntranceWaypoints) return
        if (!isEgg) return

        if (!isEnabled()) return

        val message = event.message
        if (message.startsWith("§a§lYou found an Easter Egg! §r") || message == "§aYou have received the §bsuper reward§a!" || message == "§cYou already found this egg!") {
            val egg = EasterEgg.entries.minByOrNull { it.waypoint.distanceSqToPlayer() }!!
            egg.found = true
            if (closest == egg) {
                closest = null
            }
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!config.allWaypoints && !config.allEntranceWaypoints) return
        if (!isEnabled()) return

        isEgg = checkScoreboardEasterSpecific()

        if (isEgg) {
            if (config.onlyClosest) {
                if (closest == null) {
                    val notFoundEggs = EasterEgg.entries.filter { !it.found }
                    if (notFoundEggs.isEmpty()) return
                    closest = notFoundEggs.minByOrNull { it.waypoint.distanceSqToPlayer() }!!
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (!isEgg) return

        if (config.allWaypoints) {
            for (egg in EasterEgg.entries) {
                if (!egg.shouldShow()) continue
                event.drawWaypointFilled(egg.waypoint, LorenzColor.AQUA.toColor())
                event.drawDynamicText(egg.waypoint, "§3" + egg.eggName, 1.5)
            }
        }

        if (config.allEntranceWaypoints) {
            for (eggEntrance in EggEntrance.entries) {
                if (!eggEntrance.easterEgg.any { it.shouldShow() }) continue
                event.drawWaypointFilled(eggEntrance.waypoint, LorenzColor.YELLOW.toColor())
                event.drawDynamicText(eggEntrance.waypoint, "§e" + eggEntrance.eggEntranceName, 1.5)
            }
        }
    }

    private fun EasterEgg.shouldShow(): Boolean {
        if (found) {
            return false
        }

        return if (config.onlyClosest) closest == this else true
    }

    // TODO use regex with the help of knowing the original lore. Will most likely need to wait until next egg event

    /*
        Title:
        §e§lHYPIXEL

        '§703/14/24  §8L30A'
        '  '
        'Rank: §bMVP§d+§b'
        'Achievements: §e5,370'
        'Hypixel Level: 140'
        '      '
        'Lobby: §a5'
        'Players: §a32,791'
        '         '
        '§bEaster 2024'
        'Event Level: §31'
        'Easter Eggs: §a0/§a30'
        '             '
        '§ewww.hypixel.net'
     */
    private fun checkScoreboardEasterSpecific(): Boolean {
        val a = ScoreboardData.sidebarLinesFormatted.any { it.contains("Hypixel Level") }
        val b = ScoreboardData.sidebarLinesFormatted.any { it.contains("Easter") }
        val c = ScoreboardData.sidebarLinesFormatted.any { it.contains("Easter Eggs") }
        return a && b && c
    }

    private fun isEnabled() = HypixelData.hypixelLive && !LorenzUtils.inSkyBlock
}
