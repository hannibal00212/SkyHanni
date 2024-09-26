package at.hannibal2.skyhanni.data.model

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapters.registerTypeAdapter
import at.hannibal2.skyhanni.utils.json.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.stream.JsonToken
import java.util.PriorityQueue

// TODO: This class should be disambiguated into a NodePath and a Graph class
@JvmInline
value class Graph(
    @Expose val nodes: List<GraphNode>,
) : List<GraphNode> {
    override val size
        get() = nodes.size

    override fun contains(element: GraphNode) = nodes.contains(element)

    override fun containsAll(elements: Collection<GraphNode>) = nodes.containsAll(elements)

    override fun get(index: Int) = nodes.get(index)

    override fun isEmpty() = nodes.isEmpty()

    override fun indexOf(element: GraphNode) = nodes.indexOf(element)

    override fun iterator(): Iterator<GraphNode> = nodes.iterator()
    override fun listIterator() = nodes.listIterator()

    override fun listIterator(index: Int) = nodes.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = nodes.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: GraphNode) = nodes.lastIndexOf(element)

    companion object {
        val gson = GsonBuilder().setPrettyPrinting().registerTypeAdapter<Graph>(
            { out, value ->
                out.beginObject()
                value.forEach {
                    out.name(it.id.toString()).beginObject()

                    out.name("Position").value(with(it.position) { "$x:$y:$z" })

                    it.name?.let {
                        out.name("Name").value(it)
                    }

                    it.tagNames.takeIf { list -> list.isNotEmpty() }?.let {
                        out.name("Tags")
                        out.beginArray()
                        for (tagName in it) {
                            out.value(tagName)
                        }
                        out.endArray()
                    }

                    out.name("Neighbours")
                    out.beginObject()
                    for ((node, weight) in it.neighbours) {
                        val id = node.id.toString()
                        out.name(id).value(weight.roundTo(2))
                    }
                    out.endObject()

                    out.endObject()
                }
                out.endObject()
            },
            { reader ->
                reader.beginObject()
                val list = mutableListOf<GraphNode>()
                val neighbourMap = mutableMapOf<GraphNode, List<Pair<Int, Double>>>()
                while (reader.hasNext()) {
                    val id = reader.nextName().toInt()
                    reader.beginObject()
                    var position: LorenzVec? = null
                    var name: String? = null
                    var tags = emptyList<String>()
                    val neighbors = mutableListOf<Pair<Int, Double>>()
                    while (reader.hasNext()) {
                        if (reader.peek() != JsonToken.NAME) {
                            reader.skipValue()
                            continue
                        }
                        when (reader.nextName()) {
                            "Position" -> {
                                position = reader.nextString().split(":").let { parts ->
                                    LorenzVec(parts[0].toDouble(), parts[1].toDouble(), parts[2].toDouble())
                                }
                            }

                            "Neighbours" -> {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    val nId = reader.nextName().toInt()
                                    val distance = reader.nextDouble()
                                    neighbors.add(nId to distance)
                                }
                                reader.endObject()
                            }

                            "Name" -> {
                                name = reader.nextString()
                            }

                            "Tags" -> {
                                tags = mutableListOf()
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    val tagName = reader.nextString()
                                    tags.add(tagName)
                                }
                                reader.endArray()
                            }

                        }
                    }
                    val node = GraphNode(id, position!!, name, tags)
                    list.add(node)
                    neighbourMap[node] = neighbors
                    reader.endObject()
                }
                neighbourMap.forEach { (node, edge) ->
                    node.neighbours = edge.associate { (id, distance) ->
                        list.first { it.id == id } to distance
                    }
                }
                reader.endObject()
                Graph(list)
            },
        ).create()

        fun fromJson(json: String): Graph = gson.fromJson<Graph>(json)
        fun fromJson(json: JsonElement): Graph = gson.fromJson<Graph>(json)
    }
}

// The node object that gets parsed from/to json
class GraphNode(val id: Int, val position: LorenzVec, val name: String? = null, val tagNames: List<String> = emptyList()) {

    val tags: List<GraphNodeTag> by lazy {
        tagNames.mapNotNull { GraphNodeTag.byId(it) }
    }

    /** Keys are the neighbours and value the edge weight (e.g. Distance) */
    lateinit var neighbours: Map<GraphNode, Double>

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphNode

        if (id != other.id) return false

        return true
    }
}

fun Graph.findShortestPathAsGraph(start: GraphNode, end: GraphNode): Graph = this.findShortestPathAsGraphWithDistance(start, end).first

data class DijkstraTree(
    val origin: GraphNode,
    /**
     * A map of distances between the [origin] and each node in a graph. This distance map is only accurate for nodes closer to the
     * origin than the [lastVisitedNode]. In case there is no early bailout, this map will be accurate for all nodes in the graph.
     */
    val distances: Map<GraphNode, Double>,
    /**
     * A map of nodes to the neighbouring node that is the quickest path towards the origin (the neighbouring node that has the lowest value
     * in [distances])
     */
    val towardsOrigin: Map<GraphNode, GraphNode>,
    /**
     * This is either the furthest away node in the graph, or the node that was bailed out on early because it fulfilled the search
     * condition. In case the search condition matches nothing, this will *still* be the furthest away node, so an additional check might be
     * necessary.
     */
    val lastVisitedNode: GraphNode,
)

/**
 * Find a tree of distances to the [start] node using dijkstra's algorithm.
 */
fun Graph.findDijkstraDistances(
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

fun Graph.findAllShortestDistances(start: GraphNode): DijkstraTree {
    return findDijkstraDistances(start) { false }
}

fun DijkstraTree.findPathToDestination(end: GraphNode): Pair<Graph, Double> {
    val distances = this
    val reversePath = buildList {
        var current = end
        while (true) {
            add(current)
            if (current == distances.origin) break
            current = distances.towardsOrigin[current] ?: return Graph(emptyList()) to 0.0
        }
    }
    return Graph(reversePath.reversed()) to distances.distances[end]!!
}

fun Graph.findShortestPathAsGraphWithDistance(start: GraphNode, end: GraphNode): Pair<Graph, Double> {
    val distances = findDijkstraDistances(start) { it == end }
    return distances.findPathToDestination(end)
}

fun Graph.findShortestPath(start: GraphNode, end: GraphNode): List<LorenzVec> = this.findShortestPathAsGraph(start, end).toPositionsList()

fun Graph.findShortestDistance(start: GraphNode, end: GraphNode): Double = this.findShortestPathAsGraphWithDistance(start, end).second

fun Graph.toPositionsList() = this.map { it.position }

fun Graph.toJson(): String = Graph.gson.toJson(this)
