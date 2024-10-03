package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.data.model.DijkstraTree
import at.hannibal2.skyhanni.data.model.Graph
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.model.findPathToDestination
import java.util.PriorityQueue
import java.util.Stack

object GraphUtils {
    /**
     * Find the fastest path from [closestNode] to *any* node that matches [condition].
     */
    fun findFastestPath(
        closestNode: GraphNode,
        condition: (GraphNode) -> Boolean,
    ): Pair<Graph, Double>? {
        val distances = findDijkstraDistances(closestNode, condition)
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
        val distances = findAllShortestDistances(closestNode)
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

    fun findShortestPathAsGraph(start: GraphNode, end: GraphNode): Graph = findShortestPathAsGraphWithDistance(start, end).first

    /**
     * Find a tree of distances to the [start] node using dijkstra's algorithm.
     */
    fun findDijkstraDistances(
        start: GraphNode,
        /**
         * Bail out early before collecting all the distances to all nodes in the graph. This will not collect valid distance data for *all*
         * nodes for which bailout matches, but only the closest one.
         */
        bailout: (GraphNode) -> Boolean,
    ): DijkstraTree {
        val distances = mutableMapOf<GraphNode, Double>()
        val previous = mutableMapOf<GraphNode, GraphNode>()
        val visited = mutableSetOf<GraphNode>()
        val queue = PriorityQueue<GraphNode>(compareBy { distances.getOrDefault(it, Double.MAX_VALUE) })
        var lastVisitedNode: GraphNode = start

        distances[start] = 0.0
        queue.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            lastVisitedNode = current
            if (bailout(current)) break

            visited.add(current)

            current.neighbours.forEach { (neighbour, weight) ->
                if (neighbour !in visited) {
                    val newDistance = distances.getValue(current) + weight
                    if (newDistance < distances.getOrDefault(neighbour, Double.MAX_VALUE)) {
                        distances[neighbour] = newDistance
                        previous[neighbour] = current
                        queue.add(neighbour)
                    }
                }
            }
        }

        return DijkstraTree(
            start,
            distances,
            previous,
            lastVisitedNode,
        )
    }

    fun findAllShortestDistances(start: GraphNode): DijkstraTree {
        return findDijkstraDistances(start) { false }
    }

    fun findShortestPathAsGraphWithDistance(start: GraphNode, end: GraphNode): Pair<Graph, Double> {
        val distances = findDijkstraDistances(start) { it == end }
        return distances.findPathToDestination(end)
    }

    fun findShortestPath(start: GraphNode, end: GraphNode): List<LorenzVec> = findShortestPathAsGraph(start, end).toPositionsList()

    fun findShortestDistance(start: GraphNode, end: GraphNode): Double = findShortestPathAsGraphWithDistance(start, end).second
}
