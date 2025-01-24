package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class DrawScreenAfterEvent(val mouseX: Int, val mouseY: Int, val ci: CallbackInfo) : SkyHanniEvent()
