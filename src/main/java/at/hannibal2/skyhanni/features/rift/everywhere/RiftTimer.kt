package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ActionBarStatsData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ActionBarValueUpdateEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthDisplayEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object RiftTimer {

    private val config get() = RiftApi.config.timer

    private val patternGroup = RepoPattern.group("rift.everywhere")

    /**
     * REGEX-TEST: 3150 §aф
     */
    private val nametagPattern by patternGroup.pattern(
        "nametag.timer",
        "(?<time>\\d+) §aф",
    )

    private var display = emptyList<String>()
    private var maxTime = 0.seconds
    private var currentTime = 0.seconds
    private var latestTime = 0.seconds
    private val changes = mutableMapOf<Long, String>()

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        display = emptyList()
        maxTime = 0.seconds
        latestTime = 0.seconds
        currentTime = 0.seconds
    }

    @HandleEvent
    fun onActionBarValueUpdate(event: ActionBarValueUpdateEvent) {
        if (event.updated != ActionBarStatsData.RIFT_TIME) return
        if (!isEnabled() || RiftApi.inRiftRace) return

        val newTime = TimeUtils.getDuration(event.updated.value.replace("m", "m "))
        if (newTime > maxTime) {
            maxTime = newTime
        }
        currentTime = newTime
        update()
    }

    // prevents rift time from pausing during Rift Race
    // (hypixel hides the action bar during the race)
    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled() || !RiftApi.inRiftRace) return
        if (!event.isMod(5)) return
        val newTime = TimeUtils.getDuration(Minecraft.getMinecraft().thePlayer.experienceLevel.toString() + " s")
        currentTime = newTime
        update()
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

    @HandleEvent
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

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (RiftApi.inMirrorVerse) return

        config.timerPosition.renderStrings(display, posLabel = "Rift Timer")
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onEntityHealthDisplay(event: EntityHealthDisplayEvent) {
        if (!config.nametag) return
        val time = nametagPattern.matchMatcher(event.text) {
            group("time")?.toIntOrNull()
        } ?: return
        event.text = "${time.seconds.format()} §aф"
    }

    fun isEnabled() = RiftApi.inRift() && config.enabled
}
