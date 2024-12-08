package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object VenomShotHider {

    private val config get() = SkyHanniMod.feature.chat

    private val VenomShotPattern = Regex(".*Venom Shot.*", RegexOption.IGNORE_CASE)

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (LorenzUtils.skyBlockArea == "Spider's Den") return

        if (shouldHide(event.message)) {
            event.blockedReason = "venom shot"
        }
    }

    private fun shouldHide(message: String): Boolean {
        return VenomShotPattern.containsMatchIn(message) || message.contains("Venom Shot", ignoreCase = true)
    }

    fun isEnabled() = IslandType.SPIDER_DEN.isInIsland() && config.VenomShotHider
}
