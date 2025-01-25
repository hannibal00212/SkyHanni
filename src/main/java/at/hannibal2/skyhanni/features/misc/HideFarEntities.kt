package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityWither

@SkyHanniModule
object HideFarEntities {
    private val config get() = SkyHanniMod.feature.misc.hideFarEntities

    private var ignored = emptySet<Int>()

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        val maxAmount = config.maxAmount.coerceAtLeast(1)
        val minDistance = config.minDistance.coerceAtLeast(3)

        ignored = EntityUtils.getAllEntities()
            .map { it.entityId to it.distanceToPlayer() }
            .filter { it.second > minDistance }
            .sortedBy { it.second }.drop(maxAmount)
            .map { it.first }.toSet()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onCheckRender(event: CheckRenderEntityEvent<Entity>) {
        if (!isEnabled()) return
        val entity = event.entity
        if (entity is EntityWither && entity.entityId < 0) return
        if (entity.entityId in ignored) {
            event.cancel()
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled &&
        (!(GardenApi.inGarden() && config.excludeGarden) && !(DungeonApi.inDungeon() && config.excludeDungeon))

}
