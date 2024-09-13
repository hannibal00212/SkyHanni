package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.entity.EntityDisplayNameEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthDisplayEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object EntityData {

    private val maxHealthMap = mutableMapOf<EntityLivingBase, Int>()
    private val nametagCache = TimeLimitedCache<Entity, ChatComponentText>(50.milliseconds)
    private val healthDisplayCache = TimeLimitedCache<String, String>(50.milliseconds)

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        for (entity in EntityUtils.getEntities<EntityLivingBase>()) {
            val maxHealth = entity.baseMaxHealth
            val oldMaxHealth = maxHealthMap.getOrDefault(entity, -1)
            if (oldMaxHealth != maxHealth) {
                maxHealthMap[entity] = maxHealth
                EntityMaxHealthUpdateEvent(entity, maxHealth.derpy()).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(30)) {
            maxHealthMap.keys.removeIf { it.isDead }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        maxHealthMap.clear()
    }

    @HandleEvent
    fun onHealthUpdatePacket(event: PacketReceivedEvent) {
        val packet = event.packet

        if (packet !is S1CPacketEntityMetadata) return

        val watchableObjects = packet.func_149376_c() ?: return
        val entityId = packet.entityId

        val entity = EntityUtils.getEntityByID(entityId) ?: return
        if (entity is EntityArmorStand) return
        if (entity is EntityXPOrb) return
        if (entity is EntityItem) return
        if (entity is EntityItemFrame) return

        if (entity is EntityOtherPlayerMP) return
        if (entity is EntityPlayerSP) return

        for (watchableObject in watchableObjects) {

            val dataValueId = watchableObject.dataValueId
            val any = watchableObject.`object`
            if (dataValueId != 6) continue

            val health = (any as Float).toInt()

            if (entity is EntityWither && health == 300 && entityId < 0) {
                return
            }

            if (entity is EntityLivingBase) {
                EntityHealthUpdateEvent(entity, health.derpy()).postAndCatch()
            }
        }
    }

    @JvmStatic
    fun getDisplayName(entity: Entity, oldValue: ChatComponentText): ChatComponentText {
        return postRenderNametag(entity, oldValue)
    }

    private fun postRenderNametag(entity: Entity, chatComponent: ChatComponentText) = nametagCache.getOrPut(entity) {
        val event = EntityDisplayNameEvent(entity, chatComponent)
        event.postAndCatch()
        event.chatComponent
    }

    @JvmStatic
    fun getHealthDisplay(text: String) = healthDisplayCache.getOrPut(text) {
        val event = EntityHealthDisplayEvent(text)
        event.postAndCatch()
        event.text
    }

}
