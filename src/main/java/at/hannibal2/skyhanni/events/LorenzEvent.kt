package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.EventCounter
import at.hannibal2.skyhanni.mixins.hooks.getValue
import at.hannibal2.skyhanni.mixins.hooks.setValue
import at.hannibal2.skyhanni.mixins.transformers.AccessorEventBus
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.IEventListener

@Deprecated("Use SkyHanniEvent instead")
abstract class LorenzEvent : Event() {

    private val eventName by lazy {
        this::class.simpleName!!
    }

    @Deprecated("Use SkyHanniEvent instead", ReplaceWith(""))
    fun postAndCatch() = postAndCatchAndBlock {}

    companion object {

        var eventHandlerDepth by object : ThreadLocal<Int>() {
            override fun initialValue(): Int {
                return 0
            }
        }
        val isInGuardedEventHandler get() = eventHandlerDepth > 0 || PlatformUtils.isDevEnvironment
    }

    @Deprecated("Use SkyHanniEvent instead", ReplaceWith(""))
    fun postAndCatchAndBlock(
        printError: Boolean = true,
        stopOnFirstError: Boolean = false,
        ignoreErrorCache: Boolean = false,
        onError: (Throwable) -> Unit,
    ): Boolean {
        EventCounter.count(eventName)
        val visibleErrors = 3
        var errors = 0
        eventHandlerDepth++
        for (listener in getListeners()) {
            try {
                listener.invoke(this)
            } catch (throwable: Throwable) {
                errors++
                if (printError && errors <= visibleErrors) {
                    val callerName = listener.toString().split(" ")[1].split("@")[0].split(".").last()
                    val errorName = throwable::class.simpleName ?: "error"
                    val message = "Caught an $errorName in $callerName at $eventName: ${throwable.message}"
                    ErrorManager.logErrorWithData(throwable, message, ignoreErrorCache = ignoreErrorCache)
                }
                onError(throwable)
                if (stopOnFirstError) break
            }
        }
        eventHandlerDepth--
        if (errors > visibleErrors) {
            val hiddenErrors = errors - visibleErrors
            ChatUtils.chat(
                Text.text(
                    "§c[SkyHanni-${SkyHanniMod.version}] $hiddenErrors more errors in $eventName are hidden!",
                ),
            )
        }
        return if (isCancelable) isCanceled else false
    }

    private fun getListeners(): Array<out IEventListener> {
        val accessorEventBus = MinecraftForge.EVENT_BUS as AccessorEventBus
        return listenerList.getListeners(accessorEventBus.busId)
    }

    @Deprecated("Use SkyHanniEvent instead", ReplaceWith(""))
    fun postWithoutCatch() = MinecraftForge.EVENT_BUS.post(this)

    @Deprecated("Use SkyHanniEvent instead", ReplaceWith(""))
    fun cancel() {
        isCanceled = true
    }
}
