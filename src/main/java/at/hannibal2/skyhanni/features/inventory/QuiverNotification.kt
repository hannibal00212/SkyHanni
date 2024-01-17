package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object QuiverNotification {
    private val pattern by RepoPattern.pattern("inventory.quiver.chat.low", "You only have (?<arrowsLeft>.*) arrows left in your Quiver!")
    @SubscribeEvent
    fun onChatMessage(event: LorenzChatEvent) {
        if (!SkyHanniMod.configManager.features.inventory.quiverAlert) return
        val message = event.message.removeColor()
        pattern.matchMatcher(message) {
            TitleManager.sendTitle("§c${group("arrowsLeft")} arrows left!", 3.seconds, 3.6, 7.0)
            sound()
        }
    }

    fun sound() {
        SoundUtils
        SkyHanniMod.coroutineScope.launch {
            repeat(30) {
                delay(100)
                SoundUtils.playPlingSound()
            }
        }
    }
}
