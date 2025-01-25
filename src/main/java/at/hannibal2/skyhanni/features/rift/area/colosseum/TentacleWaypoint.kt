package at.hannibal2.skyhanni.features.rift.area.colosseum

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
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
    fun onEntityHealthUpdate(event: MobEvent.Spawn.Special) {
        if (!isEnabled()) return
        val entity = event.mob.baseEntity as? EntitySlime ?: return
        if (event.mob.name != "Bacte Tentacle") return
        // Only get the tentacle on the ground
        if (ceil(entity.posY).toInt() != TENTACLE_FLOOR_Y) return
        if (entity.slimeSize !in VALID_SLIME_SIZES) return
        if (entity in tentacleHits) return

        tentacleHits += (event.mob.baseEntity as EntitySlime) to 0
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onEntityDamage(event: MobEvent.Hurt.Special) {
        if (!isEnabled()) return
        val entity = event.mob.baseEntity as? EntitySlime ?: return

        // Fixes Wall Damage counting as tentacle damage
        if (event.source.damageType != "generic") return
        tentacleHits[entity]?.let { tentacleHits[entity] = it + 1 }
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onRender(event: SkyHanniRenderWorldEvent) {
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
            event.drawDynamicText(location.add(-0.5, 1.0, -0.5), getText(hits), 1.2)
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

    private fun isEnabled() = RiftApi.inColosseum() && config.tentacleWaypoints
}
