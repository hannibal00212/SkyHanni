package at.hannibal2.skyhanni.features.rift.area.livingcave

import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object LivingCaveSnakeFeatures {
    private val config get() = RiftAPI.config.area.livingCave.livingCaveLivingMetalConfig
    private var snakes = emptyList<Snake>()

    class Snake(var show: Boolean, var blocks: List<LorenzVec>)

    private val originalBlocks = mutableMapOf<LorenzVec, String>()

    @SubscribeEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!isEnabled()) return
        val location = event.location
        val old = event.old
        val new = event.new

        // TODO remove
        if (Minecraft.getMinecraft().thePlayer.isSneaking) {
            snakes = emptyList()
        }

        val distance = 5

        if (new == "lapis_block") {
            val snake = snakes.filter { it.blocks.isNotEmpty() && it.blocks.last().distance(location) < distance }
                .minByOrNull { it.blocks.last().distance(location) }
            if (snake != null) {
                snake.blocks = snake.blocks.editCopy { add(location) }
                println("added from snake: ${snake.blocks.size}")
            } else {
                snakes = snakes.editCopy { add(Snake(false, listOf(location))) }
            }
            originalBlocks[location] = old
        }
        for (snake in snakes) {
            if (location in snake.blocks) {
                if (originalBlocks[location] == new || new == "lapis_ore") {
                    snake.blocks = snake.blocks.editCopy { remove(location) }
                    snake.show = true
                    println("removed from snake: ${snake.blocks.size}")
                }
            }
        }
//        if (old == "lapis_block") {
//            for (snake in snakes) {
//                if (snake.blocks.contains(location)) {
//                    snake.blocks = snake.blocks.editCopy { remove(location) }
//                    println("removed from snake: ${snake.blocks.size}")
//                }
//            }
//        }
    }

    @SubscribeEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!isEnabled()) return
//        if (event.clickType == ClickType.LEFT_CLICK) {
//            val name = event.getBlockState.block.toString()
//            if (name.contains("lapis_ore")) {
//                lastClicked = event.position
//            }
//        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        for (snake in snakes) {
            if (!snake.show) return
            val blocks = snake.blocks
            if (blocks.size > 1) {
                blocks.first().let {
                    event.drawWaypointFilled(it, LorenzColor.GREEN.toColor(), seeThroughBlocks = true)
                    event.drawString(it.add(0.5, 0.5, 0.5), "§aEnd", seeThroughBlocks = true)
                }
                blocks.last().let {
                    event.drawWaypointFilled(it, LorenzColor.YELLOW.toColor(), seeThroughBlocks = true)
                    event.drawString(it.add(0.5, 0.5, 0.5), "§eStart", seeThroughBlocks = true)
                }
            }
            for ((a, b) in blocks.zipWithNext()) {
                event.draw3DLine(a.add(0.5, 0.5, 0.5), b.add(0.5, 0.5, 0.5), LorenzColor.BLUE.toColor(), 3, false)
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && (RiftAPI.inLivingCave() || RiftAPI.inLivingStillness()) && config.enabled
}
