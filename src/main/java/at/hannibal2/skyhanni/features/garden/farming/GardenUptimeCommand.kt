package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.chat
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenUptimeCommand {
    private val config get() = GardenAPI.config.gardenUptime
    private val storage get() = GardenAPI.storage?.uptimeTracker

    fun onCommand(args: Array<String>) {
        if (!config.showDisplay) {
            ChatUtils.userError("shgardenuptime requires 'Show Garden Uptime' to be enabled")
        }

        val dayAmount = args.getOrNull(0)?.toIntOrNull()?.coerceAtMost(31) ?: 7

        val date = LocalDate.now()
        var totalUptime = 0

        val commandString = mutableListOf(
            "§r§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§r",
            "§b${LorenzUtils.getPlayerName()}'s§e garden uptime for the past §a$dayAmount §edays:",
            ""
        )

        for (num in 0..<dayAmount) {

            val day = date.minusDays(num.toLong())
            val entry = storage?.getEntry(SkyHanniTracker.DisplayMode.DAY, day)

            val cropBreakTime = entry?.cropBreakTime ?: 0
            val pestTime = if (config.includePests.get()) entry?.pestTime ?: 0 else 0
            val visitorTime = if (config.includeVisitors.get()) entry?.visitorTime ?: 0 else 0

            val uptime = cropBreakTime + pestTime + visitorTime

            val dayString = if (day == LocalDate.now()) "Today" else day.toString()

            val outputString = "    §e$dayString:    §b${uptime.seconds}"

            totalUptime += uptime
            commandString += outputString
        }

        commandString += ""
        commandString += "§bTotal Uptime: §e${totalUptime.seconds}"
        commandString += "§bAverage Uptime: §e${(totalUptime / dayAmount).seconds}"
        commandString += "§r§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§r"

        chat(commandString.joinToString("\n"), false)
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shgardenuptime") {
            description = "Shows garden uptime history for past x days, defaults to 7"
            category = CommandCategory.USERS_ACTIVE
            callback { onCommand(it) }
        }
    }
}
