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
        val name = "${method.declaringClass.name}.${method.name}${
            method.parameterTypes.joinTo(
                StringBuilder(),
                prefix = "(",
                postfix = ")",
                separator = ", ",
                transform = Class<*>::getTypeName,
            )
        }"

        val eventConsumer: (Any) -> Unit = when (method.parameterCount) {
            0 -> {
                require(options.eventType != SkyHanniEvent::class) {
                    "Method ${method.name} has no parameters but no eventType was provided in the annotation."
                }
                val eventType = options.eventType.java
                require(SkyHanniEvent::class.java.isAssignableFrom(eventType)) {
                    "eventType in @HandleEvent must extend SkyHanniEvent. Provided: $eventType"
                }
                ;
                { _: Any -> method.invoke(instance) }
            }
            1 -> {
                require(SkyHanniEvent::class.java.isAssignableFrom(method.parameterTypes[0])) {
                    "Method ${method.name} parameter must be a subclass of SkyHanniEvent."
                }
                ;
                { event -> method.invoke(instance, event) }
            }
            else -> {
                throw IllegalArgumentException("Method ${method.name} must have either 0 or 1 parameters.")
            }
        }

        val generic: Class<*>? = if (isGeneric) {
            val genericType = method.genericParameterTypes.getOrNull(0)
                ?: error("Method ${method.name} does not have a generic parameter type.")
            ReflectionUtils.resolveUpperBoundSuperClassGenericParameter(
                genericType,
                GenericSkyHanniEvent::class.java.typeParameters[0],
            ) ?: error(
                "Generic event handler type parameter is not present in " +
                    "event class hierarchy for type $genericType",
            )
        } else null
        listeners.add(Listener(name, eventConsumer, options, generic))
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
