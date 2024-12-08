package at.hannibal2.skyhanni.api.event

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.ReflectionUtils
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Consumer

class EventListeners private constructor(val name: String, private val isGeneric: Boolean) {

    private val listeners: MutableList<Listener> = mutableListOf()

    constructor(event: Class<*>) : this(
        (event.name.split(".").lastOrNull() ?: event.name).replace("$", "."),
        GenericSkyHanniEvent::class.java.isAssignableFrom(event),
    )

    fun addListener(method: Method, instance: Any, options: HandleEvent) {
        require(method.parameterCount == 1)
        val generic: Class<*>? = if (isGeneric) {
            ReflectionUtils.resolveUpperBoundSuperClassGenericParameter(
                method.genericParameterTypes[0],
                GenericSkyHanniEvent::class.java.typeParameters[0],
            ) ?: error(
                "Generic event handler type parameter is not present in " +
                    "event class hierarchy for type ${method.genericParameterTypes[0]}",
            )
        } else {
            null
        }
        val name = "${method.declaringClass.name}.${method.name}${
            method.parameterTypes.joinTo(
                StringBuilder(),
                prefix = "(",
                postfix = ")",
                separator = ", ",
                transform = Class<*>::getTypeName,
            )
        }"
        listeners.add(Listener(name, createEventConsumer(name, instance, method), options, generic))
    }

    /**
     * Creates a consumer using LambdaMetafactory, this is the most efficient way to reflectively call
     * a method from within code.
     */
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
                MethodType.methodType(Nothing::class.javaPrimitiveType, method.parameterTypes[0]),
            ).target.bindTo(instance).invokeExact() as Consumer<Any>
        } catch (e: Throwable) {
            throw IllegalArgumentException("Method $name is not a valid consumer", e)
        }
    }

    fun getListeners(): List<Listener> = listeners

    class Listener(
        val name: String,
        val invoker: Consumer<Any>,
        val options: HandleEvent,
        val generic: Class<*>?,
    ) {
        val onlyOnIslandTypes: Set<IslandType> = getIslands(options)

        companion object {
            private fun getIslands(options: HandleEvent): Set<IslandType> =
                if (options.onlyOnIslands.isEmpty()) setOf(options.onlyOnIsland)
                else options.onlyOnIslands.toSet()
        }
    }
}
