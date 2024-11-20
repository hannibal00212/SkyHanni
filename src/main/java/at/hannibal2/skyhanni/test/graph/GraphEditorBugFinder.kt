package at.hannibal2.skyhanni.test.graph

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandGraphs.pathFind
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.features.misc.IslandAreas.getAreaTag
import at.hannibal2.skyhanni.features.misc.pathfind.NavigationHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.graph.GraphEditor.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.GraphUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

// Trying to find errors in Area Graph for the current graph editor instance
@SkyHanniModule
object GraphEditorBugFinder {
    private var errorsInWorld = emptyMap<GraphNode, String>()

    fun runTests() {
        SkyHanniMod.coroutineScope.launch {
            asyncTest()
        }
    }

    private fun asyncTest() {
        val graph = IslandGraphs.currentIslandGraph ?: return
        val errorsInWorld: MutableMap<GraphNode, String> = mutableMapOf()
        val nodes = graph.nodes

        for (node in nodes) {
            if (node.tags.any { it in NavigationHelper.allowedTags }) {
                val remainingTags = node.tags.filter { it in NavigationHelper.allowedTags }
                if (remainingTags.size != 1) {
                    errorsInWorld[node] = "§cConflicting tags: $remainingTags"
                }
            }
        }


        val nearestArea = mutableMapOf<GraphNode, GraphNode>()
        for (node in nodes) {
            val pathToNearestArea = GraphUtils.findFastestPath(node) { it.getAreaTag() != null }?.first
            if (pathToNearestArea == null) {
                continue
            }
            val areaNode = pathToNearestArea.lastOrNull() ?: error("Empty path to nearest area")
            nearestArea[node] = areaNode
        }
        var bugs = 0
        for (node in nodes) {
            val areaNode = nearestArea[node]?.name ?: continue
            for (neighbour in node.neighbours.keys) {
                val neighbouringAreaNode = nearestArea[neighbour]?.name ?: continue
                if (neighbouringAreaNode == areaNode) continue
                if ((null == node.getAreaTag())) {
                    bugs++
                    errorsInWorld[node] = "§cConflicting areas $areaNode and $neighbouringAreaNode"
                }
            }
        }
        for (node in nodes) {
            val nameNull = node.name.isNullOrBlank()
            val tagsEmpty = node.tags.isEmpty()
            if (nameNull > tagsEmpty) {
                errorsInWorld[node] = "§cMissing name despite having tags"
                bugs++
            }
            if (tagsEmpty > nameNull) {
                errorsInWorld[node] = "§cMissing tags despite having name"
                bugs++
            }
        }

        val clusters = GraphUtils.findDisjointClusters(graph)
        if (clusters.size > 1) {
            val closestCluster = clusters.minBy { it.minOf { it.position.distanceSqToPlayer() } }
            val foreignClusters = clusters.filter { it !== closestCluster }
            val closestForeignNodes = foreignClusters.map { network -> network.minBy { it.position.distanceSqToPlayer() } }
            closestForeignNodes.forEach {
                errorsInWorld[it] = "§cDisjoint node network"
                bugs++
            }
            val closestForeignNode = closestForeignNodes.minBy { it.position.distanceSqToPlayer() }
            val closestNodeToForeignNode = closestCluster.minBy { it.position.distanceSq(closestForeignNode.position) }
            closestNodeToForeignNode.pathFind("Graph Editor Bug", Color.RED, condition = { isEnabled() })
        }

        println("found $bugs bugs!")
        this.errorsInWorld = errorsInWorld
        if (clusters.size <= 1) {
            errorsInWorld.keys.minByOrNull {
                it.position.distanceSqToPlayer()
            }?.pathFind("Graph Editor Bug", Color.RED, condition = { isEnabled() })
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        for ((node, text) in errorsInWorld) {
            event.drawDynamicText(node.position, text, 1.5)
        }
    }

    fun isEnabled() = GraphEditor.isEnabled()
}
