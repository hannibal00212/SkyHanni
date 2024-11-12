package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.MiningAPI
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ItemInHandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.mining.OreMinedEvent
import at.hannibal2.skyhanni.features.mining.FlowstateHelper.blockBreakStreak
import at.hannibal2.skyhanni.features.mining.FlowstateHelper.getSpeedBonus
import at.hannibal2.skyhanni.features.mining.FlowstateHelper.getStreakColor
import at.hannibal2.skyhanni.features.mining.FlowstateHelper.getTimerColor
import at.hannibal2.skyhanni.features.mining.FlowstateHelper.streakEndTimer
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object FlowstateHelper {
    private val config get() = SkyHanniMod.feature.mining.flowstateHelper

    var streakEndTimer = SimpleTimeMark.farPast()
        private set
    var blockBreakStreak = 0
        private set

    private var display: List<Renderable> = emptyList()
    private var displayDirty = false
    private var displayHibernating = true
    private var timeSinceHibernation = SimpleTimeMark.farPast()
    private var timeSinceMax = SimpleTimeMark.farPast()
    private var displayMaxed = false

    private var flowstateCache: Int? = null

    @HandleEvent(onlyOnSkyblock = true)
    fun onBlockMined(event: OreMinedEvent) {
        if (!MiningAPI.inCustomMiningIsland()) return
        if (flowstateCache == null) return

        displayHibernating = false
        streakEndTimer = 10.seconds.fromNow()
        blockBreakStreak += event.extraBlocks.values.sum()
        displayDirty = true
        createDisplay()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!MiningAPI.inCustomMiningIsland()) return

        attemptClearDisplay()
    }

    private fun attemptClearDisplay() {
        if (streakEndTimer.isInFuture()) return
        blockBreakStreak = 0
        timeSinceMax = SimpleTimeMark.farPast()
        displayMaxed = false
        displayDirty = true
        if (!displayHibernating) timeSinceHibernation = SimpleTimeMark.now()
        displayHibernating = true
        createDisplay()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!MiningAPI.inCustomMiningIsland() || !config.enabled) return
        if (flowstateCache == null && !streakEndTimer.isInFuture()) return

        if (shouldAutoHide()) return
        if (display.isEmpty() || streakEndTimer.isInFuture()) {
            createDisplay()
        }

        config.position.renderRenderables(display, extraSpace = 1, "Flowstate Helper")
    }

    private fun shouldAutoHide(): Boolean {
        if (config.autoHide < 0) return false
        val time = 10.seconds - config.autoHide.seconds
        return (streakEndTimer - time).isInPast()
    }

    private fun createDisplay() {
        if (displayDirty) {
            displayDirty = false
            FlowstateElements.STREAK.create()
            FlowstateElements.SPEED.create()
        }
        if (!displayHibernating) {
            FlowstateElements.TIMER.create()
            FlowstateElements.COMPACT.create()
        }
        display = config.appearance.map { it.renderable }
    }

    fun getSpeedBonus(): Int {
        val flowstateLevel = flowstateCache ?: 0

        return if (blockBreakStreak >= 200) {
            if (!displayMaxed) {
                displayMaxed = true
                timeSinceMax = SimpleTimeMark.now()
            }
            200 * flowstateLevel
        } else blockBreakStreak * flowstateLevel
    }

    @SubscribeEvent
    fun onChangeItem(event: ItemInHandChangeEvent) {
        hasFlowstate()
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        streakEndTimer = SimpleTimeMark.farPast()
        attemptClearDisplay()
    }

    fun getTimerColor(timeRemaining: Duration): String {
        if (!config.colorfulTimer) return "§b"
        return when (timeRemaining) {
            in 0.seconds..2.seconds -> "§c"
            in 2.seconds..4.seconds -> "§#§e§c§7§b§3§6§/"
            in 4.seconds..6.seconds -> "§e"
            in 6.seconds..8.seconds -> "§a"
            in 8.seconds..10.seconds -> "§2"
            else -> "§6"
        }
    }

    fun getStreakColor(streak: Int = blockBreakStreak): String = if (streak < 200) "§e" else "§a"

    private fun hasFlowstate() {
        val enchantList = InventoryUtils.getItemInHand()?.getEnchantments() ?: run {
            flowstateCache = null
            return
        }
        if ("ultimate_flowstate" !in enchantList) {
            flowstateCache = null
            return
        }
        flowstateCache = enchantList.getValue("ultimate_flowstate")
    }
}

enum class FlowstateElements(val label: String, var renderable: Renderable = Renderable.string("")) {
    TITLE("§d§lFlowstate Helper", Renderable.string("§d§lFlowstate Helper")),
    TIMER("§fTime Remaining: §b9.71"),
    STREAK("§7Streak: §f123/200"),
    SPEED("§6+600⸕"),
    COMPACT("§7x40 §6+120⸕ §b(9.71)"),
    ;

    override fun toString() = label

    fun create() {
        if (this !in config.appearance) return

        renderable = when (this) {
            TIMER -> {
                val timeRemaining = streakEndTimer.timeUntil().coerceAtLeast(0.seconds)

                Renderable.string("§7Time Remaining: ${timeRemaining.formatTime()}")
            }

            STREAK -> {
                val textColor = getStreakColor()
                val string = "§7Streak: $textColor$blockBreakStreak"
                Renderable.string(string + if (blockBreakStreak < 200) "§8/200" else "")
            }

            SPEED -> {
                Renderable.string("§6+${getSpeedBonus()}⸕")
            }

            COMPACT -> {
                val timeRemaining = streakEndTimer.timeUntil().coerceAtLeast(0.seconds)

                Renderable.string(
                    "§7x${getStreakColor()}$blockBreakStreak " +
                        "§6+${getSpeedBonus()}⸕ " +
                        timeRemaining.formatTime(),
                )
            }

            else -> return
        }
    }

    companion object {
        private val config get() = SkyHanniMod.feature.mining.flowstateHelper

        private fun Duration.formatTime(): String {
            return getTimerColor(this) + format(TimeUnit.SECOND, true, maxUnits = 2, showSmallerUnits = true)
        }

        @JvmField
        val defaultOption = listOf(
            TITLE, TIMER, STREAK, SPEED,
        )
    }
}
