package at.hannibal2.skyhanni.events

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class DrawScreenAfterEvent(val mouseX: Int, val mouseY: Int, val ci: CallbackInfo) : LorenzEvent()
