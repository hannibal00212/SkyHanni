package at.hannibal2.skyhanni.features.rift.area.colosseum

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.MobFilter.isSkyBlockMob
import at.hannibal2.skyhanni.events.entity.EntityHurtEvent
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.RenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.removeIfKey
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.StringUtils.pluralize
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntitySlime
import java.awt.Color
import kotlin.math.ceil

@SkyHanniModule
object TentacleWaypoint {

    private val config get() = SkyHanniMod.feature.rift.area.colosseum
    private val tentacleHits = mutableMapOf<EntityLivingBase, Int>()

    private val VALID_SLIME_SIZES = 4..8
    private const val TENTACLE_FLOOR_Y = 68

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!isEnabled()) return
        val entity = event.entity as? EntitySlime ?: return
        if (!entity.isSkyBlockMob()) return
        if (entity.displayName.formattedText != "Slime§r") return
        // Only get the tentacle on the ground
        if (ceil(entity.posY).toInt() != TENTACLE_FLOOR_Y) return
        if (entity.slimeSize !in VALID_SLIME_SIZES) return
        if (entity in tentacleHits) return

        tentacleHits += entity to 0
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onEntityDamage(event: EntityHurtEvent<EntitySlime>) {
        if (!isEnabled()) return

        // Fixes Wall Damage counting as tentacle damage
        if (event.source.damageType != "generic") return
        tentacleHits[event.entity]?.let { tentacleHits[event.entity] = it + 1 }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onRender(event: RenderWorldEvent) {
        if (!isEnabled()) return
        tentacleHits.removeIfKey { it.isDead || it.health == 0f }

        for ((tentacle, hits) in tentacleHits) {
            val location = tentacle.getLorenzVec()
            event.drawWaypointFilled(
                location.add(-0.5, 0.0, -0.5),
                Color.RED,
                seeThroughBlocks = true,
                beacon = true,
            )
            event.drawDynamicText(location.up(1.0), getText(hits), 1.0)
        }
    }

    private fun getText(hits: Int) = if (BactePhase.currentPhase == BactePhase.BactePhase.PHASE_5) {
        "§a${pluralize(hits, "Hit", withNumber = true)}"
    } else {
        val maxHp = when (BactePhase.currentPhase) {
            BactePhase.BactePhase.PHASE_4 -> 3
            else -> 4
        }
        val hpColor = if (hits > 0) "§c" else "§a"
        "$hpColor${maxHp - hits}§a/$maxHp§c❤"
    }

    @HandleEvent
    fun onWorldSwitch(event: WorldChangeEvent) {
        tentacleHits.clear()
    }

    private fun isEnabled() = RiftAPI.inColosseum() && config.tentacleWaypoints
}
