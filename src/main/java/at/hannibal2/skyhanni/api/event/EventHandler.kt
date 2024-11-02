package at.hannibal2.skyhanni.api.event

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.mixins.hooks.getValue
import at.hannibal2.skyhanni.mixins.hooks.setValue
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.system.PlatformUtils

class EventHandler<T : SkyHanniEvent> private constructor(
    val name: String,
    private val listeners: List<EventListeners.Listener>,
    private val canReceiveCancelled: Boolean,
) {

    var invokeCount: Long = 0L
        private set

    constructor(event: Class<T>, listeners: List<EventListeners.Listener>) : this(
        (event.name.split(".").lastOrNull() ?: event.name).replace("$", "."),
        listeners.sortedBy { it.options.priority }.toList(),
        listeners.any { it.options.receiveCancelled },
    )

    companion object {
        private var eventHandlerDepth by object : ThreadLocal<Int>() {
            override fun initialValue(): Int {
                return 0
            }
        }

        /**
         * Returns true if the current thread is in an event handler. This is because the event handler catches exceptions which means
         * that we are free to throw exceptions in the event handler without crashing the game.
         * We also return true if we are in a dev environment to alert the developer of any errors effectively.
         */
        val isInEventHandler get() = eventHandlerDepth > 0 || PlatformUtils.isDevEnvironment
    }

    fun post(event: T, onError: ((Throwable) -> Unit)? = null): Boolean {
        invokeCount++
        if (this.listeners.isEmpty()) return false

        if (SkyHanniEvents.isDisabledHandler(name)) return false

        var errors = 0

        eventHandlerDepth++
        for (listener in listeners) {
            if (!shouldInvoke(event, listener)) continue
            try {
                listener.invoker.accept(event)
            } catch (throwable: Throwable) {
                errors++
                if (errors <= 3) {
                    val errorName = throwable::class.simpleName ?: "error"
                    val aOrAn = StringUtils.optionalAn(errorName)
                    val message = "Caught $aOrAn $errorName in ${listener.name} at $name: ${throwable.message}"
                    ErrorManager.logErrorWithData(throwable, message, ignoreErrorCache = onError != null)
                }
                onError?.invoke(throwable)
            }
            if (event.isCancelled && !canReceiveCancelled) break
        }
        eventHandlerDepth--

        if (errors > 3) {
            val hiddenErrors = errors - 3
            ChatUtils.chat(
                Text.text(
                    "§c[SkyHanni/${SkyHanniMod.version}] $hiddenErrors more errors in $name are hidden!",
                ),
            )
        }
        return event.isCancelled
    }

    private fun shouldInvoke(event: SkyHanniEvent, listener: EventListeners.Listener): Boolean {
        if (SkyHanniEvents.isDisabledInvoker(listener.name)) return false
        if (listener.options.onlyOnSkyblock && !LorenzUtils.inSkyBlock) return false
        if (IslandType.ANY !in listener.onlyOnIslandTypes && !inAnyIsland(listener.onlyOnIslandTypes)) return false
        if (event.isCancelled && !listener.options.receiveCancelled) return false
        if (
            event is GenericSkyHanniEvent<*> &&
            listener.generic != null &&
            !listener.generic.isAssignableFrom(event.type)
        ) {
            return false
        }
        return true
    }
}
