package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.pests.PestTrapConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.garden.pests.PestTrapDataUpdatedEvent
import at.hannibal2.skyhanni.features.garden.GardenPlotApi
import at.hannibal2.skyhanni.features.garden.GardenPlotApi.name
import at.hannibal2.skyhanni.features.garden.GardenPlotApi.sendTeleportTo
import at.hannibal2.skyhanni.features.garden.pests.PestTrapApi.PestTrapData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.takeIfNotEmpty
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
    private val warningConfig get() = config.warningConfig
    private val enabledTypes get() = warningConfig.warningDisplayType
    private val reminderInterval get() = warningConfig.warningIntervalSeconds.get()
    private val userEnabledWarnings get() = warningConfig.enabledWarnings.get()
    private val chatWarnEnabled get() = enabledTypes in listOf(WarningDisplayType.CHAT, WarningDisplayType.BOTH)
    private val titleWarnEnabled get() = enabledTypes in listOf(WarningDisplayType.TITLE, WarningDisplayType.BOTH)

    private var data: List<PestTrapData> = emptyList()
    private var lastDataHash: Int = 0
    private var warningSound: ISound? = refreshSound()
    private var activeWarning: Pair<String, GardenPlotApi.Plot?> = "" to null
    private var nextWarning: SimpleTimeMark = SimpleTimeMark.farPast()

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(warningConfig.warningSound) {
            warningSound = refreshSound()
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (nextWarning.isInFuture()) return
        val (finalWarning, actionPlot) = activeWarning.takeIf {
            it.first.isNotEmpty()
        } ?: return PestTrapApi.releaseCache()

        warningSound?.playSound()
        tryWarnChat(finalWarning, actionPlot)
        tryWarnTitle(finalWarning)

        nextWarning = SimpleTimeMark.now() + reminderInterval.seconds
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onPestTrapDataUpdate(event: PestTrapDataUpdatedEvent) {
        data = event.data.takeIf { it.hash() != lastDataHash } ?: return
        lastDataHash = data.hash()
        handleDataUpdate()
    }

    private fun List<PestTrapData>.hash(): Int = map { data ->
        data.number to data.count to data.isFull to data.baitCount to data.noBait
    }.hashCode()

    private fun handleDataUpdate() {
        val warnings = userEnabledWarnings.mapNotNull { generateWarning(it, data) }

        val finalWarning = warnings.joinToString(" §8| ") { it.first }
        val actionPlot = warnings.firstOrNull()?.second

        activeWarning = finalWarning to actionPlot
    }

    private fun refreshSound() = warningConfig.warningSound.get().takeIf(String::isNotEmpty)?.let { SoundUtils.createSound(it, 1f) }
    private fun List<PestTrapData>.joinPlots(): String = this.joinToString("§8, ") { "§a#${it.number}" }

    private fun generateWarning(reason: WarningReason, data: List<PestTrapData>): Pair<String, GardenPlotApi.Plot?>? {
        val dataSet = data.getTrapReport(reason).takeIfNotEmpty()?.toList() ?: return null
        return "${reason.warningString}${dataSet.joinPlots()}" to dataSet.firstOrNull()?.plot
    }

    private fun tryWarnChat(finalWarning: String, actionPlot: GardenPlotApi.Plot?) {
        if (!chatWarnEnabled) return
        when (actionPlot) {
            null -> ChatUtils.chat(finalWarning)
            else -> ChatUtils.clickToActionOrDisable(
                message = finalWarning,
                config.warningConfig::enabledWarnings,
                actionName = "warp to ${actionPlot.name}",
                action = {
                    actionPlot.sendTeleportTo()
                },
                oneTimeClick = true,
            )
        }
    }

    private fun tryWarnTitle(finalWarning: String) {
        if (!titleWarnEnabled) return
        LorenzUtils.sendTitle(finalWarning, 3.seconds, 2.8, 7f)
    }

    private fun List<PestTrapData>.getTrapReport(type: WarningReason) = this.filter {
        when (type) {
            WarningReason.TRAP_FULL -> it.isFull
            WarningReason.NO_BAIT -> it.noBait
        }
    }
}
