package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthDisplayEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object RiftTimer {

    private val config get() = RiftAPI.config.timer

    private val repoGroup = RepoPattern.group("rift.everywhere")
    private val timePattern by repoGroup.pattern(
        "timer",
        "§(?<color>[a7])(?<time>.*)ф Left.*"
    )

    /**
     * REGEX-TEST: 3150 §aф
     */
    private val nametagPattern by repoGroup.pattern(
        "nametag.timer",
        "(?<time>\\d+) §aф"
    )

    private var display = emptyList<String>()
    private var maxTime = 0.seconds
    private var currentTime = 0.seconds
    private var latestTime = 0.seconds
    private val changes = mutableMapOf<Long, String>()

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        display = emptyList()
        maxTime = 0.seconds
        latestTime = 0.seconds
        currentTime = 0.seconds
    }

    //todo use ActionBarValueUpdateEvent
    @SubscribeEvent
    fun onActionBarUpdate(event: ActionBarUpdateEvent) {
        if (!isEnabled()) return

        event.actionBar.split("     ").matchFirst(timePattern) {
            val color = group("color")
            val newTime = TimeUtils.getDuration(group("time").replace("m", "m "))

            if (color == "7") {
                if (newTime > maxTime) {
                    maxTime = newTime
                }
            }
            currentTime = newTime
            update()
        }
    }

    private fun update() {
        if (currentTime != latestTime) {
            val diff = (currentTime - latestTime) + 1.seconds
            latestTime = currentTime
            if (latestTime != maxTime) {
                addDiff(diff)
            }
        }

        val currentFormat = currentTime.format()
        val percentage =
            LorenzUtils.formatPercentage(currentTime.inWholeMilliseconds.toDouble() / maxTime.inWholeMilliseconds)
        val percentageFormat = if (config.percentage.get()) " §7($percentage)" else ""
        val maxTimeFormat = if (config.maxTime.get()) "§7/§b" + maxTime.format() else ""
        val color = if (currentTime <= 1.minutes) "§c" else if (currentTime <= 5.minutes) "§e" else "§b"
        val firstLine = "§eRift Timer: $color$currentFormat$maxTimeFormat$percentageFormat"

        display = buildList {
            add(firstLine)
            changes.keys.removeIf { System.currentTimeMillis() > it + 4_000 }
            for (entry in changes.values) {
                add(entry)
            }
        }
    }

    private fun addDiff(diff: Duration) {
        val diffFormat = if (diff > 0.seconds) {
            "§a+${diff.format()}"
        } else if (diff < 0.seconds) {
            "§c-${(-diff).format()}"
        } else return

        changes[System.currentTimeMillis()] = diffFormat
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(
            config.percentage,
            config.maxTime,
        ) {
            if (isEnabled()) {
                update()
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (LorenzUtils.skyBlockArea == "Mirrorverse") return

        config.timerPosition.renderStrings(display, posLabel = "Rift Timer")
    }

    @SubscribeEvent
    fun onEntityHealthDisplay(event: EntityHealthDisplayEvent) {
        if (!RiftAPI.inRift() || !config.nametag) return
        val time = nametagPattern.matchMatcher(event.text) {
            group("time")?.toIntOrNull()
        } ?: return
        event.text = "${time.seconds.format()} §aф"
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled
}
