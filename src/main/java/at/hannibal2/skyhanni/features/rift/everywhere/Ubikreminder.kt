package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.hours

@SkyHanniModule
object Ubikreminder {

    private val config get() = RiftAPI.config.ubikReminder

    private var isTimerRunning = false
    private val messageRegex = Regex("ROUND [1-9] \\(FINAL\\):")


    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!RiftAPI.config.ubikReminder) return
        val message = event.message
        if (messageRegex.matches(message) && !isTimerRunning) {
            startTimer()
        }
    }
    private fun startTimer() {
        isTimerRunning = true

        DelayedRun.runDelayed(2.hours) { // 2 hours as a Duration
            ChatUtils.chat("§aUbik's cube is ready in the rift!")
            isTimerRunning = false

        }
    }
}
