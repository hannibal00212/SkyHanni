package at.hannibal2.skyhanni.features.misc.pathfind

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandGraphs.pathFind
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.model.GraphNodeTag
import at.hannibal2.skyhanni.features.misc.IslandAreas
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.GraphUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.chat.Text.onClick
import at.hannibal2.skyhanni.utils.chat.Text.send
import kotlinx.coroutines.launch

object NavigationHelper {
    private const val NAVIGATION_CHAT_ID = -6457562

    val allowedTags = listOf(
        GraphNodeTag.NPC,
        GraphNodeTag.AREA,
        GraphNodeTag.SMALL_AREA,
        GraphNodeTag.POI,
        GraphNodeTag.SLAYER,
        GraphNodeTag.GRIND_MOBS,
        GraphNodeTag.GRIND_ORES,
        GraphNodeTag.GRIND_CROPS,
        GraphNodeTag.MINES_EMISSARY,
        GraphNodeTag.CRIMSON_MINIBOSS,
    )

    fun onCommand(args: Array<String>) {
        SkyHanniMod.coroutineScope.launch {
            doCommandAsync(args)
        }
    }

    private fun doCommandAsync(args: Array<String>) {
        val searchTerm = args.joinToString(" ").lowercase()
        val distances = calculateDistances(searchTerm)
        val locations = calculateNames(distances)

        val goBack = {
            onCommand(searchTerm.split(" ").toTypedArray())
            IslandGraphs.stop()
        }
        val title = if (searchTerm.isBlank()) "SkyHanni Navigation Locations" else "SkyHanni Navigation Locations Matching: \"$searchTerm\""

        Text.displayPaginatedList(
            title,
            locations,
            chatLineId = NAVIGATION_CHAT_ID,
            emptyMessage = "No locations found.",
        ) { (name, node) ->
            val distance = distances[node]!!.roundTo(1)
            val component = "$name §e$distance".asComponent()
            component.onClick {
                node.pathFind(label = name, allowRerouting = true, condition = { true })
                sendNavigateMessage(name, goBack)
            }
            val tag = node.tags.first { it in allowedTags }
            val hoverText = "Name: $name\n§7Type: §r${tag.displayName}\n§7Distance: §e$distance blocks\n§eClick to start navigating!"
            component.hover = hoverText.asComponent()
            component
        }
    }

    private fun sendNavigateMessage(name: String, goBack: () -> Unit) {
        val componentText = "§7Navigating to §r$name".asComponent()
        componentText.onClick(onClick = goBack)
        componentText.hover = "§eClick to stop navigating and return to previous search".asComponent()
        componentText.send(NAVIGATION_CHAT_ID)
    }

    private fun calculateNames(distances: Map<GraphNode, Double>): List<Pair<String, GraphNode>> {
        val names = mutableMapOf<String, GraphNode>()
        for (node in distances.sorted().keys) {
            // hiding areas that are none
            if (node.name == "no_area") continue
            // no need to navigate to the current area
            if (node.name == IslandAreas.currentAreaName) continue
            val tag = node.tags.first { it in allowedTags }
            val name = "${node.name} §7(${tag.displayName}§7)"
            if (name in names) continue
            names[name] = node
        }
        return names.toList()
    }

    private fun calculateDistances(
        searchTerm: String,
    ): Map<GraphNode, Double> {
        val graph = IslandGraphs.currentIslandGraph ?: return emptyMap()
        val closestNode = IslandGraphs.closestNode ?: return emptyMap()

        val nodes = graph.nodes
        val distances = mutableMapOf<GraphNode, Double>()
        for (node in nodes) {
            val name = node.name ?: continue
            val remainingTags = node.tags.filter { it in allowedTags }
            if (remainingTags.isEmpty()) continue
            if (name.lowercase().contains(searchTerm)) {
                distances[node] = GraphUtils.findShortestDistance(closestNode, node)
            }
            if (remainingTags.size != 1) {
                println("found node with invalid amount of tags: ${node.name} (${remainingTags.map { it.cleanName }}")
            }
        }
        return distances
    }

}
