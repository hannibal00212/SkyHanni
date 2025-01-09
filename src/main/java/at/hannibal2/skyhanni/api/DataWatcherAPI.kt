package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.DataWatcherUpdatedEvent
import at.hannibal2.skyhanni.events.entity.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityXPOrb

//#if MC > 1.12
//$$ import net.minecraft.network.datasync.DataSerializers
//$$ import net.minecraft.network.datasync.EntityDataManager
//#endif

@SkyHanniModule
object DataWatcherAPI {

    private val ignoredEntities = setOf(
        EntityArmorStand::class.java,
        EntityXPOrb::class.java,
        EntityItem::class.java,
        EntityItemFrame::class.java,
        EntityOtherPlayerMP::class.java,
        EntityPlayerSP::class.java,
    )

    //#if MC < 1.12
    private const val DATA_VALUE_CUSTOM_NAME = 2
    //#else
    //$$ private val DATA_VALUE_CUSTOM_NAME = EntityDataManager.createKey(Entity::class.java, DataSerializers.STRING)
    //#endif

    //#if MC < 1.12
    private const val DATA_VALUE_HEALTH = 6
    //#else
    //$$ private val DATA_VALUE_HEALTH = EntityDataManager.createKey(EntityLivingBase::class.java, DataSerializers.FLOAT)
    //#endif

    @HandleEvent
    fun onDataWatcherUpdate(event: DataWatcherUpdatedEvent<Entity>) {
        for (updatedEntry in event.updatedEntries) {
            //#if MC < 1.12
            if (updatedEntry.dataValueId == DATA_VALUE_CUSTOM_NAME) {
                //#else
                //$$ if (updatedEntry.key == DATA_VALUE_CUSTOM_NAME) {
                //#endif
                EntityCustomNameUpdateEvent(event.entity, event.entity.customNameTag).post()
            }

            //#if MC < 1.12
            if (updatedEntry.dataValueId == DATA_VALUE_HEALTH) {
                val health = (updatedEntry.`object` as? Float)?.toInt() ?: continue
                //#else
                //$$ if (updatedEntry.key == DATA_VALUE_HEALTH) {
                //$$ val health = (updatedEntry.value as? Float)?.toInt() ?: continue
                //#endif

                val entity = EntityUtils.getEntityByID(event.entity.entityId) ?: continue
                if (entity.javaClass in ignoredEntities) continue

                if (event.entity is EntityWither && health == 300 && event.entity.entityId < 0) continue
                if (event.entity is EntityLivingBase) {
                    EntityHealthUpdateEvent(event.entity, health.derpy()).post()
                }
            }
        }
    }
}
