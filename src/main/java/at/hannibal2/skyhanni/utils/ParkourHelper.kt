package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.jsonobjects.repo.ParkourShortCut
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.toSingletonListOrEmpty
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine_nea
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox_nea
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.expandBlock
import at.hannibal2.skyhanni.utils.RenderUtils.outlineTopFace
import net.minecraft.client.Minecraft
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

class ParkourHelper(
    val locations: List<LorenzVec>,
    private val shortCuts: List<ParkourShortCut>,
    val platformSize: Double = 1.0,
    val detectionRange: Double = 1.0,
    val depth: Boolean = true,
    val onEndReach: () -> Unit = {},
    val goInOrder: Boolean = false,
) {

    private var current = -1
    private var visible = false

    var rainbowColor = false
    var monochromeColor: Color = Color.WHITE
    var lookAhead = 2
    var outline = false
    var showEverything = false

    fun inParkour() = current != -1

    fun reset() {
        current = -1
        visible = false
    }

    fun render(event: LorenzRenderWorldEvent) {
        if (locations.isEmpty()) {
            ErrorManager.logErrorWithData(
                IllegalArgumentException("locations is empty"),
                "Trying to render an empty parkour"
            )
            return
        }

        try {
            if (!showEverything) {
                if (current == locations.size - 1) visible = false

                if (visible) {
                    for ((index, location) in locations.withIndex()) {
                        val onGround = Minecraft.getMinecraft().thePlayer.onGround
                        val closeEnough = location.offsetCenter().distanceToPlayer() < detectionRange
                        if (!(closeEnough && onGround)) continue
                        if (goInOrder && (index < current - 1 || index > current + 1)) continue
                        current = index
                    }
                }

                val distanceToPlayer = locations.first().offsetCenter().distanceToPlayer()
                if (distanceToPlayer < detectionRange) {
                    visible = true
                } else if (distanceToPlayer > 15 && current < 1) {
                    visible = false
                }

                if (!visible) return

                if (current < 0) return
            } else {
                current = 0
                lookAhead = locations.size
            }

            val inProgressVec = getInProgressPair().toSingletonListOrEmpty()
            if (locations.size == current + 1) {
                onEndReach()
            }
            for ((prev, next) in locations.asSequence().withIndex().zipWithNext().drop(current)
                .take(lookAhead - 1) + inProgressVec) {
                event.draw3DLine_nea(
                    prev.value.offsetCenter(),
                    next.value.offsetCenter(),
                    colorForIndex(prev.index),
                    5,
                    false
                )
            }
            val nextShortcuts = current until current + lookAhead
            for (shortCut in shortCuts) {
                if (shortCut.from in nextShortcuts && shortCut.to in locations.indices) {
                    val from = locations[shortCut.from].offsetCenter()
                    val to = locations[shortCut.to].offsetCenter()
                    event.draw3DLine_nea(from, to, Color.RED, 3, false)
                    val textLocation = from + (to - from).normalize()
                    event.drawDynamicText(textLocation.add(-0.5, 1.0, -0.5), "§cShortcut", 1.8)

                    val aabb = axisAlignedBB(locations[shortCut.to])
                    event.drawFilledBoundingBox_nea(aabb, Color.RED, 1f)
                    if (outline) event.outlineTopFace(aabb, 2, Color.BLACK, depth)
                }
            }

            for ((index, location) in locations.asSequence().withIndex().drop(current)
                .take(lookAhead) + inProgressVec.map { it.second }) {
                val isMovingPlatform = location !in locations
                if (isMovingPlatform && showEverything) continue
                if (isMovingPlatform) {
                    val aabb = axisAlignedBB(location).expandBlock()
                    event.drawFilledBoundingBox_nea(aabb, colorForIndex(index), .6f)
                } else {
                    val aabb = axisAlignedBB(location)
                    event.drawFilledBoundingBox_nea(aabb, colorForIndex(index), 1f)
                    if (outline) event.outlineTopFace(aabb, 2, Color.BLACK, depth)
                }
                if (SkyHanniMod.feature.dev.waypoint.showPlatformNumber && !isMovingPlatform) {
                    event.drawString(location.offsetCenter().add(y = 1), "§a§l$index", seeThroughBlocks = true)
                }
            }
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(e, "Error while rendering a parkour")
        }
    }

    private fun LorenzVec.offsetCenter() = add(platformSize / 2, 1.0, platformSize / 2)

    private fun getInProgressPair(): Pair<IndexedValue<LorenzVec>, IndexedValue<LorenzVec>>? {
        if (current < 0 || current + lookAhead >= locations.size) return null
        val currentPosition = locations[current].offsetCenter()
        val nextPosition = locations[current + 1].offsetCenter()
        val lookAheadStart = locations[current + lookAhead - 1]
        val lookAheadEnd = locations[current + lookAhead]

        if (LocationUtils.playerLocation().distance(nextPosition) > currentPosition.distance(nextPosition)) return null

        val factor = LocationUtils.playerLocation().distance(currentPosition) / currentPosition.distance(nextPosition)
        val slopeLocation = lookAheadStart.slope(lookAheadEnd, factor)
        return Pair(
            IndexedValue(current + lookAhead - 1, lookAheadStart),
            IndexedValue(current + lookAhead, slopeLocation)
        )
    }

    private fun axisAlignedBB(loc: LorenzVec) = loc.boundingToOffset(platformSize, 1.0, platformSize).expandBlock()

    private fun colorForIndex(index: Int) = if (rainbowColor) {
        RenderUtils.chromaColor(4.seconds, offset = -index / 12f, brightness = 0.7f)
    } else monochromeColor
}
