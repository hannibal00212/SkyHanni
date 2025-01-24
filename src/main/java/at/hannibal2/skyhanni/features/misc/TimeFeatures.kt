package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSBFeature
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.WinterApi
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils.format
import java.text.SimpleDateFormat
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TimeFeatures {

    private val config get() = SkyHanniMod.feature.gui
    private val winterConfig get() = SkyHanniMod.feature.event.winter

    private val startOfNextYear by RecalculatingValue(1.seconds) {
        SkyBlockTime(year = SkyBlockTime.now().year + 1).asTimeMark()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        @Suppress("InSkyBlockEarlyReturn")
        if (!LorenzUtils.inSkyBlock && !OutsideSBFeature.REAL_TIME.isSelected()) return
        if (config.realTime) {
            val timeFormat = if (config.realTimeFormatToggle) {
                // 12 h format
                SimpleDateFormat("hh:mm${if (config.realTimeShowSeconds) ":ss" else ""} a")
            } else {
                // 24 h format
                SimpleDateFormat("HH:mm${if (config.realTimeShowSeconds) ":ss" else ""}")
            }
            val currentTime = timeFormat.format(System.currentTimeMillis())
            config.realTimePosition.renderString(currentTime, posLabel = "Real Time")
        }

        if (winterConfig.islandCloseTime && IslandType.WINTER.isInIsland()) {
            if (WinterApi.isDecember()) return
            val timeTillNextYear = startOfNextYear.timeUntil()
            val alreadyInNextYear = timeTillNextYear > 5.days
            val text = if (alreadyInNextYear) {
                "§fJerry's Workshop §cis closing!"
            } else {
                "§fJerry's Workshop §ecloses in §b${timeTillNextYear.format()}"
            }
            winterConfig.islandCloseTimePosition.renderString(text, posLabel = "Winter Time")
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.timeConfigs.winterTime", "event.winter.islandCloseTime")
        event.move(2, "misc.timeConfigs.winterTimePos", "event.winter.islandCloseTimePosition")

        event.move(2, "misc.timeConfigs.realTime", "gui.realTime")
        event.move(2, "misc.timeConfigs.realTimePos", "gui.realTimePosition")
    }
}
