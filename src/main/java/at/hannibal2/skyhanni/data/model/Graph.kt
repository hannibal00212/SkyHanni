package at.hannibal2.skyhanni.data.model

import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.json.SkyHanniTypeAdapters.registerTypeAdapter
import at.hannibal2.skyhanni.utils.json.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.stream.JsonToken
import java.util.PriorityQueue

@JvmInline
value class Graph(
    @Expose val graph: List<GraphNode>,
) : List<GraphNode> {
    override val size
        get() = graph.size

    override fun contains(element: GraphNode) = graph.contains(element)

    override fun containsAll(elements: Collection<GraphNode>) = graph.containsAll(elements)

    override fun get(index: Int) = graph.get(index)

    override fun isEmpty() = graph.isEmpty()

    override fun indexOf(element: GraphNode) = graph.indexOf(element)

    override fun iterator(): Iterator<GraphNode> = graph.iterator()
    override fun listIterator() = graph.listIterator()

    override fun listIterator(index: Int) = graph.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = graph.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: GraphNode) = graph.lastIndexOf(element)

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

                    it.tagNames?.takeIf { it.isNotEmpty() }?.let {
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
                        out.name(id).value(weight.round(2))
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
                    var tags: List<String>? = null
                    var neighbors = mutableListOf<Pair<Int, Double>>()
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
class GraphNode(val id: Int, val position: LorenzVec, val name: String? = null, val tagNames: List<String>? = null) {

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

fun Graph.findShortestPathAsGraphWithDistance(start: GraphNode, end: GraphNode): Pair<Graph, Double> {
    val distances = mutableMapOf<GraphNode, Double>()
    val previous = mutableMapOf<GraphNode, GraphNode>()
    val visited = mutableSetOf<GraphNode>()
    val queue = PriorityQueue<GraphNode>(compareBy { distances.getOrDefault(it, Double.MAX_VALUE) })

    distances[start] = 0.0
    queue.add(start)

    while (queue.isNotEmpty()) {
        val current = queue.poll()
        if (current == end) break

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

    return Graph(
        buildList {
            var current = end
            while (current != start) {
                add(current)
                current = previous[current] ?: return Graph(emptyList()) to 0.0
            }
            add(start)
        }.reversed(),
    ) to distances[end]!!
}

fun Graph.findShortestPath(start: GraphNode, end: GraphNode): List<LorenzVec> = this.findShortestPathAsGraph(start, end).toPositionsList()

fun Graph.findShortestDistance(start: GraphNode, end: GraphNode): Double = this.findShortestPathAsGraphWithDistance(start, end).second

fun Graph.toPositionsList() = this.map { it.position }

fun Graph.toJson(): String = Graph.gson.toJson(this)
