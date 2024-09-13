package at.hannibal2.skyhanni.features.event.winter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object NewYearCakeReminder {

    private val config get() = SkyHanniMod.feature.event.winter
    private val sidebarDetectionPattern by RepoPattern.pattern(
        "event.winter.newyearcake.reminder.sidebar",
        "§dNew Year Event!§f (?<time>.*)",
    )
    private var lastReminderSend = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (event.message == "§aYou claimed a §r§cNew Year Cake§r§a!") {
            markCakeClaimed()
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        //  cake already claimed
        if (event.inventoryName == "Baker") {
            markCakeClaimed()
        }
    }

    private fun markCakeClaimed() {
        val playerSpecific = ProfileStorageData.playerSpecific ?: return
        playerSpecific.winter.cakeCollectedYear = SkyBlockTime.now().year
    }

    private fun isClaimed(): Boolean {
        val playerSpecific = ProfileStorageData.playerSpecific ?: return false
        return playerSpecific.winter.cakeCollectedYear == SkyBlockTime.now().year
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.newYearCakeReminder) return
        if (!isCakeTime()) return
        if (ReminderUtils.isBusy()) return
        if (isClaimed()) return

        if (lastReminderSend.passedSince() < 30.seconds) return
        lastReminderSend = SimpleTimeMark.now()
        ChatUtils.clickToActionOrDisable(
            "Reminding you to grab the free New Year Cake. Click here to open the baker menu!",
            config::newYearCakeReminder,
            actionName = "open the baker menu",
            action = { HypixelCommands.openBaker() },
        )
    }

    private fun isCakeTime() = ScoreboardData.sidebarLinesFormatted.any { sidebarDetectionPattern.matches(it) }
}
