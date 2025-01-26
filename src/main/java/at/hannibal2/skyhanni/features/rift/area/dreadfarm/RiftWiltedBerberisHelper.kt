package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object RiftWiltedBerberisHelper {

    private val config get() = RiftApi.config.area.dreadfarm.wiltedBerberis
    private var isOnFarmland = false
    private var hasFarmingToolInHand = false
    private var berberisList = listOf<LorenzVec>()
    private var lastSpawn = SimpleTimeMark.now()
    private var lastSyncedAt = SimpleTimeMark.now()
    private var lastUpdated = SimpleTimeMark.now()

    // the bounds of each berberis plot
    private val plots = arrayOf(
        Plot(LorenzVec(-54, 71, -128), LorenzVec(-41, 70, -117)),
        Plot(LorenzVec(-77, 72, -143), LorenzVec(-59, 71, -125)),
        Plot(LorenzVec(-87, 73, -169), LorenzVec(-69, 72, -152)),
        Plot(LorenzVec(-72, 73, -191), LorenzVec(-57, 72, -175)),
        Plot(LorenzVec(-35, 72, -185), LorenzVec(-22, 71, -171)),
        Plot(LorenzVec(-42, 72, -155), LorenzVec(-22, 70, -126)),
    )

    private var closestPlot = 0
    private var oldClosest = 0
    private var fallback = false

    data class Plot(var a: LorenzVec, var b: LorenzVec)

    private var list = listOf<WiltedBerberis>()

    data class WiltedBerberis(var currentParticles: LorenzVec) {
        var previous: LorenzVec? = null
        var moving = true
        var y = 0.0
        var lastTime = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return

        getClosestPlot()
        updateBerberisList()
        sync()

        hasFarmingToolInHand = InventoryUtils.getItemInHand()?.getInternalName() == RiftApi.farmingTool

        if (Minecraft.getMinecraft().thePlayer.onGround) {
            val block = LorenzVec.getBlockBelowPlayer().getBlockAt()
            val currentY = LocationUtils.playerLocation().y
            isOnFarmland = block == Blocks.farmland && (currentY % 1 == 0.0)
        }
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (!hasFarmingToolInHand) return

        val location = event.location
        val berberis = nearestBerberis(location)

        // the purple particles on the edges dont get touched, just cancel them if the setting is on
        if (event.type != EnumParticleTypes.FIREWORKS_SPARK) {
            if (config.hideParticles && berberis != null) {
                event.cancel()
            }
            return
        }
        // the firework sparks in the center may get cancelled, but the below code runs on them
        if (config.hideParticles) {
            event.cancel()
        }

        if (berberis == null) {
            list = list.editCopy { add(WiltedBerberis(location)) }
            return
        }

        with(berberis) {
            val isMoving = currentParticles != location
            if (isMoving) {
                if (currentParticles.distance(location) > 3) {
                    previous = null
                    moving = true
                }
                if (!moving) {
                    previous = currentParticles
                }
            }
            if (!isMoving) {
                y = location.y - 1
            }

            moving = isMoving
            currentParticles = location
            lastTime = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onPlaySound(event: PlaySoundEvent) {
        // mute sounds if setting on
        if (!isMuteOthersSoundsEnabled()) return
        val soundName = event.soundName

        if (soundName == "mob.horse.donkey.death" || soundName == "mob.horse.donkey.hit") {
            event.cancel()
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (!hasFarmingToolInHand) return
        if (config.onlyOnFarmland && !isOnFarmland) return

        if (fallback) fallbackRender(event)
        else primaryRender(event)
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(71, "rift.area.dreadfarm.wiltedBerberis.hideparticles", "rift.area.dreadfarm.wiltedBerberis.hideParticles")
    }

    private fun getClosestPlot() {
        // calculates the player's distance to the center of each plot, then sets closestPlot to the smallest
        val plotDistances = arrayListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        for (i in 0..5) {
            val plotCenter = plots[i].a.middle(plots[i].b)
            plotDistances[i] = LocationUtils.playerLocation().distance(plotCenter)
        }
        for (i in 0..5) if (plotDistances[i] < plotDistances[closestPlot]) closestPlot = i
    }

    private fun updateBerberisList() {
        // if the player enters a new plot, clear the list of berberis locations
        if (closestPlot != oldClosest) berberisList = berberisList.editCopy { clear() }
        oldClosest = closestPlot

        // when a berberis grows in the current plot, add its location to the end of the list
        val plotCornerA = plots[closestPlot].a.toBlockPos()
        val plotCornerB = plots[closestPlot].b.toBlockPos()
        for (block in BlockPos.getAllInBox(plotCornerA, plotCornerB)) {
            if (block.toLorenzVec().getBlockAt() == Blocks.deadbush && !berberisList.contains(block.toLorenzVec())) {
                berberisList = berberisList.editCopy { add(block.toLorenzVec()) }
                lastSpawn = SimpleTimeMark.now()
                lastUpdated = SimpleTimeMark.now()
            }
        }

        // remove first berberis from list if broken and no berberis have grown in the last 1/4 seccond
        // (to stop you from breaking it before they all spawn in)
        while (berberisList.isNotEmpty() && berberisList[0].getBlockAt() != Blocks.deadbush && lastSpawn.passedSince() > 250.milliseconds) {
            berberisList = berberisList.editCopy { removeFirst() }
            lastUpdated = SimpleTimeMark.now()
        }

        // update the berberis list for the original system
        list = list.editCopy { removeIf { it.lastTime.passedSince() > 500.milliseconds } }
    }

    private fun sync() {
        // check if the new system is right about which bush to break. If the particle is still moving, assume it's right for now
        for (berberis in list) {
            with(berberis) {
                // if there is a particle in the same place as where the new helper thinks the next bush is,
                if (berberisList.isNotEmpty() && (currentParticles.distance(berberisList[0])) < 1.3 &&
                    currentParticles.distanceToPlayer() <= 20 && y != 0.0
                ) {
                    lastSyncedAt = SimpleTimeMark.now()
                }
                // or if there is a moving particle
                if (moving) {
                    lastSyncedAt = SimpleTimeMark.now()
                }
            }
        }

        // if we've been desynced (new system wrong) for more than 2 secconds and the list hasn't updated in that time,
        // switch to fallback mode. switch off of fallback once the plot is cleared
        if (lastSyncedAt.passedSince() > 1000.milliseconds && lastUpdated.passedSince() > 1000.milliseconds) fallback = true
        if (berberisList.isEmpty()) fallback = false
    }

    private fun fallbackRender(event: RenderWorldEvent) {
        for (berberis in list) {
            with(berberis) {
                if (currentParticles.distanceToPlayer() > 20) continue
                if (y == 0.0) continue

                val location = currentParticles.fixLocation(berberis)
                if (!moving) {
                    event.drawBox(location, config.highlightColor.toSpecialColor(), 0.7f)
                    event.drawDynamicText(location.up(), "§eWilted Berberis", 1.5, ignoreBlocks = false)
                } else {
                    event.drawBox(location, Color.WHITE, 0.5f)
                    previous?.fixLocation(berberis)?.let {
                        event.drawBox(it, Color.LIGHT_GRAY, 0.2f)
                        event.draw3DLine(it.add(0.5, 0.0, 0.5), location.add(0.5, 0.0, 0.5), Color.WHITE, 3, false)
                    }
                }
            }
        }
    }

    private fun primaryRender(event: RenderWorldEvent) {
        if (berberisList.isEmpty()) return
        var alpha = 0.8f
        var previousBerberis: LorenzVec? = null
        event.drawDynamicText(berberisList[0].up(), "§eWilted Berberis", 1.5, ignoreBlocks = false)

        berberisList.take(config.previewCount + 1).forEachIndexed { i, loc ->
            // box it with half the opacity of the previous box, first in list is highlighted
            if (i == 0) event.drawBox(loc, config.highlightColor.toSpecialColor(), alpha)
            else event.drawBox(loc, Color.WHITE, alpha)
            alpha *= 0.6f

            // if there's a previous berberis, draw a line to it. The line from the 2nd to the 1st should be highlighted
            if (i == 1) {
                previousBerberis?.let {
                    event.draw3DLine(loc.add(0.5, 0.5, 0.5), it.add(0.5, 0.5, 0.5), config.highlightColor.toSpecialColor(), 4, false)
                }
            } else {
                previousBerberis?.let {
                    event.draw3DLine(loc.add(0.5, 0.5, 0.5), it.add(0.5, 0.5, 0.5), Color.WHITE, 2, false)
                }
            }

            previousBerberis = loc
        }
    }

    private fun nearestBerberis(location: LorenzVec): WiltedBerberis? =
        list.filter { it.currentParticles.distanceSq(location) < 8 }
            .minByOrNull { it.currentParticles.distanceSq(location) }

    private fun LorenzVec.fixLocation(wiltedBerberis: WiltedBerberis): LorenzVec {
        val x = x - 0.5
        val y = wiltedBerberis.y
        val z = z - 0.5
        return LorenzVec(x, y, z)
    }

    private fun axisAlignedBB(loc: LorenzVec) = loc.add(0.1, -0.1, 0.1).boundingToOffset(0.8, 1.0, 0.8).expandBlock()

    private fun RenderWorldEvent.drawBox(location: LorenzVec, color: Color, alphaMultiplier: Float) {
        drawFilledBoundingBoxNea(axisAlignedBB(location), color, alphaMultiplier)
    }

    private fun isEnabled() = RiftApi.inRift() && RiftApi.inDreadfarm() && config.enabled

    private fun isMuteOthersSoundsEnabled() = RiftApi.inRift() &&
        config.muteOthersSounds &&
        (RiftApi.inDreadfarm() || RiftApi.inWestVillage()) &&
        !(hasFarmingToolInHand && isOnFarmland)
}
