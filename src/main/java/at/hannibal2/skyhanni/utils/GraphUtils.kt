package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.data.model.Graph
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.model.findAllShortestDistances
import at.hannibal2.skyhanni.data.model.findDijkstraDistances
import at.hannibal2.skyhanni.data.model.findPathToDestination
import java.util.Stack

object GraphUtils {
    /**
     * Find the fastest path from [closestNode] to *any* node that matches [condition].
     */
    fun findFastestPath(
        graph: Graph,
        closestNode: GraphNode,
        condition: (GraphNode) -> Boolean,
    ): Pair<Graph, Double>? {
        val distances = graph.findDijkstraDistances(closestNode, condition)
        val entry = distances.lastVisitedNode.takeIf(condition)
        return entry?.let {
            distances.findPathToDestination(it)
        }
    }

    /**
     * Find the fastest path from [closestNode] to *all* nodes that matches [condition].
     */
    fun findFastestPaths(
        graph: Graph,
        closestNode: GraphNode,
        condition: (GraphNode) -> Boolean = { true },
    ): Pair<MutableMap<GraphNode, Graph>, MutableMap<GraphNode, Double>> {
        val paths = mutableMapOf<GraphNode, Graph>()

        val map = mutableMapOf<GraphNode, Double>()
        val distances = graph.findAllShortestDistances(closestNode)
        for (graphNode in graph.nodes) {
            if (!condition(graphNode)) continue
            val (path, distance) = distances.findPathToDestination(graphNode)
            paths[graphNode] = path
            map[graphNode] = distance
        }
        return Pair(paths, map)
    }

    /**
     * Find all maximal sub graphs of the given graph which are not connected
     */
    fun findDisjointClusters(graph: Graph): List<Set<GraphNode>> {
        val universe = graph.toMutableSet()
        val allClusters = mutableListOf<Set<GraphNode>>()
        while (universe.isNotEmpty()) {
            val cluster = mutableSetOf<GraphNode>()
            allClusters.add(cluster)
            val queue = Stack<GraphNode>()
            queue.add(universe.first())
            while (queue.isNotEmpty()) {
                val next = queue.pop()
                universe.remove(next)
                cluster.add(next)
                queue.addAll(next.neighbours.keys)
                queue.retainAll(universe)
            }
        }
        return allClusters
    }
}
