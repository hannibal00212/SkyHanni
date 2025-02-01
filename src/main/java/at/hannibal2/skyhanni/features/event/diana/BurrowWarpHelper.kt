package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import net.minecraft.client.Minecraft
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BurrowWarpHelper {

    private val config get() = SkyHanniMod.feature.event.diana

    var currentWarp: WarpPoint? = null

    private var lastWarpTime = SimpleTimeMark.farPast()
    private var lastWarp: WarpPoint? = null

    @HandleEvent
    fun onKeyPress(event: KeyPressEvent) {
        if (!DianaApi.isDoingDiana()) return
        if (!config.burrowNearestWarp) return

        if (event.keyCode != config.keyBindWarp) return
        if (Minecraft.getMinecraft().currentScreen != null) return

        currentWarp?.let {
            if (lastWarpTime.passedSince() > 5.seconds) {
                lastWarpTime = SimpleTimeMark.now()
                HypixelCommands.warp(it.name)
                lastWarp = currentWarp
                GriffinBurrowHelper.lastTitleSentTime = SimpleTimeMark.now() + 2.seconds
                TitleManager.optionalResetTitle { currentTitle ->
                    currentTitle.startsWith("§bWarp to ")
                }
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (event.message == "§cYou haven't unlocked this fast travel destination!") {
            if (lastWarpTime.passedSince() < 1.seconds) {
                lastWarp?.let {
                    it.unlocked = false
                    ChatUtils.chat("Detected not having access to warp point §b${it.displayName}§e!")
                    ChatUtils.chat("Use §c/shresetburrowwarps §eonce you have activated this travel scroll.")
                    lastWarp = null
                    currentWarp = null
                }
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        lastWarp = null
        currentWarp = null
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Diana Burrow Nearest Warp")

        if (!DianaApi.isDoingDiana()) {
            event.addIrrelevant("not doing diana")
            return
        }
        if (!config.burrowNearestWarp) {
            event.addIrrelevant("disabled in config")
            return
        }
        val target = GriffinBurrowHelper.targetLocation
        if (target == null) {
            event.addIrrelevant("targetLocation is null")
            return
        }

        val list = mutableListOf<String>()
        shouldUseWarps(target, list)
        event.addData(list)
    }

    fun shouldUseWarps(target: SkyHanniVec3d, debug: MutableList<String>? = null) {
        debug?.add("target: ${target.printWithAccuracy(1)}")
        val playerLocation = LocationUtils.playerLocation()
        debug?.add("playerLocation: ${playerLocation.printWithAccuracy(1)}")
        val warpPoint = getNearestWarpPoint(target)
        debug?.add("warpPoint: ${warpPoint.displayName}")

        val playerDistance = playerLocation.distance(target)
        debug?.add("playerDistance: ${playerDistance.roundTo(1)}")
        val warpDistance = warpPoint.distance(target)
        debug?.add("warpDistance: ${warpDistance.roundTo(1)}")
        val difference = playerDistance - warpDistance
        debug?.add("difference: ${difference.roundTo(1)}")
        val setWarpPoint = difference > 10
        debug?.add("setWarpPoint: $setWarpPoint")
        currentWarp = if (setWarpPoint) warpPoint else null
    }

    private fun getNearestWarpPoint(location: SkyHanniVec3d) =
        WarpPoint.entries.filter { it.unlocked && !it.ignored() }.map { it to it.distance(location) }
            .sorted().first().first

    fun resetDisabledWarps() {
        WarpPoint.entries.forEach { it.unlocked = true }
        ChatUtils.chat("Reset disabled burrow warps.")
    }

    enum class WarpPoint(
        val displayName: String,
        val location: SkyHanniVec3d,
        private val extraBlocks: Int,
        val ignored: () -> Boolean = { false },
        var unlocked: Boolean = true,
    ) {

        HUB("Hub", SkyHanniVec3d(-3, 70, -70), 2),
        CASTLE("Castle", SkyHanniVec3d(-250, 130, 45), 10),
        CRYPT("Crypt", SkyHanniVec3d(-190, 74, -88), 15, { config.ignoredWarps.crypt }),
        DA("Dark Auction", SkyHanniVec3d(91, 74, 173), 2),
        MUSEUM("Museum", SkyHanniVec3d(-75, 76, 81), 2),
        WIZARD("Wizard", SkyHanniVec3d(42.5, 122.0, 69.0), 5, { config.ignoredWarps.wizard }),
        STONKS("Stonks", SkyHanniVec3d(-52.5, 70.0, -49.5), 5, { config.ignoredWarps.stonks }),
        ;

        fun distance(other: SkyHanniVec3d): Double = other.distance(location) + extraBlocks
    }
}
