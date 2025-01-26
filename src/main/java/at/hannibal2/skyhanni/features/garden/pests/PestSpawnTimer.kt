package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.garden.farming.CropClickEvent
import at.hannibal2.skyhanni.events.garden.pests.PestSpawnEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
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

    private var pestCooldownEndTime: SimpleTimeMark? = null

    private var lastCropBrokenTime = SimpleTimeMark.farPast()

    private var longestCropBrokenTime: Duration = 0.seconds

    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PESTS)) return
        pestCooldownPattern.firstMatcher(event.widget.lines) {
            val minutes = group("minutes")?.toInt()
            val seconds = group("seconds")?.toInt()
            val ready = group("ready") != null
            val maxPests = group("maxPests") != null

            if (ready) {
                pestCooldownEndTime = SimpleTimeMark.farPast()
                return
            }

            if (maxPests) return

            if (minutes == null && seconds == null) return

            val tablistCooldownEnd = SimpleTimeMark.now() + (minutes?.minutes ?: 0.seconds) + (seconds?.seconds ?: 0.seconds)

            if (pestCooldownEndTime == null) {
                pestCooldownEndTime = tablistCooldownEnd
            }

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
                ChatUtils.debug("Added time!")
            }

            if (config.pestSpawnChatMessage) {
                ChatUtils.chat("Pests spawned in §b${spawnTime.format()}")
            }
        }

        longestCropBrokenTime = 0.seconds

        averageSpawnTime = pestSpawnTimes.average().toInt()

        lastSpawnTime = SimpleTimeMark.now()

    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (config.onlyWithVacuum && !PestApi.hasVacuumInHand()) return

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

    private fun shouldSetCooldown(tabCooldownEnd: SimpleTimeMark, minutes: Int?, seconds: Int?): Boolean {

        // tablist can have up to 6 seconds of delay, besides this, there is no scenario where tablist will overestimate cooldown
        if (tabCooldownEnd > ((pestCooldownEndTime ?: SimpleTimeMark.now()) + 6.seconds)) return true

        // tablist sometimes rounds down to nearest min
        if ((tabCooldownEnd + 1.minutes) < (pestCooldownEndTime ?: SimpleTimeMark.now()) && seconds == null) return true

        // tablist shouldn't underestimate if it is displaying seconds
        if ((tabCooldownEnd + 1.seconds) < (pestCooldownEndTime ?: SimpleTimeMark.now()) && seconds != null) return true

        return false
    }

    private fun drawDisplay() = buildList {
        val lastPestSpawned = if (lastSpawnTime.isFarPast()) {
            "§cNo pest spawned since joining."
        } else {
            val timeSinceLastPest = lastSpawnTime.passedSince().format()
            "§eLast pest spawned: §b$timeSinceLastPest ago"
        }

        add(Renderable.string(lastPestSpawned))

        val pestCooldown = if (!TabWidget.PESTS.isActive) {
            "§cPests Widget not detected! Enable via /widget!"
        } else {
            var cooldownValue = pestCooldownEndTime?.timeUntil()?.format() ?: "§cUnknown"
            if (cooldownValue == "Soon") cooldownValue = "§aReady!"

            "§ePest Cooldown: §b$cooldownValue"
        }

        if (config.pestSpawnCooldown) add(Renderable.string(pestCooldown))

        val averageSpawn = averageSpawnTime.seconds.format()

        if (config.averagePestSpawnTime && averageSpawnTime != 0) {
            add(Renderable.string("§eAverage time to spawn: §b$averageSpawn"))
        }
    }

    fun isEnabled() = GardenApi.inGarden() && config.enabled
}
