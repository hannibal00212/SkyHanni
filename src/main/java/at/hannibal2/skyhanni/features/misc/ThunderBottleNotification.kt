package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ThunderBottleNotification {

    private val config get() = SkyHanniMod.feature.misc

    private val thunderBottleChargedPattern by RepoPattern.pattern(
        "thunderbottle.charged",
        "§e> Your bottle of thunder has fully charged!",
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        if (thunderBottleChargedPattern.matches(event.message)) {
            LorenzUtils.sendTitle("§eThunder Bottle Charged!", 3.seconds)
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.thunderBottleNotification
}
