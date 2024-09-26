package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.model.Graph
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.model.findShortestPathAsGraphWithDistance
import at.hannibal2.skyhanni.data.repo.RepoUtils
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.skyblock.ScoreboardAreaChangeEvent
import at.hannibal2.skyhanni.features.misc.IslandAreas
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DPathWithWaypoint
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.io.File
import kotlin.math.abs
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

    val existsForThisIsland get() = currentIslandGraph != null

    var closedNote: GraphNode? = null

    private var currentTarget: LorenzVec? = null
    private var color = Color.WHITE
    private var showGoalExact = false
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
        setNewGraph(graph)
    }

    fun setNewGraph(graph: Graph) {
        IslandAreas.display = null
        reset()
        currentIslandGraph = graph

        // calling various update functions to make swtiching between deep caverns and glacite tunnels bareable
        handleTick()
        IslandAreas.noteMoved()
        DelayedRun.runDelayed(150.milliseconds) {
            IslandAreas.updatePosition()
        }
    }

    private fun reset() {
        closedNote = null
        currentTarget = null
        goal = null
        fastestPath = null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        handleTick()
    }

    private fun handleTick() {
        val prevClosed = closedNote

        val graph = currentIslandGraph ?: return

        currentTarget?.let {
            if (it.distanceToPlayer() < 3) {
                onFound()
                reset()
            }
            if (!condition()) {
                reset()
            }
        }

        val newClosest = if (SkyHanniDebugsAndTests.c == 0.0) {
            graph.minBy { it.position.distanceSqToPlayer() }
        } else null
        if (closedNote == newClosest) return
        closedNote = newClosest
        onNewNote()
        val closest = closedNote ?: return
        val goal = goal ?: return
        if (closest == prevClosed) return

        val (path, distance) = graph.findShortestPathAsGraphWithDistance(closest, goal)
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

    private fun setFastestPath(path: Pair<Graph, Double>) {
        fastestPath = path.takeIf { it.first.isNotEmpty() }?.first

        fastestPath?.let {
            fastestPath = Graph(cutByMaxDistance(it.nodes, 3.0))
        }
    }

    private fun onNewNote() {
        // TODO create an event
        IslandAreas.noteMoved()
    }

    fun stop() {
        currentTarget = null
        goal = null
        fastestPath = null
    }

    fun pathFind(
        location: LorenzVec,
        color: Color = LorenzColor.WHITE.toColor(),
        onFound: () -> Unit = {},
        showGoalExact: Boolean = false,
        condition: () -> Boolean = { true },
    ) {
        reset()
        currentTarget = location
        this.color = color
        this.onFound = onFound
        this.showGoalExact = showGoalExact
        this.condition = condition
        val graph = currentIslandGraph ?: return
        goal = graph.minBy { it.position.distance(currentTarget!!) }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val path = fastestPath ?: return

        var graph = path
        graph = skipNodes(graph) ?: graph

        event.draw3DPathWithWaypoint(
            graph,
            color,
            6,
            true,
            bezierPoint = 2.0,
            textSize = 1.0,
        )
        val lastNode = graph.nodes.last().position
        val targetLocation = currentTarget ?: return
        event.draw3DLine(lastNode.add(0.5, 0.5, 0.5), targetLocation.add(0.5, 0.5, 0.5), color, 4, true)

        if (showGoalExact) {
            event.drawWaypointFilled(targetLocation, color)
        }
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

    // trying to find a faster node-path, if the future nodes are in line of sight and gratly beneift the current path
    private fun skipNodes(graph: Graph): Graph? {
        val closedNode = closedNote ?: return null

        val playerEyeLocation = LocationUtils.playerEyeLocation()
        val playerY = playerEyeLocation.y - 1

        val distanceToPlayer = closedNode.position.distanceToPlayer()
        val skipNodeDistance = distanceToPlayer > 8
        val maxSkipDistance = if (skipNodeDistance) 50.0 else 20.0

        val nodes = graph.nodes
        val potentialSkip =
            nodes.lastOrNull { it.position.canBeSeen(maxSkipDistance, -1.0) && abs(it.position.y - playerY) <= 2 } ?: return null

        val angleSkip = if (potentialSkip == nodes.first()) {
            false
        } else {
            val v1 = potentialSkip.position - playerEyeLocation
            val v2 = nodes.first().position - playerEyeLocation
            val v = v1.angleInRad(v2)
            v > 1
        }

        if (!skipNodeDistance && !angleSkip) return null

        val list = mutableListOf<GraphNode>()
        list.add(potentialSkip)

        var passed = false
        for (node in nodes) {
            if (passed) {
                list.add(node)
            } else {
                if (node == potentialSkip) {
                    passed = true
                }
            }
        }

        return Graph(list)
    }
}
