package at.hannibal2.skyhanni.api.event

import at.hannibal2.skyhanni.data.MinecraftData
import at.hannibal2.skyhanni.data.jsonobjects.repo.DisabledEventsJson
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.lang.reflect.Method

@SkyHanniModule
object SkyHanniEvents {

    private val listeners: MutableMap<Class<*>, EventListeners> = mutableMapOf()
    private val handlers: MutableMap<Class<*>, EventHandler<*>> = mutableMapOf()
    private var disabledHandlers = emptySet<String>()
    private var disabledHandlerInvokers = emptySet<String>()

    fun init(instances: List<Any>) {
        instances.forEach { instance ->
            instance.javaClass.declaredMethods.forEach {
                registerMethod(it, instance)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : SkyHanniEvent> getEventHandler(event: Class<T>): EventHandler<T> = handlers.getOrPut(event) {
        EventHandler(
            event,
            getEventClasses(event).mapNotNull { listeners[it] }.flatMap(EventListeners::getListeners)
        )
    } as EventHandler<T>

    fun isDisabledHandler(handler: String): Boolean = handler in disabledHandlers
    fun isDisabledInvoker(invoker: String): Boolean = invoker in disabledHandlerInvokers

    @Suppress("UNCHECKED_CAST")
    private fun registerMethod(method: Method, instance: Any) {
        if (method.parameterCount != 1) return
        val options = method.getAnnotation(HandleEvent::class.java) ?: return
        val event = method.parameterTypes[0]
        if (!SkyHanniEvent::class.java.isAssignableFrom(event)) return
        listeners.getOrPut(event as Class<SkyHanniEvent>) { EventListeners(event) }
            .addListener(method, instance, options)
    }

    @SubscribeEvent
    fun onRepoLoad(event: RepositoryReloadEvent) {
        val data = event.getConstant<DisabledEventsJson>("DisabledEvents")
        disabledHandlers = data.disabledHandlers
        disabledHandlerInvokers = data.disabledInvokers
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Events")
        event.addIrrelevant {
            handlers.values.toMutableList()
                .filter { it.invokeCount > 0 }
                .sortedWith(compareBy({ -it.invokeCount }, { it.name }))
                .forEach {
                    add("- ${it.name} (${it.invokeCount.addSeparators()} ${it.invokeCount / (MinecraftData.totalTicks / 20)}/s)")
                }
        }
    }

    /**
     * Returns a list of all super classes and the class itself up to [SkyHanniEvent].
     */
    private fun getEventClasses(clazz: Class<*>): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        classes.add(clazz)

        var current = clazz
        while (current.superclass != null) {
            val superClass = current.superclass
            if (superClass == SkyHanniEvent::class.java) break
            if (superClass == GenericSkyHanniEvent::class.java) break
            if (superClass == CancellableSkyHanniEvent::class.java) break
            classes.add(superClass)
            current = superClass
        }
        return classes
    }
}
