package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.entity.EntityDisplayNameEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthDisplayEvent
import at.hannibal2.skyhanni.events.entity.EntityLeaveWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object EntityData {

    private val maxHealthMap = mutableMapOf<Int, Int>()
    private val nametagCache = TimeLimitedCache<Entity, ChatComponentText>(50.milliseconds)
    private val healthDisplayCache = TimeLimitedCache<String, String>(50.milliseconds)

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        for (entity in EntityUtils.getEntities<EntityLivingBase>()) { // this completely ignores the ignored entities list?
            val maxHealth = entity.baseMaxHealth
            val id = entity.entityId
            val oldMaxHealth = maxHealthMap.getOrDefault(id, -1)
            if (oldMaxHealth != maxHealth) {
                maxHealthMap[id] = maxHealth
                EntityMaxHealthUpdateEvent(entity, maxHealth.derpy()).postAndCatch()
            }
        }
    }

    @HandleEvent
    fun onEntityLeaveWorld(event: EntityLeaveWorldEvent<EntityLivingBase>) {
        maxHealthMap -= event.entity.entityId
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        maxHealthMap.clear()
    }

    @JvmStatic
    fun getDisplayName(entity: Entity, oldValue: ChatComponentText): ChatComponentText {
        return postRenderNametag(entity, oldValue)
    }

    @JvmStatic
    fun despawnEntity(entity: Entity) {
        EntityLeaveWorldEvent(entity).post()
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
