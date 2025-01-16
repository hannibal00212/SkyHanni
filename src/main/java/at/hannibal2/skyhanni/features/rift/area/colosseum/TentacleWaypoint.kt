package at.hannibal2.skyhanni.features.rift.area.colosseum

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.MobFilter.isSkyBlockMob
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.entity.EntityHurtEvent
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.RenderWorldEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.StringUtils.pluralize
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntitySlime
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.ceil

@SkyHanniModule
object TentacleWaypoint {

    private val config get() = SkyHanniMod.feature.rift.area.colosseum
    private var tentacles = mutableMapOf<EntityLivingBase, Int>()

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!isEnabled()) return
        val entity = event.entity as? EntitySlime ?: return
        if (!entity.isSkyBlockMob()) return
        if (entity.displayName.formattedText != "Slime§r") return
        // Only get the tentacle on the ground
        if (ceil(entity.posY).toInt() != 68) return
        // Only get the tentacle with size 4 to 8
        if (entity.slimeSize !in 4..8) return
        if (entity in tentacles) return

        tentacles += entity to 0
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onEntityDamage(event: EntityHurtEvent<EntitySlime>) {
        if (!isEnabled()) return

        // Fixes Wall Damage counting as tentacle damage
        if (event.source.damageType != "generic") return
        tentacles[event.entity]?.let { tentacles[event.entity] = it + 1 }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onRender(event: RenderWorldEvent) {
        if (!isEnabled()) return
        tentacles = tentacles.filterNot { it.key.isDead || it.key.health == 0f }.toMutableMap()

        for ((tentacle, hits) in tentacles) {
            event.drawWaypointFilled(
                tentacle.getLorenzVec().add(-0.5, 0.0, -0.5),
                Color.RED,
                seeThroughBlocks = true,
                beacon = true,
            )

            val text = if (BactePhase.currentPhase == BactePhase.BactePhase.PHASE_5) {
                "§a${pluralize(hits, "Hit", withNumber = true)}"
            } else {
                val maxHp = when (BactePhase.currentPhase) {
                    BactePhase.BactePhase.PHASE_4 -> 3
                    else -> 4
                }
                val hpColor = if (hits > 0) "§c" else "§a"
                "$hpColor${maxHp - hits}§a/$maxHp§c❤"
            }

            event.drawDynamicText(
                tentacle.getLorenzVec().up(1.0),
                text,
                1.0,
            )
        }
    }

    @SubscribeEvent
    fun onWorldSwitch(event: LorenzWorldChangeEvent) {
        tentacles.clear()
    }

    fun isEnabled() = RiftAPI.inColosseum() && config.tentacleWaypoints

}
