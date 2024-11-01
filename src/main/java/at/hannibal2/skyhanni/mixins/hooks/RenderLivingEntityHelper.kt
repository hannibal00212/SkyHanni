package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.entity.EntityLeaveWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RenderLivingEntityHelper {

    private val entityColorMap = mutableMapOf<EntityLivingBase, Int>()
    private val entityColorCondition = mutableMapOf<EntityLivingBase, () -> Boolean>()

    private val entityNoHurtTimeCondition = mutableMapOf<EntityLivingBase, () -> Boolean>()

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        entityColorMap.clear()
        entityColorCondition.clear()

        entityNoHurtTimeCondition.clear()
    }

    @HandleEvent
    fun onEntityLeaveWorld(event: EntityLeaveWorldEvent<EntityLivingBase>) {
        removeCustomRender(event.entity)
    }

    fun <T : EntityLivingBase> removeEntityColor(entity: T) {
        entityColorMap.remove(entity)
        entityColorCondition.remove(entity)
    }

    fun <T : EntityLivingBase> setEntityColor(entity: T, color: Int, condition: () -> Boolean) {
        entityColorMap[entity] = color
        entityColorCondition[entity] = condition
    }

    fun <T : EntityLivingBase> setNoHurtTime(entity: T, condition: () -> Boolean) {
        entityNoHurtTimeCondition[entity] = condition
    }

    fun <T : EntityLivingBase> setEntityColorWithNoHurtTime(entity: T, color: Int, condition: () -> Boolean) {
        setEntityColor(entity, color, condition)
        setNoHurtTime(entity, condition)
    }

    fun <T : EntityLivingBase> removeNoHurtTime(entity: T) {
        entityNoHurtTimeCondition.remove(entity)
    }

    fun <T : EntityLivingBase> removeCustomRender(entity: T) {
        removeEntityColor(entity)
        removeNoHurtTime(entity)
    }

    @JvmStatic
    fun <T : EntityLivingBase> internalSetColorMultiplier(entity: T): Int {
        if (!SkyHanniDebugsAndTests.globalRender) return 0
        if (entityColorMap.containsKey(entity)) {
            val condition = entityColorCondition[entity]!!
            if (condition.invoke()) {
                return entityColorMap[entity]!!
            }
        }
        return 0
    }

    @JvmStatic
    fun <T : EntityLivingBase> internalChangeHurtTime(entity: T): Int {
        if (!SkyHanniDebugsAndTests.globalRender) return entity.hurtTime
        run {
            val condition = entityNoHurtTimeCondition[entity] ?: return@run
            if (condition.invoke()) {
                return 0
            }
        }
        return entity.hurtTime
    }
}
