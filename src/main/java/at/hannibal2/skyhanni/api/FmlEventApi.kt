package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.data.mob.MobDetection
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.entity.EntityHurtEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

@SkyHanniModule
object FmlEventApi {

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        ClientDisconnectEvent.post()
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        EntityEnterWorldEvent(event.entity).post()
    }

    @SubscribeEvent
    fun onEntityHurt(event: LivingAttackEvent) {
        val entity = event.entity
        val source = event.source
        val amount = event.ammount
        EntityHurtEvent(entity, source, amount).post()
        val skyblockMob = MobData.entityToMob[entity] ?: return
        MobDetection.postMobHurtEvent(skyblockMob, source, amount)
    }
}
