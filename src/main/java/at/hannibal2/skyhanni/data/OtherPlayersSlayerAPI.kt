package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.entity.slayer.SlayerDeathEvent
import at.hannibal2.skyhanni.features.slayer.SlayerType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object OtherPlayersSlayerAPI {

    @SubscribeEvent
    fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        val mob = event.mob

        // no death, rather despawn because too far away
        if (mob.baseEntity.health != 0f) return

        if (mob.mobType != Mob.Type.SLAYER) return

        val owner = mob.owner?.ownerName
        val tier = mob.levelOrTier
        val name = mob.name
        val slayerType = SlayerType.getByName(name) ?: run {
            ErrorManager.logErrorStateWithData(
                "Unknown slayer type found", "unknown slayer",
                "name" to name,
            )
            return
        }

        SlayerDeathEvent(slayerType, tier, owner).post()
    }
}
