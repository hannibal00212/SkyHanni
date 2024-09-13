package at.hannibal2.skyhanni.api.event

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.chat.Text
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.function.Consumer

class EventHandler<T : SkyHanniEvent> private constructor(val name: String, private val isGeneric: Boolean) {

    private val listeners: MutableList<Listener> = mutableListOf()

    private var isFrozen = false
    private var canReceiveCancelled = false

    var invokeCount: Long = 0L
        private set

    constructor(event: Class<T>) : this(
        (event.name.split(".").lastOrNull() ?: event.name).replace("$", "."),
        GenericSkyHanniEvent::class.java.isAssignableFrom(event)
    )

    fun addListener(method: Method, instance: Any, options: HandleEvent) {
        if (isFrozen) throw IllegalStateException("Cannot add listener to frozen event handler")
        val generic: Class<*>? = if (isGeneric) {
            method.genericParameterTypes
                .firstNotNullOfOrNull { it as? ParameterizedType }
                ?.let { it.actualTypeArguments.firstOrNull() as? Class<*> }
                ?: throw IllegalArgumentException("Generic event handler must have a generic type")
        } else {
            null
        }
        val name = "${method.declaringClass.name}.${method.name}${
            method.parameterTypes.joinTo(
                StringBuilder(),
                prefix = "(",
                postfix = ")",
                separator = ", ",
                transform = Class<*>::getTypeName
            )
        }"
        listeners.add(Listener(name, createEventConsumer(name, instance, method), options, generic))
    }

    @Suppress("UNCHECKED_CAST")
    private fun createEventConsumer(name: String, instance: Any, method: Method): Consumer<Any> {
        try {
            val handle = MethodHandles.lookup().unreflect(method)
            return LambdaMetafactory.metafactory(
                MethodHandles.lookup(),
                "accept",
                MethodType.methodType(Consumer::class.java, instance::class.java),
                MethodType.methodType(Nothing::class.javaPrimitiveType, Object::class.java),
                handle,
                MethodType.methodType(Nothing::class.javaPrimitiveType, method.parameterTypes[0])
            ).target.bindTo(instance).invokeExact() as Consumer<Any>
        } catch (e: Throwable) {
            throw IllegalArgumentException("Method $name is not a valid consumer", e)
        }
    }

    fun freeze() {
        isFrozen = true
        listeners.sortBy { it.options.priority }
        canReceiveCancelled = listeners.any { it.options.receiveCancelled }
    }

    fun post(event: T, onError: ((Throwable) -> Unit)? = null): Boolean {
        invokeCount++
        if (this.listeners.isEmpty()) return false
        if (!isFrozen) error("Cannot invoke event on unfrozen event handler")

        if (SkyHanniEvents.isDisabledHandler(name)) return false

        var errors = 0

        for (listener in listeners) {
            if (!shouldInvoke(event, listener)) continue
            try {
                listener.invoker.accept(event)
            } catch (throwable: Throwable) {
                errors++
                if (errors <= 3) {
                    val errorName = throwable::class.simpleName ?: "error"
                    val message = "Caught an $errorName in ${listener.name} at $name: ${throwable.message}"
                    ErrorManager.logErrorWithData(throwable, message, ignoreErrorCache = onError != null)
                }
                onError?.invoke(throwable)
            }
            if (event.isCancelled && !canReceiveCancelled) break
        }

        if (errors > 3) {
            val hiddenErrors = errors - 3
            ChatUtils.chat(
                Text.text(
                    "§c[SkyHanni/${SkyHanniMod.version}] $hiddenErrors more errors in $name are hidden!"
                )
            )
        }
        return event.isCancelled
    }

    private fun shouldInvoke(event: SkyHanniEvent, listener: Listener): Boolean {
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

    private class Listener(
        val name: String,
        val invoker: Consumer<Any>,
        val options: HandleEvent,
        val generic: Class<*>?,
        val onlyOnIslandTypes: Set<IslandType> = options.onlyOnIslands.toSet(),
    )
}
