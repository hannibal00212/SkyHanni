package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import net.minecraft.entity.DataWatcher
import net.minecraft.entity.Entity

data class DataWatcherUpdatedEvent<T : Entity>(
    val entity: T,
    val updatedEntries: List<DataWatcher.WatchableObject>,
) : GenericSkyHanniEvent<T>(entity.javaClass)
