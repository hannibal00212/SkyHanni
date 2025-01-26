package at.hannibal2.skyhanni.features.rift.area.livingcave

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.graph.GraphEditor.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object LivingCaveSnakeFeatures {
    private val config get() = RiftApi.config.area.livingCave.livingCaveLivingMetalConfig
    private var snakes = emptyList<Snake>()

    class Snake(
        var blocks: List<LorenzVec>,
        var lastRemoveTime: SimpleTimeMark = SimpleTimeMark.now(),
        var lastAddTime: SimpleTimeMark = SimpleTimeMark.now(),
        var phase: Phase = Phase.SPAWNING,
    )

    enum class Phase(val color: LorenzColor) {
        SPAWNING(LorenzColor.AQUA),
        MOVING(LorenzColor.YELLOW),
        NOT_TOUCHING_AIR(LorenzColor.RED),
        PAUSED(LorenzColor.GREEN),
    }

    private val originalBlocks = mutableMapOf<LorenzVec, Block>()

    var selectedSnake: Snake? = null

    @HandleEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!isEnabled()) return
        val location = event.location
        val old = event.oldState.block
        val new = event.newState.block

        // TODO remove
        if (Minecraft.getMinecraft().thePlayer.isSneaking) {
            snakes = emptyList()
        }

        val distance = 5

        if (new == Blocks.lapis_block) {
            val snake = snakes.filter { it.blocks.isNotEmpty() && it.blocks.last().distance(location) < distance }
                .minByOrNull { it.blocks.last().distance(location) }
            if (snake != null) {
                snake.blocks = snake.blocks.editCopy { add(location) }
                snake.lastAddTime = SimpleTimeMark.now()
            } else {
                snakes = snakes.editCopy { add(Snake(listOf(location))) }
            }
            originalBlocks[location] = old
        }
        for (snake in snakes) {
            if (location in snake.blocks) {
                if (originalBlocks[location] == new || new == Blocks.lapis_ore) {
                    snake.blocks = snake.blocks.editCopy { remove(location) }
                    if (snake.blocks.isEmpty()) {
                        snakes = snakes.editCopy { remove(snake) }
                    }
                    if (snake.phase == Phase.SPAWNING) {
                        snake.phase = Phase.MOVING
                    }
                    snake.lastRemoveTime = SimpleTimeMark.now()
                }
            }
        }
    }

    private fun Duration.getType(): DiffType {
        val ms = inWholeMilliseconds
        return when {
            ms < 50 -> DiffType.SMALL
            ms in 100..200 -> DiffType.BIG

            else -> DiffType.UNKNWON
        }
    }

    enum class DiffType {
        BIG,
        SMALL,
        UNKNWON
    }

    private fun getClosest(): Snake? {
        if (snakes.isEmpty()) return null
        val snakeDistances = mutableMapOf<Snake, Double>()
        for (snake in snakes) {
            val d = snake.blocks.minOfOrNull { it.distanceSqToPlayer() } ?: Double.MAX_VALUE
            snakeDistances[snake] = d
        }
        return snakeDistances.sorted().keys.first()
    }

    @HandleEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!isEnabled()) return
        getClosest()?.let {
            if (event.position in it.blocks) {
                selectedSnake = it
            }
        }
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        snakes = snakes.filter { !it.invalidSize() }
        for (snake in snakes) {
            if (snake.phase == Phase.SPAWNING) continue

            snake.phase = if (snake.isBelowBlocks()) {
                Phase.NOT_TOUCHING_AIR
            } else {
                val paused = snake.lastAddTime.passedSince() > 200.milliseconds
                if (paused) Phase.PAUSED else Phase.MOVING
            }
        }
    }

    private fun Snake.invalidSize(): Boolean = blocks.zipWithNext().any { (a, b) ->
        a.distance(b) > 3
    }

    private fun Snake.isBelowBlocks(): Boolean = blocks.any { it.isBelowBlocks() }

    private val directions = setOf(
        LorenzVec(1, 0, 0),
        LorenzVec(-1, 0, 0),
        LorenzVec(0, 1, 0),
        LorenzVec(0, -1, 0),
        LorenzVec(0, 0, 1),
        LorenzVec(0, 0, -1),
    )

    private fun LorenzVec.isBelowBlocks(): Boolean = directions.none { plus(it).getBlockAt() == Blocks.air }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        val seeThroughBlocks = true

        for (snake in snakes) {
            val blocks = snake.blocks
            if (blocks.isEmpty()) continue
            val color = snake.phase.color.toColor()
            if (blocks.size > 1 && snake.phase == Phase.PAUSED) {
                blocks.first().let {
                    event.drawWaypointFilled(it, LorenzColor.GREEN.toColor(), seeThroughBlocks)
                    event.drawString(it.add(0.5, 0.5, 0.5), "§aTail", seeThroughBlocks)
                }
                if (blocks.size > 2 && snake == selectedSnake) {
                    blocks[1].let {
                        event.drawWaypointFilled(it, LorenzColor.GOLD.toColor(), seeThroughBlocks)
                        event.drawString(it.add(0.5, 0.5, 0.5), "§aNext", seeThroughBlocks)
                    }
                }
            }
            blocks.last().let {
                event.drawWaypointFilled(it, color, seeThroughBlocks)
                val headColor = if (snake.phase == Phase.NOT_TOUCHING_AIR) "§c" else "§e"
                event.drawString(it.add(0.5, 0.5, 0.5), "${headColor}Head", seeThroughBlocks)
            }
            for ((a, b) in blocks.zipWithNext()) {
                event.draw3DLine(a.add(0.5, 0.5, 0.5), b.add(0.5, 0.5, 0.5), color, 3, !seeThroughBlocks)
            }
        }
    }

    fun isEnabled() = RiftApi.inRift() && (RiftApi.inLivingCave() || RiftApi.inLivingStillness()) && config.enabled
}
