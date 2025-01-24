package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.events.TabListLineRenderEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.reflect.KProperty

var tabListGuard by object : ThreadLocal<Boolean>() {
    override fun initialValue(): Boolean {
        return false
    }
}

operator fun <T> ThreadLocal<T>.setValue(t: Any?, property: KProperty<*>, any: T) {
    this.set(any)
}

operator fun <T> ThreadLocal<T>.getValue(t: Any?, property: KProperty<*>): T {
    return get()
}

fun getPlayerName(original: String, cir: CallbackInfoReturnable<String>) {
    if (tabListGuard) return

    val event = TabListLineRenderEvent(original)
    event.post()
    val newText = event.text
    if (original != newText) {
        cir.returnValue = newText
    }
}
