package at.hannibal2.skyhanni.events

import net.minecraft.entity.Entity
//#if MC < 1.12
import net.minecraft.entity.DataWatcher
//#else
//$$ import net.minecraft.network.datasync.EntityDataManager
//#endif

data class DataWatcherUpdatedEvent(
    val entity: Entity,
    //#if MC < 1.12
    val updatedEntries: List<DataWatcher.WatchableObject>,
    //#else
    //$$ val updatedEntries: List<EntityDataManager.DataEntry<*>>
    //#endif
) : LorenzEvent()
