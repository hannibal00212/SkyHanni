package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.pests.PestTrapConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.garden.pests.PestTrapDataUpdatedEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
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
import io.github.notenoughupdates.moulconfig.observer.Property
import net.minecraft.client.audio.ISound
import kotlin.time.Duration.Companion.seconds

private typealias WarningReason = PestTrapConfig.WarningConfig.WarningReason
private typealias WarningDisplayType = PestTrapConfig.WarningConfig.WarningDisplayType

@SkyHanniModule
object PestTrapFeatures {

    private val config get() = SkyHanniMod.feature.garden.pests.pestTrap
    private val enabledTypes: WarningDisplayType get() = config.warningConfig.warningDisplayType.get()
    private val userEnabledWarnings: List<WarningReason> get() = config.warningConfig.enabledWarnings.get()
    private val chatWarnEnabled: Boolean get() = enabledTypes in listOf(WarningDisplayType.CHAT, WarningDisplayType.BOTH)
    private val titleWarnEnabled: Boolean get() = enabledTypes in listOf(WarningDisplayType.TITLE, WarningDisplayType.BOTH)
    private val reminderInterval: Property<Int> get() = config.warningConfig.warningIntervalSeconds
    private val soundString get(): String = config.warningConfig.warningSound.get()

    private var data: List<PestTrapData> = emptyList()
    private var lastDataHash: Int = 0
    private var warningSound: ISound? = refreshSound()
    private var activeWarning: Pair<String, GardenPlotApi.Plot?> = "" to null
    private var nextWarningMark: SimpleTimeMark = SimpleTimeMark.farPast()

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.warningConfig.warningSound) {
            warningSound = refreshSound()
        }
        ConditionalUtils.onToggle(reminderInterval) {
            nextWarningMark = getNextWarningMark()
        }
        ConditionalUtils.onToggle(config.warningConfig.enabledWarnings) {
            updateData()
        }
        ConditionalUtils.onToggle(config.warningConfig.warningDisplayType) {
            nextWarningMark = SimpleTimeMark.now() + 5.seconds
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (nextWarningMark.isInFuture()) return
        val (finalWarning, actionPlot) = activeWarning.takeIf {
            it.first.isNotEmpty()
        } ?: return PestTrapApi.releaseCache()

        warningSound?.playSound()
        tryWarnChat(finalWarning, actionPlot)
        tryWarnTitle(finalWarning)

        nextWarningMark = getNextWarningMark()
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onPestTrapDataUpdate(event: PestTrapDataUpdatedEvent) {
        updateData(event.data)
    }

    private fun updateData(passedData: List<PestTrapData>? = GardenApi.storage?.pestTrapStatus) {
        // Check and update data if hashes are different
        if (passedData == null || passedData.hashCode() == lastDataHash) return
        this.data = passedData
        lastDataHash = passedData.hashCode()

        val warnings = userEnabledWarnings.mapNotNull {
            val (warning, plot) = generateWarning(it, this.data) ?: return@mapNotNull null
            warning to plot
        }

        val finalWarning = warnings.joinToString(" §8| ") { it.first }
        val actionPlot = warnings.firstOrNull()?.second

        activeWarning = finalWarning to actionPlot
    }

    private fun getNextWarningMark() = SimpleTimeMark.now() + reminderInterval.get().toInt().seconds
    private fun refreshSound() = soundString.takeIf(String::isNotEmpty)?.let { SoundUtils.createSound(it, 1f) }

    private fun generateWarning(reason: WarningReason, data: List<PestTrapData>): Pair<String, GardenPlotApi.Plot?>? {
        val dataSet = data.getTrapReport(reason).takeIfNotEmpty()?.toList() ?: return null
        val plot = GardenPlotApi.getPlotByName(dataSet.firstOrNull()?.plotName.orEmpty())
        val affectedTrapsFormat = dataSet.joinToString("§8, ") { "§a#${it.number}" }
        val warningFormat = "${reason.warningString} $affectedTrapsFormat"

        return warningFormat to plot
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
