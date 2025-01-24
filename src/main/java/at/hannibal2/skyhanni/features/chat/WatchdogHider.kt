package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.message
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.util.IChatComponent

@SkyHanniModule
object WatchdogHider {

    private var inWatchdog = false
    private var blockedLines = 0
    private var startLineComponent: IChatComponent? = null

    private const val START_LINE = "§f"
    private const val ANNOUNCEMENT_LINE = "§4[WATCHDOG ANNOUNCEMENT]"
    private const val END_LINE = "§c"

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!LorenzUtils.onHypixel || !SkyHanniMod.feature.chat.filterType.watchDog) return

        when (event.message) {
            START_LINE -> {
                startLineComponent = event.chatComponent
                blockedLines = 0
            }

            ANNOUNCEMENT_LINE -> {
                ChatUtils.deleteMessage("watchdog") { it.message == START_LINE }
                startLineComponent = null
                inWatchdog = true
            }

            END_LINE -> {
                event.blockedReason = "watchdog"
                inWatchdog = false
            }
        }

        if (inWatchdog) {
            event.blockedReason = "watchdog"
            blockedLines++
            if (blockedLines > 10) {
                blockedLines = 0
                inWatchdog = false
            }
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "chat.watchDog", "chat.filterType.watchDog")
    }
}
