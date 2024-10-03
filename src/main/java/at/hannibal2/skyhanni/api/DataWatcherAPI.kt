package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.events.DataWatcherUpdatedEvent
import at.hannibal2.skyhanni.events.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DataWatcherAPI {

    private const val DATA_VALUE_CUSTOM_NAME = 2

    @SubscribeEvent
    fun onDataWatcherUpdate(event: DataWatcherUpdatedEvent) {
        for (updatedEntry in event.updatedEntries) {
            if (updatedEntry.dataValueId == DATA_VALUE_CUSTOM_NAME) {
                EntityCustomNameUpdateEvent(event.entity.customNameTag, event.entity).postAndCatch()
            }
        }
    }

    // TODO move EntityHealthUpdateEvent logic from EntityData in here
}
