package at.hannibal2.skyhanni.features.rift.area.mountaintop

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.block.BlockStainedGlassPane
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TimiteHelper {

    private val TIME_GUN = "TIME_GUN".toInternalName()
    private var holdingClick = SimpleTimeMark.farPast()
    private var lastClick = SimpleTimeMark.farPast()
    private val config get() = SkyHanniMod.feature.rift.area.mountaintop.timite
    private var currentPos: LorenzVec? = null
    private var currentBlockState: IBlockState? = null
    private var doubleTimeShooting = false

    @HandleEvent
    fun onBlockHit(event: BlockClickEvent) {
        if (!isEnabled()) return
        if (InventoryUtils.itemInHandId != TIME_GUN) return
        if (event.clickType != ClickType.RIGHT_CLICK) return
        if (event.position != currentPos || currentBlockState != event.getBlockState) {
            lastClick = SimpleTimeMark.farPast()

            if (event.position == currentPos && currentBlockState != event.getBlockState) {
                timiteLocations[event.position] = SimpleTimeMark.now()
                doubleTimeShooting = true
            } else {
                doubleTimeShooting = false
            }
        }
        currentPos = event.position
        currentBlockState = event.getBlockState
        if (event.getBlockState.block != Blocks.stained_glass_pane) return
        val color = event.getBlockState.getValue(BlockStainedGlassPane.COLOR)
        if (color != EnumDyeColor.BLUE && color != EnumDyeColor.LIGHT_BLUE) return
        if (lastClick + 300.milliseconds > SimpleTimeMark.now()) {
            lastClick = SimpleTimeMark.now()
            return
        }
        lastClick = SimpleTimeMark.now()
        holdingClick = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (InventoryUtils.itemInHandId != TIME_GUN) return
        if (lastClick + 400.milliseconds < SimpleTimeMark.now()) {
            holdingClick = SimpleTimeMark.farPast()
            doubleTimeShooting = false
        }
        if (holdingClick.isFarPast()) return

        if ((currentBlockState?.block ?: return) != Blocks.stained_glass_pane) return
        // this works for me but idk if ive just tuned it for my ping only
        val time = if (doubleTimeShooting) 1800 else 2000
        val timeLeft = holdingClick + time.milliseconds
        if (!timeLeft.isInPast()) {
            val formattedTime = timeLeft.timeUntil().format(showMilliSeconds = true)
            config.timerPos.renderString("§b$formattedTime", 0, 0, "Timite Helper")
        }
    }

    private val timiteLocations = mutableMapOf<LorenzVec, SimpleTimeMark>()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        val location = LocationUtils.playerLocation()
        val from = location.add(-15, -15, -15).toBlockPos()
        val to = location.add(15, 15, 15).toBlockPos()

        for (pos in BlockPos.getAllInBox(from, to)) {
            val loc = pos.toLorenzVec()
            if (loc.getBlockAt() == Blocks.stained_glass_pane && loc.distanceToPlayer() <= 15) {
                val color = loc.getBlockStateAt().getValue(BlockStainedGlassPane.COLOR)
                if (color != EnumDyeColor.BLUE && color != EnumDyeColor.LIGHT_BLUE) continue
                if (timiteLocations[loc] == null) timiteLocations[loc] = SimpleTimeMark.now()
            }
        }
        val iterator = timiteLocations.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key.getBlockAt() == Blocks.air) {
                iterator.remove()
            } else if (entry.key.getBlockAt() == Blocks.stained_glass_pane) {
                val color = entry.key.getBlockStateAt().getValue(BlockStainedGlassPane.COLOR)
                if (color == EnumDyeColor.LIGHT_BLUE) {
                    iterator.remove()
                }
            }
        }


    }

    @SubscribeEvent
    fun onBlockRender(event: LorenzRenderWorldEvent) {
        if (!RiftAPI.inMountainTop() || !config.timiteExpiryTimer) return

        for (timiteLocation in timiteLocations.entries) {
            val timeLeft = timiteLocation.value + 31.seconds
            if (timeLeft.timeUntil() < 6.seconds) {
                event.drawDynamicText(timiteLocation.key, "§c${timeLeft.timeUntil().format()}", 1.5)
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) = timiteLocations.clear()


    private fun isEnabled() = RiftAPI.inMountainTop() && config.timiteTimer

}
