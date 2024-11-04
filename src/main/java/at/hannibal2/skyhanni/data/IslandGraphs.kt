package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.model.Graph
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.repo.RepoUtils
import at.hannibal2.skyhanni.events.EntityMoveEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.skyblock.ScoreboardAreaChangeEvent
import at.hannibal2.skyhanni.features.misc.IslandAreas
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.GraphUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DPathWithWaypoint
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.chat.Text.onClick
import at.hannibal2.skyhanni.utils.chat.Text.send
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

/**
 * TODO
 * benefits of every island graphs:
 * global:
 * 	NEU's fairy souls
 * 	slayer area (not all there yet)
 * 	NEU's NPC's (auto acitvate when searching via neu)
 * 	races (end, park, winter, dungeon hub)
 * 	jump pads between servers
 * 	ring of love/romeo juliet quest
 * 	death location
 * 	% of island discvovered (every node was most closest node at least once)
 * hub:
 * 	12 starter NPC's
 * 	diana
 * farming:
 * 	pelt farming area
 * rift:
 * 	eyes
 * 	big quests
 * 	montezuma souls
 * 	blood effigies
 * 	avoid area around enderman
 * spider:
 * 	relicts + throw spot
 * dwarven mines:
 * 	emissary
 * 	commssion areas
 * 	events: raffle, goblin slayer, donpieresso
 * deep
 * 	path to the bottom (Rhys NPC)
 * end
 * 	golem spawn
 * 	dragon death spot
 * crimson
 *  vanquisher path
 *  area mini bosses
 *  daily quests
 *  intro tutorials with elle
 *  fishing spots
 * mineshaft
 *  different types mapped out
 *  paths to ladder and possible corpse locations, and known corpse locations
 *
 * Additional global things:
 *  use custom graphs for your island/garden
 *  suggest using warp points if closer
 *  support cross island paths (have a list of all node names in all islands)
 *
 * Changes in graph editor:
 * 	fix rename not using tick but input event we have (+ create the input event in the first place)
 * 	toggle distance to node by node path lengh, instead of eye of sight lenght
 * 	press test button again to enable "true test mode", with graph math and hiding other stuff
 * 	option to compare two graphs, and store multiple graphs in the edit mode in paralell
 */

@SkyHanniModule
object IslandGraphs {
    var currentIslandGraph: Graph? = null

    private var pathfindClosestNode: GraphNode? = null
    var closestNode: GraphNode? = null
    private var secondClosestNode: GraphNode? = null

    private var currentTarget: LorenzVec? = null
    private var currentTargetNode: GraphNode? = null
    private var label = ""
    private var lastDistance = 0.0
    private var totalDistance = 0.0
    private var color = Color.WHITE
    private var shouldAllowRerouting = false
    private var onFound: () -> Unit = {}
    private var goal: GraphNode? = null
        set(value) {
            prevGoal = field
            field = value
        }
    private var prevGoal: GraphNode? = null

    private var fastestPath: Graph? = null
    private var condition: () -> Boolean = { true }
    private var inGlaciteTunnels: Boolean? = null

    private val patternGroup = RepoPattern.group("data.island.navigation")

    /**
     * REGEX-TEST: Dwarven Base Camp
     * REGEX-TEST: Forge
     * REGEX-TEST: Fossil Research Center
     */
    private val glaciteTunnelsPattern by patternGroup.pattern(
        "glacitetunnels",
        "(Glacite Tunnels|Dwarven Base Camp|Great Glacite Lake|Fossil Research Center)",
    )

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        if (!LorenzUtils.inSkyBlock) return

        loadIsland(LorenzUtils.skyBlockIsland)
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (currentIslandGraph != null) return
        if (event.newIsland == IslandType.NONE) return
        loadIsland(event.newIsland)
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        currentIslandGraph = null
        if (currentTarget != null) {
            "§e[SkyHanni] Navigation stopped because of world switch!".asComponent().send(PATHFIND_ID)
        }
        reset()
    }

    fun isGlaciteTunnelsArea(area: String?): Boolean = glaciteTunnelsPattern.matches(area)

    @HandleEvent
    fun onAreaChange(event: ScoreboardAreaChangeEvent) {
        if (!IslandType.DWARVEN_MINES.isInIsland()) {
            inGlaciteTunnels = null
            return
        }

        val now = isGlaciteTunnelsArea(LorenzUtils.skyBlockArea)
        if (inGlaciteTunnels != now) {
            inGlaciteTunnels = now
            loadDwarvenMines()
        }
    }

    fun loadLobby(lobby: String) {
        reloadFromJson(lobby)
    }

    private fun loadDwarvenMines() {
        if (isGlaciteTunnelsArea(LorenzUtils.skyBlockArea)) {
            reloadFromJson("GLACITE_TUNNELS")
        } else {
            reloadFromJson("DWARVEN_MINES")
        }
    }

    private fun loadIsland(newIsland: IslandType) {
        if (newIsland == IslandType.DWARVEN_MINES) {
            loadDwarvenMines()
        } else {
            reloadFromJson(newIsland.name)
        }
    }

    private fun reloadFromJson(islandName: String) {
        val constant = "island_graphs/$islandName"
        val name = "constants/$constant.json"
        val jsonFile = File(SkyHanniMod.repo.repoLocation, name)
        if (!jsonFile.isFile) {
            currentIslandGraph = null
            return
        }

        val graph = RepoUtils.getConstant(SkyHanniMod.repo.repoLocation, constant, Graph.gson, Graph::class.java)
        IslandAreas.display = null
        setNewGraph(graph)
    }

    fun setNewGraph(graph: Graph) {
        reset()
        currentIslandGraph = graph

        // calling various update functions to make swtiching between deep caverns and glacite tunnels bareable
        handleTick()
        IslandAreas.nodeMoved()
        DelayedRun.runDelayed(150.milliseconds) {
            IslandAreas.updatePosition()
        }
    }

    private fun reset() {
        stop()
        pathfindClosestNode = null
        closestNode = null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (currentIslandGraph == null) return
        if (event.isMod(2)) {
            handleTick()
            checkMoved()
        }
    }

    private fun handleTick() {
        val prevClosest = pathfindClosestNode

        currentTarget?.let {
            if (it.distanceToPlayer() < 3) {
                onFound()
                "§e[SkyHanni] Navigation reached §r$label§e!".asComponent().send(PATHFIND_ID)
                reset()
            }
            if (!condition()) {
                reset()
            }
        }

        val graph = currentIslandGraph ?: return
        val sortedNodes = graph.sortedBy { it.position.distanceSqToPlayer() }
        val newClosest = sortedNodes.first()
        if (pathfindClosestNode == newClosest) return
        val newPath = !onCurrentPath()

        closestNode = newClosest
        secondClosestNode = sortedNodes.getOrNull(1)
        onNewNode()
        if (newClosest == prevClosest) return
        if (newPath) {
            pathfindClosestNode = closestNode
            findNewPath()
        }
    }

    private fun onCurrentPath(): Boolean {
        val path = fastestPath ?: return false
        if (path.isEmpty()) return false
        val closest = path.nodes.minBy { it.position.distanceSqToPlayer() }
        val distance = closest.position.distanceToPlayer()
        if (distance > 7) return false

        val index = path.nodes.indexOf(closest)
        val newNodes = path.drop(index)
        val newGraph = Graph(newNodes)
        fastestPath = skipIfCloser(newGraph)
        newNodes.getOrNull(1)?.let {
            secondClosestNode = it
        }
        setFastestPath(newGraph to newGraph.totalLenght(), setPath = false)
        return true
    }

    private fun skipIfCloser(graph: Graph): Graph = if (graph.nodes.size > 1) {
        val hideNearby = if (Minecraft.getMinecraft().thePlayer.onGround) 3 else 5
        Graph(graph.nodes.takeLastWhile { it.position.distanceToPlayer() > hideNearby })
    } else {
        graph
    }

    private fun findNewPath() {
        val goal = IslandGraphs.goal ?: return
        val closest = pathfindClosestNode ?: return

        val (path, distance) = GraphUtils.findShortestPathAsGraphWithDistance(closest, goal)
        val first = path.firstOrNull()
        val second = path.getOrNull(1)

        val playerPosition = LocationUtils.playerLocation()
        val nodeDistance = first?.let { playerPosition.distance(it.position) } ?: 0.0
        if (first != null && second != null) {
            val direct = playerPosition.distance(second.position)
            val firstPath = first.neighbours[second] ?: 0.0
            val around = nodeDistance + firstPath
            if (direct < around) {
                setFastestPath(Graph(path.drop(1)) to (distance - firstPath + direct))
                return
            }
        }
        setFastestPath(path to (distance + nodeDistance))
    }

    private fun Graph.totalLenght(): Double = nodes.zipWithNext().sumOf { (a, b) -> a.position.distance(b.position) }

    private fun handlePositionChange() {
        updateChat()
    }

    private var hasMoved = false

    private fun checkMoved() {
        if (hasMoved) {
            hasMoved = false
            if (goal != null) {
                handlePositionChange()
            }
        }
    }

    @SubscribeEvent
    fun onPlayerMove(event: EntityMoveEvent) {
        if (currentIslandGraph != null && event.entity == Minecraft.getMinecraft().thePlayer) {
            hasMoved = true
        }
    }

    private fun setFastestPath(path: Pair<Graph, Double>, setPath: Boolean = true) {
        // TODO cleanup
        val (fastestPath, distance) = path.takeIf { it.first.isNotEmpty() } ?: return
        val nodes = fastestPath.nodes.toMutableList()
        if (Minecraft.getMinecraft().thePlayer.onGround) {
            nodes.add(0, GraphNode(0, LocationUtils.playerLocation()))
        }
        if (setPath) {
            this.fastestPath = skipIfCloser(Graph(cutByMaxDistance(nodes, 2.0)))
        }
        updateChat()
    }

    private fun onNewNode() {
        // TODO create an event
        IslandAreas.nodeMoved()
        if (shouldAllowRerouting) {
            tryRerouting()
        }
    }

    private fun tryRerouting() {
        val target = currentTargetNode ?: return
        val closest = pathfindClosestNode ?: return
        val map = GraphUtils.findAllShortestDistances(closest).distances.filter { it.key.sameNameAndTags(target) }
        val newTarget = map.sorted().keys.firstOrNull() ?: return
        if (newTarget != target) {
            ChatUtils.debug("Rerouting navigation..")
            newTarget.pathFind(label, color, onFound, allowRerouting = true, condition = condition)
        }
    }

    fun stop() {
        currentTarget = null
        goal = null
        fastestPath = null
        currentTargetNode = null
        label = ""
        totalDistance = 0.0
        lastDistance = 0.0
    }

    /**
     * Activates pathfinding, with this graph node as goal.
     *
     * @param label The name of the naviation goal in chat.
     * @param color The color of the lines in world.
     * @param onFound The callback that gets fired when the goal is reached.
     * @param allowRerouting When a different node with same name and tags as the origianl goal is closer to the player, starts routing to this instead.
     * @param condition The pathfinding stops when the condition is no longer valid.
     */
    fun GraphNode.pathFind(
        label: String,
        color: Color = LorenzColor.WHITE.toColor(),
        onFound: () -> Unit = {},
        allowRerouting: Boolean = false,
        condition: () -> Boolean,
    ) {
        reset()
        currentTargetNode = this
        shouldAllowRerouting = allowRerouting
        pathFind0(location = position, label, color, onFound, condition)
    }

    /**
     * Activates pathfinding to a location in the island.
     *
     * @param location The goal of the pathfinder.
     * @param label The name of the navigation goal in chat. Cannot be empty.
     * @param color The color of the lines in the world.
     * @param onFound The callback that gets fired when the goal is reached.
     * @param condition The pathfinding stops when the condition is no longer valid.
     */
    fun pathFind(
        location: LorenzVec,
        label: String,
        color: Color = LorenzColor.WHITE.toColor(),
        onFound: () -> Unit = {},
        condition: () -> Boolean,
    ) {
        require(label.isNotEmpty()) { "Label cannot be empty." }
        reset()
        shouldAllowRerouting = false
        pathFind0(location, label, color, onFound, condition)
    }

    private fun pathFind0(
        location: LorenzVec,
        label: String,
        color: Color = LorenzColor.WHITE.toColor(),
        onFound: () -> Unit = {},
        condition: () -> Boolean,
    ) {
        currentTarget = location
        this.label = label
        this.color = color
        this.onFound = onFound
        this.condition = condition
        val graph = currentIslandGraph ?: return
        goal = graph.minBy { it.position.distance(currentTarget!!) }
        updateChat()
    }

    private const val PATHFIND_ID = -6457563

    private fun updateChat() {
        if (label == "") return
        val path = fastestPath ?: return
        var distance = 0.0
        if (path.isNotEmpty()) {
            for ((a, b) in path.zipWithNext()) {
                distance += a.position.distance(b.position)
            }
            val distanceToPlayer = path.first().position.distanceToPlayer()
            distance += distanceToPlayer
            distance = distance.roundTo(1)
        }

        if (distance == lastDistance) return
        lastDistance = distance
        if (distance == 0.0) return
        if (totalDistance == 0.0 || distance > totalDistance) {
            totalDistance = distance
        }
        sendChatDistance(distance)
    }

    private fun sendChatDistance(distance: Double) {
        val percentage = (1 - (distance / totalDistance)) * 100
        val componentText = "§e[SkyHanni] Navigating to §r$label §f[§e$distance§f] §f(§c${percentage.roundTo(1)}%§f)".asComponent()
        componentText.onClick(
            onClick = {
                stop()
                "§e[SkyHanni] Navigation stopped!".asComponent().send(PATHFIND_ID)
            },
        )
        componentText.hover = "§eClick to stop navigating!".asComponent()
        componentText.send(PATHFIND_ID)
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (currentIslandGraph == null) return
        val path = fastestPath ?: return

        // maybe reuse for debuggin
//         for ((a, b) in path.nodes.zipWithNext()) {
//             val diff = a.position.distance(b.position)
//             event.drawString(a.position, "diff: ${diff.roundTo(1)}")
//         }
        event.draw3DPathWithWaypoint(
            path,
            color,
            6,
            true,
            bezierPoint = 0.6,
            textSize = 1.0,
        )

        val targetLocation = currentTarget ?: return
        val lastNode = path.nodes.lastOrNull()?.position ?: return
        event.draw3DLine(lastNode.add(0.5, 0.5, 0.5), targetLocation.add(0.5, 0.5, 0.5), color, 4, true)
    }

    // TODO move into new utils class
    private fun cutByMaxDistance(nodes: List<GraphNode>, maxDistance: Double): List<GraphNode> {
        var index = nodes.size * 10
        val locations = mutableListOf<LorenzVec>()
        var first = true
        for (node in nodes) {
            if (first) {
                first = false
            } else {
                var lastPosition = locations.last()
                val currentPosition = node.position
                val vector = (currentPosition - lastPosition).normalize()
                var distance = lastPosition.distance(currentPosition)
                while (distance > maxDistance) {
                    distance -= maxDistance
                    val nextStepDistance = if (distance < maxDistance / 2) {
                        (maxDistance + distance) / 2
                        break
                    } else maxDistance
                    val newPosition = lastPosition + (vector * (nextStepDistance))
                    locations.add(newPosition)
                    lastPosition = newPosition
                }
            }
            locations.add(node.position)
        }

        return locations.map { GraphNode(index++, it) }
    }
}
