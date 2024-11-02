package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.data.ActionBarStatsData
import at.hannibal2.skyhanni.events.ActionBarValueUpdateEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthDisplayEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object RiftTimer {

    private val config get() = RiftAPI.config.timer

    private val repoGroup = RepoPattern.group("rift.everywhere")
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

    @SubscribeEvent
    fun onActionBarValueUpdate(event: ActionBarValueUpdateEvent) {
        if (event.updated != ActionBarStatsData.RIFT_TIME) return
        if (!isEnabled() || RiftAPI.inRiftRace) return

        val newTime = TimeUtils.getDuration(event.updated.value.replace("m", "m "))
        if (newTime > maxTime) {
            maxTime = newTime
        }
        currentTime = newTime
        update()
    }

    // prevents rift time from pausing during Rift Race
    // (hypixel hides the action bar during the race)
    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled() || !RiftAPI.inRiftRace) return
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
        if (RiftAPI.inMirrorVerse) return

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
