package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenBurrowingSporesNotifier {

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.config.burrowingSporesNotification) return

        if (event.message.endsWith("§6§lVERY RARE CROP! §r§f§r§9Burrowing Spores")) {
            LorenzUtils.sendTitle("§9Burrowing Spores!", 5.seconds)
            // would be sent too often, nothing special then
//            ItemBlink.setBlink(NEUItems.getItemStackOrNull("BURROWING_SPORES"), 5_000)
        }
    }
}
