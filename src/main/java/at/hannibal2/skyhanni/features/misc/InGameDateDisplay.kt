package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.formatted
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object InGameDateDisplay {

    private val config get() = SkyHanniMod.feature.gui.inGameDate

    private val patternGroup = RepoPattern.group("misc.ingametime")

    /**
     * REGEX-TEST: Spring 26th
     * REGEX-TEST: Early Winter 1st
     */
    private val monthAndDatePattern by patternGroup.pattern(
        "date",
        ".*((Early|Late) )?(Winter|Spring|Summer|Autumn) [0-9]{1,2}(nd|rd|th|st)?.*",
    )

    /**
     * REGEX-TEST: 8:30am ☀
     * REGEX-TEST: 11:40pm ☽
     */
    private val timeSymbolsPattern by patternGroup.pattern(
        "symbols",
        "[☀☽࿇]",
    )

    private var display = ""

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (!config.useScoreboard && !event.repeatSeconds(config.refreshSeconds)) return

        checkDate()
    }

    private fun checkDate() {
        val date = SkyBlockTime.now()
        var theBaseString: String
        if (config.useScoreboard) {
            val list = ScoreboardData.sidebarLinesFormatted // we need this to grab the moon/sun symbol
            val year = "Year ${date.year}"
            var monthAndDate = (list.find { monthAndDatePattern.matches(it) } ?: "??").trim()
            if (monthAndDate.last().isDigit()) {
                monthAndDate = "${monthAndDate}${SkyBlockTime.daySuffix(monthAndDate.takeLast(2).trim().toInt())}"
            }
            val time = list.find { it.lowercase().contains("am ") || it.lowercase().contains("pm ") } ?: "??"
            theBaseString = "$monthAndDate, $year ${time.trim()}".removeColor()
            if (!config.includeSunMoon) {
                theBaseString = timeSymbolsPattern.matcher(theBaseString).replaceAll("")
            }
        } else {
            theBaseString = date.formatted()
            if (config.includeSunMoon) {
                theBaseString = if ((date.hour >= 6) && (date.hour < 17)) "$theBaseString ☀"
                else "$theBaseString ☽"
            }
        }
        if (!config.includeOrdinal) theBaseString = theBaseString.removeOrdinal()
        display = theBaseString
    }

    private fun String.removeOrdinal() = replace("nd", "").replace("rd", "").replace("st", "").replace("th", "")

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.position.renderString(display, posLabel = "In-game Date Display")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
