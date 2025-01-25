package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.pests.PestTrapConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.garden.pests.PestTrapDataUpdatedEvent
import at.hannibal2.skyhanni.features.garden.GardenPlotApi
import at.hannibal2.skyhanni.features.garden.GardenPlotApi.sendTeleportTo
import at.hannibal2.skyhanni.features.garden.pests.PestTrapAPI.PestTrapData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import net.minecraft.client.audio.ISound
import kotlin.time.Duration.Companion.seconds

private typealias WarningReason = PestTrapConfig.WarningConfig.WarningReason
private typealias WarningDisplayType = PestTrapConfig.WarningConfig.WarningDisplayType

@SkyHanniModule
object PestTrapFeatures {

    private val config get() = SkyHanniMod.feature.garden.pests.pestTrap
    private val sound get() = config.warningConfig.warningSound
    private val enabledTypes get() = config.warningConfig.warningDisplayType

    private var fullSet: Set<Int> = emptySet()
    private var noBaitSet: Set<Int> = emptySet()
    private var warningSound: ISound? = null
    private var nextWarning: SimpleTimeMark = SimpleTimeMark.farPast()
    private var lastWarningCount: Int = 0

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(sound) {
            warningSound = refreshSound()
        }
        warningSound = refreshSound()
    }

    private fun refreshSound() = sound.get().takeIf { it.isNotEmpty() }?.let {
        SoundUtils.createSound(it, 1f)
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onPestTrapDataUpdate(event: PestTrapDataUpdatedEvent) {
        val data = event.data
        fullSet = data.checkFullWarnings()
        noBaitSet = data.checkNoBaitWarnings()

        val warningsEnabled = config.warningConfig.enabledWarnings.get()
        val fullEnabled = fullSet.any() && warningsEnabled.contains(WarningReason.TRAP_FULL)
        val noBaitEnabled = noBaitSet.any() && warningsEnabled.contains(WarningReason.NO_BAIT)

        val warningCount = when {
            fullEnabled && noBaitEnabled -> 2
            fullEnabled || noBaitEnabled -> 1
            else -> 0
        }.takeIf { it != 0 } ?: return

        val fullWarning = data.buildFullWarning(warningCount)
        val noBaitWarning = data.buildNoBaitWarning(warningCount)
        val finalWarning = listOf(fullWarning, noBaitWarning).joinToString(" §8| ")

        val chatWarnEnabled = enabledTypes in listOf(WarningDisplayType.CHAT, WarningDisplayType.BOTH)
        val titleWarnEnabled = enabledTypes in listOf(WarningDisplayType.TITLE, WarningDisplayType.BOTH)

        val firstDataItem = data.firstOrNull { it.isFull || it.noBait }
        val actionPlot = firstDataItem?.plot ?: firstDataItem?.location?.let { GardenPlotApi.closestPlot(it) }

        lastWarningCount = (fullSet.size + noBaitSet.size).takeIf {
            it != 0
        }?.takeIf { it > lastWarningCount || !nextWarning.isInFuture() } ?: return

        warningSound?.playSound()
        if (chatWarnEnabled) {
            when (actionPlot) {
                null -> ChatUtils.chat(finalWarning)
                else -> ChatUtils.clickToActionOrDisable(
                    message = finalWarning,
                    config.warningConfig::enabledWarnings,
                    actionName = "warp to $actionPlot",
                    action = {
                        actionPlot.sendTeleportTo()
                    },
                    oneTimeClick = true,
                )
            }
        }
        if (titleWarnEnabled) LorenzUtils.sendTitle(finalWarning, 3.seconds, 2.8, 7f)
        nextWarning = SimpleTimeMark.now().plus(config.warningConfig.warningIntervalSeconds.get().seconds)
    }

    private fun List<PestTrapData>.getFullWarningJoinedString() =
        this.filter { it.isFull }.toList().joinToString("§8, ") { "§a#${it.number}" }

    private fun List<PestTrapData>.buildFullWarning(warningCount: Int) = when (warningCount) {
        2 -> "§cFull: ${this.getFullWarningJoinedString()}"
        1 -> "§cPest Traps Full! ${this.getFullWarningJoinedString()}"
        else -> "§cF: ${this.getFullWarningJoinedString()}"
    }

    private fun List<PestTrapData>.getNoBaitWarningJoinedString() =
        this.filter { it.noBait }.joinToString("§8, ") { "§a#${it.number}" }

    private fun List<PestTrapData>.buildNoBaitWarning(warningCount: Int) = when (warningCount) {
        2 -> "§cNo Bait: ${this.getNoBaitWarningJoinedString()}"
        1 -> "§cNo Bait in Pest Traps! ${this.getNoBaitWarningJoinedString()}"
        else -> "§cNB: ${this.getNoBaitWarningJoinedString()}"
    }

    private fun List<PestTrapData>.checkFullWarnings() = this.filter {
        it.count >= PestTrapAPI.MAX_PEST_COUNT_PER_TRAP
    }.map { it.number }.toSet()

    private fun List<PestTrapData>.checkNoBaitWarnings() = this.filter {
        it.baitCount == 0
    }.map { it.number }.toSet()
}
