package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.TitleReceivedEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.network.play.server.S45PacketTitle

@SkyHanniModule
object TitleData {

    @HandleEvent
    fun onReceiveCurrentShield(event: PacketReceivedEvent) {
        val packet = event.packet

        if (packet !is S45PacketTitle) return
        val message = packet.message ?: return
        val formattedText = message.formattedText
        if (TitleReceivedEvent(formattedText).post()) {
            event.cancel()
        }
    }
}
