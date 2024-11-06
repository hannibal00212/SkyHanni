package at.hannibal2.skyhanni.features.rift.area.livingcave

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.isAtFullHealth
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object LivingCaveDefenseBlocks {

    private val config get() = RiftAPI.config.area.livingCave.defenseBlockConfig
    private var movingBlocks = mapOf<DefenseBlock, Long>()
    private var staticBlocks = emptyList<DefenseBlock>()

    class DefenseBlock(val entity: EntityOtherPlayerMP, val location: LorenzVec, var hidden: Boolean = false)

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        staticBlocks = staticBlocks.editCopy { removeIf { it.entity.isDead } }
    }

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return

        movingBlocks = movingBlocks.editCopy {
            values.removeIf { System.currentTimeMillis() > it + 2000 }
            keys.removeIf { staticBlocks.any { others -> others.location.distance(it.location) < 1.5 } }
        }

        val location = event.location.add(-0.5, 0.0, -0.5)

        // Ignore particles around blocks
        if (staticBlocks.any { it.location.distance(location) < 3 }) {
            if (config.hideParticles) {
                event.cancel()
            }
            return
        }
        if (config.hideParticles && movingBlocks.keys.any { it.location.distance(location) < 3 }) {
            event.cancel()
        }

        if (event.type == EnumParticleTypes.CRIT_MAGIC) {
            var entity: EntityOtherPlayerMP? = null

            // read old entity data
            getNearestMovingDefenseBlock(location)?.let {
                if (it.location.distance(location) < 0.5) {
                    movingBlocks = movingBlocks.editCopy {
                        it.hidden = true
                    }
                    entity = it.entity
                }
            }

            if (entity == null) {
                // read new entity data
                val compareLocation = event.location.add(-0.5, -1.5, -0.5)
                entity = EntityUtils.getEntitiesNearby<EntityOtherPlayerMP>(compareLocation, 2.0)
                    .filter { isCorrectMob(it.name) }
                    .filter { !it.isAtFullHealth() }
                    .minByOrNull { it.distanceTo(compareLocation) }
            }

            val defenseBlock = entity?.let { DefenseBlock(it, location) } ?: return

            movingBlocks = movingBlocks.editCopy { this[defenseBlock] = System.currentTimeMillis() + 250 }
            if (config.hideParticles) {
                event.cancel()
            }
        }
    }

    private fun isCorrectMob(name: String) = when (name) {
        "Autonull ",

        "Autocap ",
        "Autochest ",
        "Autopants ",
        "Autoboots ",
        -> true

        else -> false
    }

    @SubscribeEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!isEnabled()) return
        val location = event.location
        val old = event.old
        val new = event.new

        // spawn block
        if (old == "air" && (new == "stained_glass" || new == "diamond_block")) {
            val entity = getNearestMovingDefenseBlock(location)?.entity ?: return
            staticBlocks = staticBlocks.editCopy {
                add(DefenseBlock(entity, location))
                RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                    entity,
                    color.withAlpha(50)
                ) { isEnabled() && staticBlocks.any { it.entity == entity } }
            }
        }

        // despawn block
        val nearestBlock = getNearestStaticDefenseBlock(location)
        if (new == "air" && location == nearestBlock?.location) {
            staticBlocks = staticBlocks.editCopy { remove(nearestBlock) }
        }
    }

    private fun getNearestMovingDefenseBlock(location: LorenzVec) =
        movingBlocks.keys.filter { it.location.distance(location) < 15 }
            .minByOrNull { it.location.distance(location) }

    private fun getNearestStaticDefenseBlock(location: LorenzVec) =
        staticBlocks.filter { it.location.distance(location) < 15 }.minByOrNull { it.location.distance(location) }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        for ((block, time) in movingBlocks) {
            if (block.hidden) continue
            if (time > System.currentTimeMillis()) {
                val location = block.location
                event.drawWaypointFilled(location, color)
                event.drawLineToEye(
                    location.blockCenter(),
                    color,
                    1,
                    false,
                )
            }
        }
        for (block in staticBlocks) {
            val location = block.location
            event.drawDynamicText(location, "§bBreak!", 1.5, ignoreBlocks = false)
            event.drawWaypointFilled(location, color)

            event.draw3DLine(
                event.exactLocation(block.entity).up(0.5),
                location.blockCenter(),
                color,
                3,
                true,
            )
        }
    }

    val color get() = config.color.get().toChromaColor()

    fun isEnabled() = RiftAPI.inRift() && config.enabled && RiftAPI.inLivingCave()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(9, "rift.area.livingCaveConfig", "rift.area.livingCave")
    }
}
