package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.ActionBarBeforeUpdateEvent
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ActionBarData {
    var actionBar = ""
        private set

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        actionBar = ""
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onActionBarReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() != 2) return

        ActionBarBeforeUpdateEvent(event.message.formattedText.stripHypixelMessage(), event.message).postAndCatch()
    }


    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        //#if MC<1.12
        if (event.type.toInt() != 2) return
        //#else
        //$$ if (event.type.id.toInt() != 2) return
        //#endif

        val original = event.message
        val message = LorenzUtils.stripVanillaMessage(original.formattedText)
        actionBar = message
        val actionBarEvent = ActionBarUpdateEvent(actionBar, event.message)
        actionBarEvent.postAndCatch()
        if (event.message.formattedText != actionBarEvent.chatComponent.formattedText) {
            event.message = actionBarEvent.chatComponent
        }
    }
}
