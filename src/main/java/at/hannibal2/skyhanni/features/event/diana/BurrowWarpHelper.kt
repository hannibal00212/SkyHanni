package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BurrowWarpHelper {

    private val config get() = SkyHanniMod.feature.event.diana

    var currentWarp: WarpPoint? = null

    private var lastWarpTime = SimpleTimeMark.farPast()
    private var lastWarp: WarpPoint? = null

    @SubscribeEvent
    fun onKeyClick(event: LorenzKeyPressEvent) {
        if (!DianaAPI.isDoingDiana()) return
        if (!config.burrowNearestWarp) return

        if (event.keyCode != config.keyBindWarp) return
        if (Minecraft.getMinecraft().currentScreen != null) return

        currentWarp?.let {
            if (lastWarpTime.passedSince() > 5.seconds) {
                lastWarpTime = SimpleTimeMark.now()
                HypixelCommands.warp(it.name)
                lastWarp = currentWarp
                GriffinBurrowHelper.lastTitleSentTime = SimpleTimeMark.now() + 2.seconds
                TitleManager.optionalResetTitle {
                    it.startsWith("§bWarp to ")
                }
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

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

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastWarp = null
        currentWarp = null
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Diana Burrow Nearest Warp")

        if (!DianaAPI.isDoingDiana()) {
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

    fun shouldUseWarps(target: LorenzVec, debug: MutableList<String>? = null) {
        debug?.add("target: ${target.printWithAccuracy(1)}")
        val playerLocation = LocationUtils.playerLocation()
        debug?.add("playerLocation: ${playerLocation.printWithAccuracy(1)}")
        val warpPoint = getNearestWarpPoint(target)
        debug?.add("warpPoint: ${warpPoint.displayName}")

        val playerDistance = playerLocation.distance(target)
        debug?.add("playerDistance: ${playerDistance.round(1)}")
        val warpDistance = warpPoint.distance(target)
        debug?.add("warpDistance: ${warpDistance.round(1)}")
        val difference = playerDistance - warpDistance
        debug?.add("difference: ${difference.round(1)}")
        val setWarpPoint = difference > 10
        debug?.add("setWarpPoint: $setWarpPoint")
        currentWarp = if (setWarpPoint) warpPoint else null
    }

    private fun getNearestWarpPoint(location: LorenzVec) =
        WarpPoint.entries.filter { it.unlocked && !it.ignored() }.map { it to it.distance(location) }
            .sorted().first().first

    fun resetDisabledWarps() {
        WarpPoint.entries.forEach { it.unlocked = true }
        ChatUtils.chat("Reset disabled burrow warps.")
    }

    enum class WarpPoint(
        val displayName: String,
        val location: LorenzVec,
        private val extraBlocks: Int,
        val ignored: () -> Boolean = { false },
        var unlocked: Boolean = true,
    ) {

        HUB("Hub", LorenzVec(-3, 70, -70), 2),
        CASTLE("Castle", LorenzVec(-250, 130, 45), 10),
        CRYPT("Crypt", LorenzVec(-190, 74, -88), 15, { config.ignoredWarps.crypt }),
        DA("Dark Auction", LorenzVec(91, 74, 173), 2),
        MUSEUM("Museum", LorenzVec(-75, 76, 81), 2),
        WIZARD("Wizard", LorenzVec(42.5, 122.0, 69.0), 5, { config.ignoredWarps.wizard }),
        STONKS("Stonks", LorenzVec(-52.5, 70.0, -49.5), 5, { config.ignoredWarps.stonks }),
        ;

        fun distance(other: LorenzVec): Double = other.distance(location) + extraBlocks
    }
}
