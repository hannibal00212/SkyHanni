package at.hannibal2.skyhanni.features.slayer.enderman

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.util.EnumParticleTypes

@SkyHanniModule
object EndermanSlayerHideParticles {

    private var endermanLocations = listOf<LorenzVec>()

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        endermanLocations = EntityUtils.getEntities<EntityEnderman>().map { it.getLorenzVec() }.toList()
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return

        when (event.type) {
            EnumParticleTypes.SMOKE_LARGE,
            EnumParticleTypes.FLAME,
            EnumParticleTypes.SPELL_WITCH,
            -> {
            }

            else -> return
        }

        val distance = event.location.distanceToNearestEnderman() ?: return
        if (distance < 9) {
            event.cancel()
        }
    }

    private fun LorenzVec.distanceToNearestEnderman() = endermanLocations.minOfOrNull { it.distanceSq(this) }

    fun isEnabled() = IslandType.THE_END.isInIsland() && SkyHanniMod.feature.slayer.endermen.hideParticles

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "slayer.endermanHideParticles", "slayer.endermen.hideParticles")
    }
}
