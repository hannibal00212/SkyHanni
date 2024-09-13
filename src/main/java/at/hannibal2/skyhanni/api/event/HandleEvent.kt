package at.hannibal2.skyhanni.api.event

import at.hannibal2.skyhanni.data.IslandType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class HandleEvent(
    /**
     * If the event should only be received while on SkyBlock.
     */
    val onlyOnSkyblock: Boolean = false,

    /**
     * If the event should only be received while on a specific skyblock island.
     */
    vararg val onlyOnIslands: IslandType = [IslandType.ANY],

    /**
     * The priority of when the event will be called, lower priority will be called first, see the companion object.
     */
    val priority: Int = 0,

    /**
     * If the event is cancelled & receiveCancelled is true, then the method will still invoke.
     */
    val receiveCancelled: Boolean = false,
) {

    companion object {
        const val HIGHEST = -2
        const val HIGH = -1
        const val LOW = 1
        const val LOWEST = 2
    }
}
