package at.hannibal2.skyhanni.features.event.spook

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.jsonobjects.repo.EventsJson
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUCalculator
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TheGreatSpook {
    private val config get() = SkyHanniMod.feature.event.spook

    private var isGreatSpookActive = false
    private var greatSpookTimeRange: ClosedRange<SimpleTimeMark>? = null
    private var greatSpookEndTime = SimpleTimeMark.farPast()

    private var displayMobCooldown: Renderable? = null
    private var displayGreatSpookEnd: Renderable? = null

    private var timeUntilNextMob = SimpleTimeMark.farPast()

    private val patternGroup = RepoPattern.group("event.greatspook")

    /**
     * REGEX-TEST: §d§lQUICK MATHS! §r§7Solve: §r§e(10*2)+12*5
     */
    private val mathFearMessagePattern by patternGroup.pattern(
        "chat.math",
        "§d§lQUICK MATHS! §r§7Solve: §r§e(?<math>.*)",
    )

    /**
     * REGEX-TEST: §4[FEAR] Public Speaking Demon§r§f: Speak PlasticEating!
     */
    private val speakingFearMessagePattern by patternGroup.pattern(
        "chat.speaking",
        "§4\\[FEAR] Public Speaking Demon§r§f: (Speak|Say something interesting) (?<name>.*)!",
    )

    /**
     * REGEX-TEST: §5§lFEAR. §r§eA §r§dPrimal Fear §r§ehas been summoned!
     */
    private val primalFearSpawnPattern by patternGroup.pattern(
        "mob.spawn",
        "§5§lFEAR\\. §r§eA §r§dPrimal Fear §r§ehas been summoned!",
    )

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isGreatSpookActive) return

        val fear = SkyblockStat.FEAR.lastKnownValue ?: 0.0
        val mobCooldown = timeUntilNextMob.minus((3 * fear).seconds)
        val mobCooldownString = if (mobCooldown.isInFuture()) {
            "§5Next fear in: §b${
                mobCooldown.timeUntil().format(
                    biggestUnit = TimeUnit.MINUTE,
                    showMilliSeconds = false,
                    showSmallerUnits = false,
                )
            }"
        } else {
            "§5§lPrimal Fear Ready!"
        }
        displayMobCooldown = Renderable.string(mobCooldownString)

        if (config.primalFearNotification && mobCooldown.isInPast()) {
            SoundUtils.playPlingSound()
        }

        val greatSpookEnd = greatSpookTimeRange?.endInclusive ?: return
        val timeLeftString = if (greatSpookEnd.isInFuture()) {
            "§5Great Spook time left: §b${
                greatSpookEnd.timeUntil().format(
                    biggestUnit = TimeUnit.DAY,
                    maxUnits = 2,
                )
            }"
        } else {
            "§5§lThe Great Spook has ended!"
        }
        displayGreatSpookEnd = Renderable.string(timeLeftString)
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val config = SkyHanniMod.feature.dev.debug.forceGreatSpook
        config.afterChange {
            if (config.get()) {
                isGreatSpookActive = true
                greatSpookEndTime = SimpleTimeMark.farFuture()
            } else {
                val timeRange = greatSpookTimeRange
                if (timeRange == null) {
                    isGreatSpookActive = false
                    greatSpookEndTime = SimpleTimeMark.farPast()
                    return@afterChange
                }
                isGreatSpookActive = SimpleTimeMark.now() in timeRange
                greatSpookEndTime = timeRange.endInclusive
            }
        }
    }

    @SubscribeEvent
    fun onWorldSwitch(event: IslandChangeEvent) {
        val currentTime = SimpleTimeMark.now()
        val timeRange = greatSpookTimeRange ?: run {
            isGreatSpookActive = false
            return
        }

        isGreatSpookActive = currentTime in timeRange
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isGreatSpookActive) return

        if (config.primalFearTimer) {
            displayMobCooldown.let {
                config.positionTimer.renderRenderable(it, posLabel = "Primal Fear Timer")
            }
        }
        if (config.fearStatDisplay) {
            SkyblockStat.FEAR.displayValue?.let {
                config.positionFear.renderString(it, posLabel = "Fear Stat Display")
            }
        }
        if (config.greatSpookTimeLeft) {
            displayGreatSpookEnd.let {
                config.positionTimeLeft.renderRenderable(it, posLabel = "Great Spook Time Left")
            }
        }
    }

    private fun mathSolver(query: String?) {
        val answer = query?.let { NEUCalculator.calculateOrNull(it)?.toInt() } ?: run {
            ChatUtils.userError("Failed to solve $query!")
            return
        }
        ChatUtils.clickToActionOrDisable(
            "The answer is §b$answer§e.",
            config.primalFearSolver::math,
            actionName = "send the answer",
            action = {
                HypixelCommands.allChat(answer.toString())
            },
        )
    }

    private fun publicSpeakingSolver() {
        ChatUtils.clickToActionOrDisable(
            "Solving Public Speak puzzle for you.",
            config.primalFearSolver::publicSpeaking,
            actionName = "send a random string",
            action = {
                HypixelCommands.allChat("I looove SkyHanni! ${StringUtils.generateRandomString(4)}")
            },
        )
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isGreatSpookActive) return

        if (primalFearSpawnPattern.matches(event.message)) {
            timeUntilNextMob = SimpleTimeMark.now().plus(6.minutes)
            if (SkyblockStat.FEAR.lastKnownValue == null && (config.primalFearNotification || config.primalFearTimer)) {
                ChatUtils.userError(
                    "Fear stat not found! Please enable the Stats widget and enable the Fear stat for the best results.",
                    replaceSameMessage = true,
                )
            }
            return
        }

        if (config.primalFearSolver.math) {
            mathFearMessagePattern.matchMatcher(event.message) {
                DelayedRun.runNextTick {
                    mathSolver(group("math"))
                }
            }
        }

        if (config.primalFearSolver.publicSpeaking) {
            speakingFearMessagePattern.matchMatcher(event.message) {
                DelayedRun.runNextTick {
                    publicSpeakingSolver()
                }
            }
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<EventsJson>("Events").greatSpook

        val startTime = data["start_time"] ?: SimpleTimeMark.farPast()
        val endTime = data["end_time"] ?: SimpleTimeMark.farPast()

        greatSpookTimeRange = startTime..endTime
        greatSpookEndTime = if (SkyHanniMod.feature.dev.debug.forceGreatSpook.get()) SimpleTimeMark.farFuture() else endTime
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Great Spook")

        event.addIrrelevant {
            add("isActive: $isGreatSpookActive")
            add("activeTimeRange: $greatSpookTimeRange")
            add("eventEndTime: $greatSpookEndTime")
            add("timeUntilNextMob: $timeUntilNextMob")
        }
    }
}
