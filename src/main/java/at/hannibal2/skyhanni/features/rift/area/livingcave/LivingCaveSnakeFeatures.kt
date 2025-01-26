package at.hannibal2.skyhanni.features.rift.area.livingcave

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object LivingCaveSnakeFeatures {
    private val config get() = RiftApi.config.area.livingCave.livingCaveLivingMetalConfig
    private var snakes = emptyList<Snake>()

    class Snake(
        var blocks: List<LorenzVec>,
        var lastRemoveTime: SimpleTimeMark = SimpleTimeMark.farPast(),
        var lastAddTime: SimpleTimeMark = SimpleTimeMark.farPast(),
        var state: State = State.SPAWNING,
        var lastCalmTime: SimpleTimeMark = SimpleTimeMark.farPast(),
        var lastHitTime: SimpleTimeMark = SimpleTimeMark.farPast(),
        var invalidHeadSince: SimpleTimeMark? = null,
        var lastBrokenBlock: LorenzVec? = null,
    ) {
        fun invalidSize(): Boolean = blocks.isEmpty() || blocks.zipWithNext().any { (a, b) ->
            a.distance(b) > 3
        }

        fun invalidHeadRightNow(): Boolean = blocks.last().getBlockAt() != Blocks.lapis_block

        fun invalidHead(): Boolean = invalidHeadSince?.let { it.passedSince() > 1.seconds } ?: false

        fun isNotTouchingAir(): Boolean = blocks.any { it.isNotTouchingAir() }

        fun getInteraction(): Interaction {
            if (this == selectedSnake) {
                if (lastHitTime.passedSince() < 2.seconds) {
                    return Interaction.BREAKING
                }
                if (lastCalmTime.passedSince() < 2.seconds) {
                    return Interaction.CALMING
                }
            }

            return Interaction.NONE
        }
    }

    enum class State(val color: LorenzColor) {
        SPAWNING(LorenzColor.AQUA),
        ACTIVE(LorenzColor.YELLOW),
        NOT_TOUCHING_AIR(LorenzColor.RED),
        CALM(LorenzColor.GREEN),
    }

    private val originalBlocks = mutableMapOf<LorenzVec, Block>()

    private var selectedSnake: Snake? = null

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


        if (new == Blocks.lapis_block) {
            val snake = fixCollisions(findNearbySnakeHeads(location))
            if (snake != null) {
                // hypixel is sometimes funny
                if (location in snake.blocks) return

                snake.blocks = snake.blocks.editCopy { add(location) }
                snake.lastAddTime = SimpleTimeMark.now()
                snake.invalidHeadSince = null
            } else {
                snakes = snakes.editCopy { add(Snake(listOf(location))) }
            }
            originalBlocks[location] = old
        }
        for (snake in snakes) {
            if (location !in snake.blocks) continue
            if (originalBlocks[location] != new) continue
            originalBlocks.remove(location)
            snake.blocks = snake.blocks.editCopy { remove(location) }
            if (snake.blocks.isEmpty()) {
                snakes = snakes.editCopy { remove(snake) }
            }
            if (snake.state == State.SPAWNING) {
                snake.state = State.ACTIVE
            }
            snake.lastRemoveTime = SimpleTimeMark.now()
            snake.lastBrokenBlock = location

            originalBlocks[location] = old
        }
    }

    // sqrt(3) =~ 1.73
    private fun findNearbySnakeHeads(location: LorenzVec): List<Snake> =
        snakes.filter { it.blocks.isNotEmpty() && it.blocks.last().distance(location) < 1.74 }
            .sortedBy { it.blocks.last().distance(location) }

    private fun fixCollisions(found: List<Snake>): Snake? = if (found.size > 1) {
        val filtered = found.filter { it.state != State.CALM }
        if (filtered.size < found.size && filtered.isNotEmpty()) {
            filtered.firstOrNull()
        } else {
            found.firstOrNull()
        }
    } else {
        found.firstOrNull()
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

    // TODO maybe move this in repo
    private val pickaxes = setOf(
        "SELF_RECURSIVE_PICKAXE",
        "ANTI_SENTIENT_PICKAXE",
        "EON_PICKAXE",
        "CHRONO_PICKAXE",
    ).map { it.toInternalName() }

    @HandleEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!isEnabled()) return
        val snake = getClosest() ?: return
        if (event.position !in snake.blocks) return

        selectedSnake = snake
        if (event.clickType == ClickType.RIGHT_CLICK) {
            if (InventoryUtils.itemInHandId == "FROZEN_WATER_PUNGI".toInternalName())
                snake.lastCalmTime = SimpleTimeMark.now()
        } else {
            if (InventoryUtils.itemInHandId in pickaxes) {
                snake.lastHitTime = SimpleTimeMark.now()
            }
        }
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        snakes = snakes.filter { !it.invalidSize() && !it.invalidHead() }
        for (snake in snakes) {
            if (snake.invalidHeadRightNow()) {
                if (snake.invalidHeadSince == null) {
                    snake.invalidHeadSince = SimpleTimeMark.now()
                }
            } else {
                snake.invalidHeadSince = null
            }
            if (snake.state == State.SPAWNING) continue

            snake.state = if (snake.isNotTouchingAir()) {
                State.NOT_TOUCHING_AIR
            } else {
                val notMoving = snake.lastAddTime.passedSince() > 200.milliseconds
                if (notMoving) State.CALM else State.ACTIVE
            }
        }
    }

    private val directions = setOf(
        LorenzVec(1, 0, 0),
        LorenzVec(-1, 0, 0),
        LorenzVec(0, 1, 0),
        LorenzVec(0, -1, 0),
        LorenzVec(0, 0, 1),
        LorenzVec(0, 0, -1),
    )

    private fun LorenzVec.isNotTouchingAir(): Boolean = directions.none { plus(it).getBlockAt() == Blocks.air }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        val seeThroughBlocks = true

        for (snake in snakes) {
            val blocks = snake.blocks
            if (blocks.isEmpty()) continue
            val interaction = snake.getInteraction()
            event.drawString(blocks.last().add(0.5, 0.8, 0.5), "§fstate = ${snake.state}", seeThroughBlocks)
            event.drawString(blocks.last().add(0.5, 1.1, 0.5), "§finteraction = $interaction", seeThroughBlocks)

            val remainingSize = blocks.size
            val color = snake.state.color.toColor()


            if (blocks.size > 1 && snake.state == State.CALM && interaction != Interaction.CALMING) {
                blocks.first().let {
                    val lastBrokenBlock = snake.lastBrokenBlock
                    val location = if (interaction == Interaction.BREAKING && lastBrokenBlock != null) {
                        LocationUtils.slopeOverTime(snake.lastRemoveTime, 300.milliseconds, lastBrokenBlock, it)
                    } else {
                        it
                    }

                    event.drawWaypointFilled(location, LorenzColor.GREEN.toColor(), seeThroughBlocks)
                    event.drawString(location.add(0.5, 0.5, 0.5), "§aTail", seeThroughBlocks)

                    if (interaction == Interaction.BREAKING) {
                        event.drawString(location.add(0.5, 0.2, 0.5), "§7($remainingSize blocks)", seeThroughBlocks)
                    }
                }
            }
            if (interaction != Interaction.BREAKING || blocks.size == 1) {
                blocks.last().let {
                    event.drawWaypointFilled(it, color, seeThroughBlocks)
                    val headColor = if (snake.state == State.NOT_TOUCHING_AIR) "§c" else "§e"
                    event.drawString(it.add(0.5, 0.5, 0.5), "${headColor}Head", seeThroughBlocks)
                    if (interaction == Interaction.CALMING) {
                        event.drawString(it.add(0.5, 0.2, 0.5), "§7($remainingSize blocks)", seeThroughBlocks)
                    }
                }
            }
            for ((a, b) in blocks.zipWithNext()) {
                event.draw3DLine(a.add(0.5, 0.5, 0.5), b.add(0.5, 0.5, 0.5), color, 3, !seeThroughBlocks)
            }
        }
    }

    enum class Interaction {
        NONE,
        CALMING,
        BREAKING,
    }

    fun isEnabled() = RiftApi.inRift() && (RiftApi.inLivingCave() || RiftApi.inLivingStillness()) && config.enabled
}
