package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.entity.boss.EntityWither
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HideFarEntities {
    private val config get() = SkyHanniMod.feature.misc.hideFarEntities

    private var ignored = emptySet<Int>()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        val maxAmount = config.maxAmount.coerceAtLeast(1)
        val minDistance = config.minDistance.coerceAtLeast(3)

        ignored = EntityUtils.getAllEntities()
            .map { it.entityId to it.distanceToPlayer() }
            .filter { it.second > minDistance }
            .sortedBy { it.second }.drop(maxAmount)
            .map { it.first }.toSet()
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        val entity = event.entity
        if (entity is EntityWither && entity.entityId < 0) return
        if (isEnabled() && entity.entityId in ignored) {
            event.cancel()
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled &&
        (!(GardenAPI.inGarden() && config.excludeGarden) && !(DungeonAPI.inDungeon() && config.excludeDungeon))

}
