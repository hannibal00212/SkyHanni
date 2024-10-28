package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChumBucketHider {

    private val config get() = SkyHanniMod.feature.fishing.chumBucketHider
    private val titleEntity = TimeLimitedSet<Entity>(5.seconds)
    private val hiddenEntities = TimeLimitedSet<Entity>(5.seconds)

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        reset()
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.enabled.get()) return

        val entity = event.entity
        if (entity !is EntityArmorStand) return

        if (entity in hiddenEntities) {
            event.cancel()
            return
        }

        val name = entity.name

        // First text line
        if (name.endsWith("'s Chum Bucket") || name.endsWith("'s Chumcap Bucket")) {
            if (name.contains(LorenzUtils.getPlayerName()) && !config.hideOwn.get()) return
            titleEntity.add(entity)
            hiddenEntities.add(entity)
            event.cancel()
            return
        }

        // Second text line
        if (name.contains("/10 §aChums")) {
            val entityLocation = entity.getLorenzVec()
            for (title in titleEntity.toSet()) {
                if (entityLocation.equalsIgnoreY(title.getLorenzVec())) {
                    hiddenEntities.add(entity)
                    event.cancel()
                    return
                }
            }
        }

        // Chum Bucket
        if (config.hideBucket.get() &&
            entity.inventory.any {
                it != null && (it.name == "§fEmpty Chum Bucket" || it.name == "§aEmpty Chumcap Bucket")
            }
        ) {
            val entityLocation = entity.getLorenzVec()
            for (title in titleEntity.toSet()) {
                if (entityLocation.equalsIgnoreY(title.getLorenzVec())) {
                    hiddenEntities.add(entity)
                    event.cancel()
                    return
                }
            }
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.enabled, config.hideBucket, config.hideOwn) { reset() }
    }

    private fun reset() {
        titleEntity.clear()
        hiddenEntities.clear()
    }
}
