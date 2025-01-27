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
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
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
    private val edges = LocationUtils.generateCubeEdges(0.005)

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
        val head get() = blocks.first()
        val tail get() = blocks.last()

        fun invalidSize(): Boolean = blocks.isEmpty() || blocks.zipWithNext().any { (a, b) ->
            a.distance(b) > 3
        }

        fun invalidHeadRightNow(): Boolean = head.getBlockAt() != Blocks.lapis_block

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

    enum class State(val color: LorenzColor, label: String) {
        SPAWNING(LorenzColor.AQUA, "Spawning"),
        ACTIVE(LorenzColor.YELLOW, "Active"),
        NOT_TOUCHING_AIR(LorenzColor.RED, "Not touching air"),
        CALM(LorenzColor.GREEN, "Calm"),
        ;

        val display = "${color.getChatColor()}$label"
    }

    private val originalBlocks = mutableMapOf<LorenzVec, Block>()

    private var selectedSnake: Snake? = null

    @HandleEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!isEnabled()) return
        val location = event.location
        val old = event.oldState.block
        val new = event.newState.block

        if (LorenzUtils.debug && Minecraft.getMinecraft().thePlayer.isSneaking && snakes.isNotEmpty()) {
            snakes = emptyList()
            ChatUtils.debug("Snakes reset.", replaceSameMessage = true)
        }


        if (new == Blocks.lapis_block) {
            val snake = fixCollisions(findNearbySnakeHeads(location))
            if (snake != null) {
                // hypixel is sometimes funny
                if (location in snake.blocks) return

                snake.blocks = snake.blocks.editCopy { add(0, location) }
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
        snakes.filter { it.blocks.isNotEmpty() && it.head.distance(location) < 1.74 }
            .sortedBy { it.head.distance(location) }

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

        snakes = snakes.filter {
            val invalidSize = it.invalidSize()
            val invalidHead = it.invalidHead()
            if (invalidSize && LorenzUtils.debug) ChatUtils.chat("invalidSize")
            if (invalidHead && LorenzUtils.debug) ChatUtils.chat("invalidHead")
            !invalidSize && !invalidHead
        }
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

        for (snake in snakes) {
            val isSelected = snake == selectedSnake
            val blocks = snake.blocks
            if (blocks.isEmpty()) continue
            val interaction = snake.getInteraction()
            if (LorenzUtils.debug) {
                event.drawString(snake.head.add(0.5, 0.8, 0.5), "§fstate = ${snake.state}", isSelected)
                event.drawString(snake.head.add(0.5, 1.1, 0.5), "§finteraction = $interaction", isSelected)
            }

            val size = blocks.size
            val color = snake.state.color.toColor()
            if (size > 1 && snake.state == State.CALM && interaction != Interaction.CALMING) {
                val tail = snake.tail
                val lastBrokenBlock = snake.lastBrokenBlock
                val location = if (interaction == Interaction.BREAKING && lastBrokenBlock != null) {
                    LocationUtils.slopeOverTime(snake.lastRemoveTime, 300.milliseconds, lastBrokenBlock, tail)
                } else tail

                event.drawColor(location, LorenzColor.GREEN.toColor(), alpha = 1f, seeThroughBlocks = isSelected)
                val label = snake.state.display
                if (isSelected) {
                    event.drawString(location.add(0.5, 0.5, 0.5), label, seeThroughBlocks = true)

                    if (interaction == Interaction.BREAKING) {
                        event.drawString(location.add(0.5, 0.2, 0.5), "§b$size blocks", seeThroughBlocks = true)
                    }
                }
            }
            if (interaction != Interaction.BREAKING || size == 1) {
                val head = snake.head
                val location = if (size > 1) {
                    LocationUtils.slopeOverTime(snake.lastAddTime, 200.milliseconds, snake.blocks[1], head)
                } else head
                event.drawColor(location, color, alpha = 1f, seeThroughBlocks = isSelected)

                val label = snake.state.display

                if (isSelected) {
                    event.drawString(location.add(0.5, 0.5, 0.5), label, seeThroughBlocks = true)
                    if (interaction == Interaction.CALMING) {
                        event.drawString(location.add(0.5, 0.2, 0.5), "§b$size blocks", seeThroughBlocks = true)
                    }
                }
            }
            for (block in blocks) {
                if (block == snake.head && snake.lastAddTime.passedSince() < 200.milliseconds) {
                    continue
                }
                for ((x, y) in edges) {
                    event.draw3DLine(block + x, block + y, color, 2, true)
                }
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
