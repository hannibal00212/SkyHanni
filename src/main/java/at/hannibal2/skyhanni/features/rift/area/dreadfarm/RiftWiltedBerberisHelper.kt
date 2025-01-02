package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object RiftWiltedBerberisHelper {
    //not a great programmer, but it's better than nothing :p -maj

    private val config get() = RiftAPI.config.area.dreadfarm.wiltedBerberis
    private var isOnFarmland = false
    private var hasFarmingToolInHand = false

    //list of berberis in the current plot, in the order they appeared in
    private var list = listOf<LorenzVec>()

    //array of the bounds of each berberis plot
    private val plots = arrayOf(
        Plot(LorenzVec(-54,71,-128), LorenzVec(-41,70,-117)),
        Plot(LorenzVec(-77,72,-143), LorenzVec(-59,71,-125)),
        Plot(LorenzVec(-87,73,-169), LorenzVec(-69,72,-152)),
        Plot(LorenzVec(-72,73,-191), LorenzVec(-57,72,-175)),
        Plot(LorenzVec(-35,72,-185), LorenzVec(-22,71,-171)),
        Plot(LorenzVec(-42,72,-155), LorenzVec(-22,70,-126))
    )

    //the closest plot to the player
    private var closestPlot = 0
    //the closest plot to the player last tick
    private var oldClosest = 0

    data class Plot(var c1: LorenzVec, var c2: LorenzVec)

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return

        //detect if the player enters a different plot
        if(closestPlot != oldClosest) list = list.editCopy { clear() }
        oldClosest = closestPlot

        //calculates the player's distance to the center of each plot, then sets closestPlot to the smallest
        var plotDistances = arrayOf(0.0,0.0,0.0,0.0,0.0,0.0)
        for (i in 0..5) plotDistances[i] = LocationUtils.playerLocation().distance(plots[i].c1.middle(plots[i].c2)) //this line is a monstrosity
        for (i in 0..5) if (plotDistances[i] < plotDistances[closestPlot]) closestPlot = i


        //remove first berberis from list if broken
        if(list.size > 1 && list[0].getBlockAt() != Blocks.deadbush) list = list.editCopy { removeFirst() }


        //when a berberis grows, add its location to the end of the list
        for (block in BlockPos.getAllInBox(plots[closestPlot].c1.toBlockPos(), plots[closestPlot].c2.toBlockPos())) {
            if (block.toLorenzVec().getBlockAt() == Blocks.deadbush && !list.contains(block.toLorenzVec())) {
                list = list.editCopy { add(block.toLorenzVec()) }
            }
        }

        //get if player holding farming wand
        hasFarmingToolInHand = InventoryUtils.getItemInHand()?.getInternalName() == RiftAPI.farmingTool

        //get if player is on farmland
        if (Minecraft.getMinecraft().thePlayer.onGround) {
            val block = LorenzVec.getBlockBelowPlayer().getBlockAt()
            val currentY = LocationUtils.playerLocation().y
            isOnFarmland = block == Blocks.farmland && (currentY % 1 == 0.0)
        }
    }

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        //hide particles when farming wand is out and the setting is enabled
        if (!isEnabled()) return
        if (!hasFarmingToolInHand) return

        if (config.hideParticles) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onPlaySound(event: PlaySoundEvent) {
        //mute sounds if setting on
        if (!isMuteOthersSoundsEnabled()) return
        val soundName = event.soundName

        if (soundName == "mob.horse.donkey.death" || soundName == "mob.horse.donkey.hit") {
            event.cancel()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (!hasFarmingToolInHand) return
        if (config.onlyOnFarmland && !isOnFarmland) return

        var alpha = 0.8f
        var previous: LorenzVec? = null
        event.drawDynamicText(list[0].up(), "§eWilted Berberis", 1.5, ignoreBlocks = false)

        //for the first 3 berberis
        for (i in 0..(list.size - 1).coerceAtMost(2)) {
            //box it with half the opacity of the previous box, first in list is yellow
            if (i == 0) event.drawFilledBoundingBoxNea(axisAlignedBB(list[i]), Color.YELLOW, alpha)
            else event.drawFilledBoundingBoxNea(axisAlignedBB(list[i]), Color.WHITE, alpha)
            alpha /= 2f

            //if there's a previous berberis, draw a line to it. The line from the 2nd to the 1st should be yellow
            if(i == 1) previous?.let { event.draw3DLine(list[i].add(0.5, 0.5, 0.5), it.add(0.5, 0.5, 0.5), Color.YELLOW, 4, false) }
            else previous?.let { event.draw3DLine(list[i].add(0.5, 0.5, 0.5), it.add(0.5, 0.5, 0.5), Color.WHITE, 2, false) }

            previous = list[i]
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(60, "rift.area.dreadfarm.wiltedBerberis.hideparticles", "rift.area.dreadfarm.wiltedBerberis.hideParticles")
    }

    private fun axisAlignedBB(loc: LorenzVec) = loc.add(0.1, -0.1, 0.1).boundingToOffset(0.8, 1.0, 0.8).expandBlock()

    private fun isEnabled() = RiftAPI.inRift() && RiftAPI.inDreadfarm() && config.enabled

    private fun isMuteOthersSoundsEnabled() = RiftAPI.inRift() &&
        config.muteOthersSounds &&
        (RiftAPI.inDreadfarm() || RiftAPI.inWestVillage()) &&
        !(hasFarmingToolInHand && isOnFarmland)
}
