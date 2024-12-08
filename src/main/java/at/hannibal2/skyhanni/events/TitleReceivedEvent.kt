package at.hannibal2.skyhanni.events

import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class TitleReceivedEvent(val title: String) : LorenzEvent()
