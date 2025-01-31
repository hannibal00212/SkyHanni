package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.pests.PestTimerConfig.PestTimerTextEntry
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.garden.farming.CropClickEvent
import at.hannibal2.skyhanni.events.garden.pests.PestSpawnEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.sendTitle
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.hasGroup
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PestSpawnTimer {

    private val config get() = PestApi.config.pestTimer

    private val patternGroup = RepoPattern.group("garden.pests")

    /**
     * REGEX-TEST:  Cooldown: §r§a§lREADY
     * REGEX-TEST:  Cooldown: §r§e1m 58s
     * REGEX-TEST:  Cooldown: §r§e1m
     * REGEX-TEST:  Cooldown: §r§e58s
     * REGEX-TEST:  Cooldown: §r§c§lMAX PESTS
     */

    private val pestCooldownPattern by patternGroup.pattern(
        "cooldown",
        "\\sCooldown: §r§.(?:§.)?(?:(?<minutes>\\d+)m)? ?(?:(?<seconds>\\d+)s)?(?<ready>READY)?(?<maxPests>MAX PESTS)?.*",
    )

    private val pestSpawnTimes: MutableList<Int> = mutableListOf()

    private var averageSpawnTime: Int = pestSpawnTimes.average().toInt()

    var lastSpawnTime = SimpleTimeMark.farPast()

    private var pestCooldownEndTime = SimpleTimeMark.farPast()

    private var lastCropBrokenTime = SimpleTimeMark.farPast()

    private var longestCropBrokenTime: Duration = 0.seconds

    private var pestSpawned = false

    private var hasWarned = false

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PESTS)) return

        if (pestSpawned) {
            hasWarned = false
            pestSpawned = false
        }

        pestCooldownPattern.firstMatcher(event.widget.lines) {
            val minutes = groupOrNull("minutes")?.formatInt()
            val seconds = groupOrNull("seconds")?.formatInt()
            val ready = hasGroup("ready")
            val maxPests = hasGroup("maxPests")

            if (ready) {
                pestCooldownEndTime = SimpleTimeMark.now() - 1.seconds
                return
            }

            if (maxPests) return

            if (minutes == null && seconds == null) return

            val tablistCooldownEnd = SimpleTimeMark.now() + (minutes?.minutes ?: 0.seconds) + (seconds?.seconds ?: 0.seconds)

            if (shouldSetCooldown(tablistCooldownEnd, minutes, seconds)) {

                // hypixel generally rounds times down to nearest min if over 6 mins, we'll overestimate and add a min

                pestCooldownEndTime = if (seconds == null && (minutes ?: 0) >= 6) {
                    tablistCooldownEnd + 1.minutes
                } else {
                    tablistCooldownEnd
                }
            }
        }
    }

    @HandleEvent
    fun onPestSpawn(event: PestSpawnEvent) {
        val spawnTime = lastSpawnTime.passedSince()

        if (!lastSpawnTime.isFarPast()) {
            if (longestCropBrokenTime.inWholeSeconds.toInt() <= config.averagePestSpawnTimeout) {
                pestSpawnTimes.add(spawnTime.inWholeSeconds.toInt())
                ChatUtils.debug("Added pest spawn time ${spawnTime.format()}")
            }

            if (config.pestSpawnChatMessage) {
                ChatUtils.chat("Pests spawned in §b${spawnTime.format()}")
            }

            pestSpawned = true
        }

        longestCropBrokenTime = 0.seconds

        averageSpawnTime = pestSpawnTimes.average().toInt()

        lastSpawnTime = SimpleTimeMark.now()

    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        if (config.onlyWithVacuum xor config.onlyWithFarmingTool) {
            if (config.onlyWithFarmingTool && !GardenApi.hasFarmingToolInHand()) return
            if (config.onlyWithVacuum && !PestApi.hasVacuumInHand()) return
        } else if (config.onlyWithFarmingTool && config.onlyWithVacuum) {
            if (!GardenApi.hasFarmingToolInHand() && !PestApi.hasVacuumInHand()) return
        }

        config.position.renderRenderables(drawDisplay(), posLabel = "Pest Spawn Timer")
    }

    @HandleEvent
    fun onCropBreak(event: CropClickEvent) {
        if (event.clickType != ClickType.LEFT_CLICK) return
        val timeDiff = lastCropBrokenTime.passedSince()

        if (timeDiff > longestCropBrokenTime) {
            longestCropBrokenTime = timeDiff
        }

        lastCropBrokenTime = SimpleTimeMark.now()
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (hasWarned || !config.pestCooldownOverWarning) return

        if ((pestCooldownEndTime - config.cooldownWarningTime.seconds).isInPast()) {
            warn()
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onIslandChange(event: IslandChangeEvent) {
        reset()
    }

    private fun shouldSetCooldown(tabCooldownEnd: SimpleTimeMark, minutes: Int?, seconds: Int?): Boolean {

        // tablist can have up to 6 seconds of delay, besides this, there is no scenario where tablist will overestimate cooldown
        if (tabCooldownEnd > ((pestCooldownEndTime) + 6.seconds)) return true

        // tablist sometimes rounds down to nearest min
        if ((tabCooldownEnd + 1.minutes) < (pestCooldownEndTime) && seconds == null) return true

        // tablist shouldn't underestimate if it is displaying seconds
        if ((tabCooldownEnd + 1.seconds) < (pestCooldownEndTime) && seconds != null) return true

        return false
    }

    private fun drawDisplay(): List<Renderable> {
        val lineMap = mutableMapOf<PestTimerTextEntry, Renderable>()

        val lastPestSpawned = if (lastSpawnTime.isFarPast()) {
            "§cNo pest spawned since joining."
        } else {
            val timeSinceLastPest = lastSpawnTime.passedSince().format()
            "§eLast pest spawned: §b$timeSinceLastPest ago"
        }

        lineMap[PestTimerTextEntry.PEST_TIMER] = Renderable.string(lastPestSpawned)

        val pestCooldown = if (!TabWidget.PESTS.isActive) {
            "§cPests Widget not detected! Enable via /widget!"
        } else {
            var cooldownValue = if (!pestCooldownEndTime.isFarPast()) pestCooldownEndTime.timeUntil().format() else "§cUnknown"
            if (cooldownValue == "Soon") cooldownValue = "§aReady!"

            "§ePest Cooldown: §b$cooldownValue"
        }

        lineMap[PestTimerTextEntry.PEST_COOLDOWN] = Renderable.string(pestCooldown)

        val averageSpawn = averageSpawnTime.seconds.format()

        if (averageSpawnTime != 0) {
            lineMap[PestTimerTextEntry.AVERAGE_PEST_SPAWN] = Renderable.string("§eAverage time to spawn: §b$averageSpawn")
        }

        return formatDisplay(lineMap)
    }

    private fun formatDisplay(lineMap: Map<PestTimerTextEntry, Renderable>): List<Renderable> {
        return config.defaultDisplay.mapNotNull { lineMap[it] }
    }

    private fun warn() {
        sendTitle("§cPests Cooldown Expired!", duration = 3.seconds)
        ChatUtils.chat("§cPest spawn cooldown has expired!")
        SoundUtils.playPlingSound()
        hasWarned = true
    }

    private fun reset() {
        pestCooldownEndTime = SimpleTimeMark.farPast()

        lastCropBrokenTime = SimpleTimeMark.farPast()

        longestCropBrokenTime = 0.seconds

        pestSpawned = false

        hasWarned = false
    }

    fun isEnabled() = GardenApi.inGarden() && config.enabled
}
