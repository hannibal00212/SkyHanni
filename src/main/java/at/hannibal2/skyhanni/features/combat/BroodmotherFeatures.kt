package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.reflect.KMutableProperty0
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@SkyHanniModule
object BroodmotherFeatures {

    enum class StageEntry(private val str: String, val minutes: Int) {
        SLAIN("§eSlain", 10),
        DORMANT("§eDormant", 9),
        SOON("§6Soon", 6),
        AWAKENING("§6Awakening", 3),
        IMMINENT("§4Imminent", 1),
        ALIVE("§4Alive!", 0);

        override fun toString() = str
    }

    private val config get() = SkyHanniMod.feature.combat.broodmother
    private val spawnAlertConfig get() = config.spawnAlert

    private var lastStage: StageEntry? = null
    private var currentStage: StageEntry? = null
    private var broodmotherSpawnTime = SimpleTimeMark.farPast()
    private var display = ""

    @SubscribeEvent
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.BROODMOTHER)) return
        val newStage = event.widget.matchMatcherFirstLine { group("stage") } ?: ""
        if (newStage.isNotEmpty()) {
            lastStage = currentStage
            currentStage = StageEntry.valueOf(newStage.replace("!", "").uppercase())
            onStageUpdate()
        }
    }

    private fun onStageUpdate() {
        ChatUtils.debug("New Broodmother stage: $currentStage")

        if (lastStage == null) {
            if (onServerJoin()) return
        }

        // ignore Hypixel bug where the stage may temporarily revert to Imminent after the Broodmother's death
        if (currentStage == StageEntry.IMMINENT && lastStage == StageEntry.ALIVE) return

        if (currentStage == StageEntry.ALIVE) {
            onBroodmotherSpawn()
            return
        }

        val timeUntilSpawn = currentStage?.minutes?.minutes
        broodmotherSpawnTime = SimpleTimeMark.now() + timeUntilSpawn!!

        if (currentStage == StageEntry.IMMINENT && config.imminentWarning) {
            playImminentWarning()
            return
        }

        if (config.stages.contains(currentStage) && lastStage != null) {
            if (currentStage == StageEntry.SLAIN) {
                onBroodmotherSlain()
            } else {
                val pluralize = StringUtils.pluralize(timeUntilSpawn.toInt(DurationUnit.MINUTES), "minute")
                ChatUtils.chat(
                    "Broodmother: $lastStage §e-> $currentStage§e. §b${timeUntilSpawn.inWholeMinutes} $pluralize §euntil it spawns!"
                )
            }
        }
    }

    private fun onServerJoin(): Boolean {
        // don't send if user has config enabled for either of the alive messages
        // this is so that two messages aren't immediately sent upon joining a server
        if (config.stageOnJoin && !(currentStage == StageEntry.ALIVE && isAliveMessageEnabled())) {
            val pluralize = StringUtils.pluralize(currentStage?.minutes ?: 0, "minute")
            var message = "The Broodmother's current stage in this server is ${currentStage.toString().replace("!", "")}§e."
            if (currentStage?.minutes != 0) {
                message += " It will spawn within §b${currentStage?.minutes} $pluralize§e."
            }
            ChatUtils.chat(message)
            return true
        } else {
            return false
        }
    }

    private fun onBroodmotherSpawn() {
        broodmotherSpawnTime = SimpleTimeMark.farPast()
        if (!isAliveMessageEnabled()) return
        val feature: KMutableProperty0<*>
        if (config.alertOnSpawn) {
            feature = config::alertOnSpawn
            val alertSound = SoundUtils.createSound(spawnAlertConfig.alertSound, spawnAlertConfig.pitch)
            SoundUtils.repeatSound(100, spawnAlertConfig.repeatSound, alertSound)
            LorenzUtils.sendTitle(spawnAlertConfig.text.replace("&", "§"), 3.seconds)
        } else {
            feature = config::stages
        }
        ChatUtils.clickToActionOrDisable(
            "The Broodmother has spawned!",
            feature,
            actionName = "warp to the Top of the Nest",
            action = { HypixelCommands.warp("nest") },
        )
    }

    private fun playImminentWarning() {
        SoundUtils.repeatSound(100, 2, SoundUtils.createSound("note.pling", 0.5f))
        ChatUtils.chat("The Broodmother is §4Imminent§e! It will spawn in §b60 seconds§e!")
    }

    private fun onBroodmotherSlain() {
        broodmotherSpawnTime = SimpleTimeMark.now() + 10.minutes
        if (!(config.hideSlainWhenNearby && SpidersDenAPI.isAtTopOfNest())) {
            ChatUtils.chat("The Broodmother was killed!")
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        broodmotherSpawnTime = SimpleTimeMark.farPast()
        lastStage = null
        currentStage = null
        display = ""
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isCountdownEnabled()) return
        if (display.isEmpty()) return

        config.countdownPosition.renderString(display, posLabel = "Broodmother Countdown")
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isCountdownEnabled()) return

        if (broodmotherSpawnTime.isFarPast()) {
            if (lastStage != null) {
                display = "§4Broodmother spawned!"
            }
        } else {
            val countdown = broodmotherSpawnTime.timeUntil().format()
            display = "§4Broodmother spawning in §b$countdown"
        }
    }

    @JvmStatic
    fun playTestSound() {
        with(spawnAlertConfig) {
            SoundUtils.createSound(alertSound, pitch).playSound()
        }
    }

    private fun inSpidersDen() = IslandType.SPIDER_DEN.isInIsland()
    private fun isCountdownEnabled() = inSpidersDen() && config.countdown
    private fun isAliveMessageEnabled() = config.alertOnSpawn || config.stages.contains(StageEntry.ALIVE)
}
