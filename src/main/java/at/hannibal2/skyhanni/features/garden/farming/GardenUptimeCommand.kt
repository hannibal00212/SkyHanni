package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.chat
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

object GardenUptimeCommand {
    private val config get() = GardenAPI.config.gardenUptime
    private val storage get() = GardenAPI.storage?.uptimeTracker

    fun onCommand(args: Array<String>) {
        if (!config.showDisplay) {
            ChatUtils.userError("shgardenuptime requires 'Show Garden Uptime' to be enabled")
        }

        val dayAmount = when {
            args.isEmpty() -> 7
            args[0].toIntOrNull() == null -> 7
            else -> args[0].toInt().coerceAtMost(31)
        }

        val date = LocalDate.now()
        var totalUptime = 0
        val commandString = mutableListOf(
            "§r§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§r",
            "§b${LorenzUtils.getPlayerName()}§e's garden uptime for the past §a$dayAmount §edays:",
            ""
        )

        for(num in 0..< dayAmount) {

            val day = date.minusDays(num.toLong())
            val entry = storage?.getEntry(SkyHanniTracker.DisplayMode.DAY, day)

            val cropBreakTime = entry?.cropBreakTime ?: 0
            val pestTime = if (config.includePests) entry?.pestTime ?: 0 else 0
            val visitorTime = if (config.includeVisitors) entry?.visitorTime ?: 0 else 0

            val uptime = cropBreakTime + pestTime + visitorTime

            val dayString = if (day == LocalDate.now()) "Today" else day.toString()

            val outputString = "    §e$dayString:    §b${uptime.seconds}"

            totalUptime += uptime
            commandString += outputString
        }

        commandString += ""
        commandString += "§bTotal Uptime: §e${totalUptime.seconds}"
        commandString += "§bAverage Uptime: §e${(totalUptime/dayAmount).seconds}"
        commandString += "§r§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§r"

        chat(commandString.joinToString("\n"), false)
    }
}
