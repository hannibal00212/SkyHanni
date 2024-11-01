package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.projectile.EntitySmallFireball
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ParticleHider {

    private fun inM7Boss() = DungeonAPI.inDungeon() && DungeonAPI.dungeonFloor == "M7" && DungeonAPI.inBossRoom

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        val distanceToPlayer = event.distanceToPlayer
        if (SkyHanniMod.feature.misc.particleHiders.hideFarParticles && distanceToPlayer > 40 && !inM7Boss()) {
            event.cancel()
            return
        }

        val type = event.type
        if (SkyHanniMod.feature.misc.particleHiders.hideCloseRedstoneParticles &&
            type == EnumParticleTypes.REDSTONE && distanceToPlayer < 2
        ) {
            event.cancel()
            return
        }

        if (SkyHanniMod.feature.misc.particleHiders.hideFireballParticles &&
            (type == EnumParticleTypes.SMOKE_NORMAL || type == EnumParticleTypes.SMOKE_LARGE)
        ) {
            for (entity in EntityUtils.getEntities<EntitySmallFireball>()) {
                val distance = entity.getLorenzVec().distance(event.location)
                if (distance < 5) {
                    event.cancel()
                    return
                }
            }
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "misc.hideBlazeParticles", "misc.particleHiders.hideBlazeParticles")
        event.move(3, "misc.hideEndermanParticles", "misc.particleHiders.hideEndermanParticles")
        event.move(3, "misc.hideFarParticles", "misc.particleHiders.hideFarParticles")
        event.move(3, "misc.hideFireballParticles", "misc.particleHiders.hideFireballParticles")
        event.move(3, "misc.hideCloseRedstoneparticles", "misc.particleHiders.hideCloseRedstoneParticles")
        event.move(3, "misc.hideFireBlockParticles", "misc.particleHiders.hideFireBlockParticles")
        event.move(3, "misc.hideSmokeParticles", "misc.particleHiders.hideSmokeParticles")
    }
}
