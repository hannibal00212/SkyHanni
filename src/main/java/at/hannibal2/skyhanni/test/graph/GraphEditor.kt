package at.hannibal2.skyhanni.test.graph

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandGraphs.pathFind
import at.hannibal2.skyhanni.data.model.Graph
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.model.GraphNodeTag
import at.hannibal2.skyhanni.data.model.TextInput
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils
import at.hannibal2.skyhanni.utils.GraphUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RaycastUtils
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLineNea
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawPyramid
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import kotlinx.coroutines.runBlocking
import net.minecraft.client.settings.KeyBinding
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.awt.Color
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@Suppress("LargeClass")
@SkyHanniModule
object GraphEditor {

    val config get() = SkyHanniMod.feature.dev.devTool.graph

    fun isEnabled() = config != null && config.enabled

    private var id = 0

    val nodes = mutableListOf<GraphingNode>()
    private val edges = mutableListOf<GraphingEdge>()

    var activeNode: GraphingNode? = null
        set(value) {
            field = value
            selectedEdge = findEdgeBetweenActiveAndClosest()
            checkDissolve()
        }
    private var closestNode: GraphingNode? = null
        set(value) {
            field = value
            selectedEdge = findEdgeBetweenActiveAndClosest()
        }

    private var selectedEdge: GraphingEdge? = null
    private var ghostPosition: LorenzVec? = null

    private var seeThroughBlocks = true

    private var inEditMode = false
    private var inTextMode = false
        set(value) {
            field = value
            if (value) {
                activeNode?.name?.let {
                    textBox.textBox = it
                }

                textBox.makeActive()
            } else {
                textBox.clear()
                textBox.disable()
            }
        }

    private var inTutorialMode = false

    private val textBox = TextInput()

    private val nodeColor = LorenzColor.BLUE.addOpacity(200)
    private val activeColor = LorenzColor.GREEN.addOpacity(200)
    private val closestColor = LorenzColor.YELLOW.addOpacity(200)
    private val dijkstraColor = LorenzColor.LIGHT_PURPLE.addOpacity(200)

    private val edgeColor = LorenzColor.GOLD.addOpacity(150)
    private val edgeDijkstraColor = LorenzColor.DARK_BLUE.addOpacity(150)
    private val edgeSelectedColor = LorenzColor.DARK_RED.addOpacity(150)

    private val nodesAlreadyFound = mutableListOf<LorenzVec>()
    private val nodesToFind: List<LorenzVec>
        get() = IslandGraphs.currentIslandGraph?.nodes?.map { it.position }?.filter { it !in nodesAlreadyFound }.orEmpty()
    private var currentNodeToFind: LorenzVec? = null
    private var active = false

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        nodes.forEach { event.drawNode(it) }
        edges.forEach { event.drawEdge(it) }
        drawGhostPosition(event)
    }

    private fun drawGhostPosition(event: SkyHanniRenderWorldEvent) {
        val ghostPosition = ghostPosition ?: return
        if (ghostPosition.distanceToPlayer() >= config.maxNodeDistance) return

        event.drawWaypointFilled(
            ghostPosition,
            if (activeNode == null) Color.RED else Color.GRAY,
            seeThroughBlocks = seeThroughBlocks,
            minimumAlpha = 0.2f,
            inverseAlphaScale = true,
        )
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.infoDisplay.renderStrings(buildDisplay(), posLabel = "Graph Info")
    }

    private fun buildDisplay(): List<String> = buildList {
        add("§eExit: §6${KeyboardManager.getKeyName(config.exitKey)}")
        if (!inEditMode && !inTextMode) {
            add("§ePlace: §6${KeyboardManager.getKeyName(config.placeKey)}")
            add("§eSelect: §6${KeyboardManager.getKeyName(config.selectKey)}")
            add("§eSelect (Look): §6${KeyboardManager.getKeyName(config.selectRaycastKey)}")
            add("§eConnect: §6${KeyboardManager.getKeyName(config.connectKey)}")
            add("§eTest: §6${KeyboardManager.getKeyName(config.dijkstraKey)}")
            add("§eVision: §6${KeyboardManager.getKeyName(config.throughBlocksKey)}")
            add("§eSave: §6${KeyboardManager.getKeyName(config.saveKey)}")
            add("§eLoad: §6${KeyboardManager.getKeyName(config.loadKey)}")
            add("§eClear: §6${KeyboardManager.getKeyName(config.clearKey)}")
            add("§eTutorial: §6${KeyboardManager.getKeyName(config.tutorialKey)}")
            add("§eToggle Ghost Position: §6${KeyboardManager.getKeyName(config.toggleGhostPosition)}")
            add(" ")
            if (activeNode != null) {
                add("§eText: §6${KeyboardManager.getKeyName(config.textKey)}")
                if (dissolvePossible) add("§eDissolve: §6${KeyboardManager.getKeyName(config.dissolveKey)}")
                if (selectedEdge != null) {
                    add("§eSplit: §6${KeyboardManager.getKeyName(config.splitKey)}")
                    add("§eCycle Direction: §6${KeyboardManager.getKeyName(config.edgeCycle)}")
                }
            }
        }

        if (!inTextMode) {
            if (activeNode != null) {
                add("§eEdit active node: §6${KeyboardManager.getKeyName(config.editKey)}")
            } else if (ghostPosition != null) {
                add("Edit Ghost Position: §6${KeyboardManager.getKeyName(config.editKey)}")
            }
        }

        if (inEditMode) {
            add("§ex+ §6${KeyboardManager.getKeyName(KeyboardManager.WasdInputMatrix.w.keyCode)}")
            add("§ex- §6${KeyboardManager.getKeyName(KeyboardManager.WasdInputMatrix.s.keyCode)}")
            add("§ez+ §6${KeyboardManager.getKeyName(KeyboardManager.WasdInputMatrix.a.keyCode)}")
            add("§ez- §6${KeyboardManager.getKeyName(KeyboardManager.WasdInputMatrix.d.keyCode)}")
            add("§ey+ §6${KeyboardManager.getKeyName(KeyboardManager.WasdInputMatrix.up.keyCode)}")
            add("§ey- §6${KeyboardManager.getKeyName(KeyboardManager.WasdInputMatrix.down.keyCode)}")
        }
        if (inTextMode) {
            add("§eFormat: ${textBox.finalText()}")
            add("§eRaw:     ${textBox.editText(textColor = LorenzColor.YELLOW)}")
        }
    }

    private var dissolvePossible = false

    private fun findEdgeBetweenActiveAndClosest(): GraphingEdge? = getEdgeIndex(activeNode, closestNode)?.let { edges[it] }

    private fun checkDissolve() {
        if (activeNode == null) {
            dissolvePossible = false
            return
        }
        dissolvePossible = edges.count { it.isInEdge(activeNode) } == 2
    }

    private fun feedBackInTutorial(text: String) {
        if (inTutorialMode) {
            ChatUtils.chat(text)
        }
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        input()
        if (nodes.isEmpty()) return
        closestNode = nodes.minBy { distanceToPlayer(it.position) }
        handleAllNodeFind()

    }

    private fun handleAllNodeFind() {
        if (!active) return

        if (nodesToFind.isEmpty()) return
        val closest = nodesToFind.minBy { distanceToPlayer(it) }
        if (closest.distanceToPlayer() >= 3) return
        nodesAlreadyFound.add(closest)

        if (nodesToFind.isEmpty()) {
            currentNodeToFind = null
            ChatUtils.chat("Found all nodes on this island")
            LorenzUtils.sendTitle("§eAll Found!", 3.seconds)
            active = false
            return
        }

        calculateNewAllNodeFind()
    }

    private fun calculateNewAllNodeFind(): LorenzVec {
        val next = GraphUtils.findAllShortestDistancesOnCurrentIsland(
            LocationUtils.playerLocation(),
        ).distances.keys.first { it.position in nodesToFind }.position

        val max = IslandGraphs.currentIslandGraph?.nodes?.size ?: -1
        val todo = nodesToFind.size
        val done = max - todo
        val percentage = (done.toDouble() / max.toDouble()) * 100
        val node = GraphUtils.nearestNodeOnCurrentIsland(next)
        node.pathFind(
            "Progress: ${done.addSeparators()}/${max.addSeparators()} (${percentage.roundTo(2)}%)",
            condition = { active },
        )
        currentNodeToFind = next
        return next
    }

    private fun toggleFindAll() {
        active = !active
        if (active) {
            nodesAlreadyFound.clear()
            calculateNewAllNodeFind()
            ChatUtils.chat("Graph navigation over all nodes started.")
        } else {
            ChatUtils.chat("Graph navigation over all nodes stopped.")
        }
    }

    private fun SkyHanniRenderWorldEvent.drawNode(node: GraphingNode) {
        if (node.position.distanceToPlayer() > config.maxNodeDistance) return
        this.drawWaypointFilled(
            node.position,
            node.getNodeColor(),
            seeThroughBlocks = seeThroughBlocks,
            minimumAlpha = 0.2f,
            inverseAlphaScale = true,
        )

        val nodeName = node.name ?: return
        this.drawDynamicText(
            node.position,
            nodeName,
            0.8,
            ignoreBlocks = seeThroughBlocks || distanceToPlayer(node.position) < 100,
            smallestDistanceVew = 12.0,
            ignoreY = true,
            yOff = -15f,
            maxDistance = 80,
        )

        val tags = node.tags
        if (tags.isEmpty()) return
        val tagText = tags.map { it.displayName }.joinToString(" §f+ ")
        this.drawDynamicText(
            node.position,
            tagText,
            0.8,
            ignoreBlocks = seeThroughBlocks || distanceToPlayer(node.position) < 100,
            smallestDistanceVew = 12.0,
            ignoreY = true,
            yOff = 0f,
            maxDistance = 80,
        )
    }

    private fun SkyHanniRenderWorldEvent.drawEdge(edge: GraphingEdge) {
        if (edge.node1.position.distanceToPlayer() > config.maxNodeDistance) return
        val color = when {
            selectedEdge == edge -> edgeSelectedColor
            edge in highlightedEdges -> edgeDijkstraColor
            else -> edgeColor
        }

        this.draw3DLineNea(
            edge.node1.position.add(0.5, 0.5, 0.5),
            edge.node2.position.add(0.5, 0.5, 0.5),
            color,
            7,
            !seeThroughBlocks,
        )
        if (edge.direction != EdgeDirection.BOTH) {
            drawDirection(edge, color)
        }
    }

    private fun SkyHanniRenderWorldEvent.drawDirection(edge: GraphingEdge, color: Color) {
        val lineVec = edge.node2.position - edge.node1.position
        val center = edge.node1.position + lineVec / 2.0
        val quad1 = edge.node1.position + lineVec / 4.0
        val quad2 = edge.node1.position + lineVec * (3.0 / 4.0)

        val pyramidSize =
            lineVec.normalize().times(min(lineVec.length() / 10.0, 1.0)) * (if (edge.direction == EdgeDirection.ONE_TO_TWO) 1.0 else -1.0)

        val lineOffsetVec = LorenzVec(0.5, 0.5, 0.5)

        fun pyramidDraw(
            pos: LorenzVec,
        ) {
            this.drawPyramid(
                pos + lineOffsetVec + pyramidSize,
                pos + lineOffsetVec,
                pos.crossProduct(lineVec).normalize().times(pyramidSize.length() / 2.5) + pos + lineOffsetVec,
                color,
            )
        }

        pyramidDraw(center)
        pyramidDraw(quad1)
        pyramidDraw(quad2)
    }

    private fun GraphingNode.getNodeColor() = when (this) {
        activeNode -> if (this == closestNode) ColorUtils.blendRGB(activeColor, closestColor, 0.5) else activeColor
        closestNode -> closestColor
        in highlightedNodes -> dijkstraColor
        else -> nodeColor
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shgraph") {
            description = "Enables the graph editor"
            category = CommandCategory.DEVELOPER_TEST
            callback { toggleFeature() }
        }
        event.register("shgraphfindall") {
            description = "Navigate over the whole graph network"
            category = CommandCategory.DEVELOPER_TEST
            callback { toggleFindAll() }
        }
        event.register("shgraphloadthisisland") {
            description = "Loads the current island data into the graph editor."
            category = CommandCategory.DEVELOPER_TEST
            callback { loadThisIsland() }
        }
    }

    private fun loadThisIsland() {
        val graph = IslandGraphs.currentIslandGraph
        if (graph == null) {
            ChatUtils.userError("This island does not have graph data!")
            return
        }
        if (!config.enabled) {
            config.enabled = true
            ChatUtils.chat("Graph Editor is now active.")
        }

        import(graph)
        ChatUtils.chat("Graph Editor loaded this island!")
    }

    private fun toggleFeature() {
        config.enabled = !config.enabled
        if (config.enabled) {
            ChatUtils.chat("Graph Editor is now active.")
        } else {
            chatAtDisable()
        }
    }

    private fun chatAtDisable() = ChatUtils.clickableChat(
        "Graph Editor is now inactive. §lClick to activate.",
        GraphEditor::toggleFeature,
    )

    private fun input() {
        if (LorenzUtils.isAnyGuiActive()) return
        if (config.exitKey.isKeyClicked()) {
            if (inTextMode) {
                inTextMode = false
                feedBackInTutorial("Exited Text Mode.")
                return
            }
            if (inEditMode) {
                inEditMode = false
                feedBackInTutorial("Exited Edit Mode.")
                return
            }
            config.enabled = false
            chatAtDisable()
        }
        if (inTextMode) {
            textBox.handle()
            val text = textBox.finalText()
            if (text.isEmpty()) {
                activeNode?.name = null
            } else {
                activeNode?.name = text
            }
            return
        }
        if (activeNode != null && config.textKey.isKeyClicked()) {
            inTextMode = true
            feedBackInTutorial("Entered Text Mode.")
            return
        }
        if (inEditMode) {
            editModeClicks()
            inEditMode = false
        }
        if ((activeNode != null || ghostPosition != null) && config.editKey.isKeyHeld()) {
            inEditMode = true
            return
        }
        if (config.saveKey.isKeyClicked()) {
            save()
            return
        }
        if (config.loadKey.isKeyClicked()) {
            runBlocking {
                OSUtils.readFromClipboard()?.let {
                    try {
                        Graph.fromJson(it)
                    } catch (e: Exception) {
                        ErrorManager.logErrorWithData(
                            e,
                            "Import of graph failed.",
                            "json" to it,
                            ignoreErrorCache = true,
                        )
                        null
                    }
                }?.let {
                    import(it)
                    ChatUtils.chat("Loaded Graph from clip board.")
                }
            }
            return
        }
        if (config.clearKey.isKeyClicked()) {
            val json = compileGraph().toJson()
            OSUtils.copyToClipboard(json)
            ChatUtils.chat("Copied Graph to Clipboard and cleared the graph.")
            clear()
        }
        if (config.placeKey.isKeyClicked()) {
            addNode()
        }
        if (config.toggleGhostPosition.isKeyClicked()) {
            toggleGhostPosition()
        }
        if (config.selectKey.isKeyClicked()) {
            activeNode = if (activeNode == closestNode) {
                feedBackInTutorial("De-selected active node.")
                null
            } else {
                feedBackInTutorial("Selected new active node.")
                closestNode
            }
        }
        if (config.selectRaycastKey.isKeyClicked()) {
            val playerRay = RaycastUtils.createPlayerLookDirectionRay()
            var minimumDistance = Double.MAX_VALUE
            var minimumNode: GraphingNode? = null
            for (node in nodes) {
                val nodeCenterPosition = node.position.add(0.5, 0.5, 0.5)
                val distance = RaycastUtils.findDistanceToRay(playerRay, nodeCenterPosition)
                if (distance > minimumDistance) {
                    continue
                }
                if (minimumDistance > 1.0) {
                    minimumNode = node
                    minimumDistance = distance
                    continue
                }
                if (minimumNode == null || distanceToPlayer(minimumNode.position) > distanceToPlayer(node.position)) {
                    minimumNode = node
                    minimumDistance = distance
                }
            }
            activeNode = minimumNode
        }
        if (activeNode != closestNode && config.connectKey.isKeyClicked()) {
            val edge = getEdgeIndex(activeNode, closestNode)
            if (edge == null) {
                addEdge(activeNode, closestNode)
                feedBackInTutorial("Added new edge.")
            } else {
                edges.removeAt(edge)
                checkDissolve()
                selectedEdge = findEdgeBetweenActiveAndClosest()
                feedBackInTutorial("Removed edge.")
            }
        }
        if (config.throughBlocksKey.isKeyClicked()) {
            seeThroughBlocks = !seeThroughBlocks
            feedBackInTutorial(
                if (seeThroughBlocks) "Graph is visible though walls." else "Graph is invisible behind walls.",
            )
        }
        if (config.dijkstraKey.isKeyClicked()) {
            feedBackInTutorial("Calculated shortest route and cleared active node.")
            testDijkstra()
        }
        if (config.tutorialKey.isKeyClicked()) {
            inTutorialMode = !inTutorialMode
            ChatUtils.chat("Tutorial mode is now ${if (inTutorialMode) "active" else "inactive"}.")
        }
        val selectedEdge = selectedEdge
        if (selectedEdge != null) {
            if (config.splitKey.isKeyClicked()) {
                feedBackInTutorial("Split Edge into a Node and two edges.")
                val middle = selectedEdge.node1.position.middle(selectedEdge.node2.position).roundLocationToBlock()
                val node = GraphingNode(id++, middle)
                nodes.add(node)
                edges.remove(selectedEdge)
                addEdge(selectedEdge.node1, node, selectedEdge.direction)
                addEdge(node, selectedEdge.node2, selectedEdge.direction)
                activeNode = node
            }
            if (config.edgeCycle.isKeyClicked()) {
                selectedEdge.cycleDirection(activeNode)
                feedBackInTutorial("Cycled Direction to: ${selectedEdge.cycleText(activeNode)}")
            }
        }
        if (dissolvePossible && config.dissolveKey.isKeyClicked()) {
            feedBackInTutorial("Dissolved the node, now it is gone.")
            val edgePair = edges.filter { it.isInEdge(activeNode) }
            val edge1 = edgePair[0]
            val edge2 = edgePair[1]
            val neighbors1 = if (edge1.node1 == activeNode) edge1.node2 else edge1.node1
            val neighbors2 = if (edge2.node1 == activeNode) edge2.node2 else edge2.node1
            val direction =
                if (edge1.direction == EdgeDirection.BOTH || edge2.direction == EdgeDirection.BOTH) EdgeDirection.BOTH else when {
                    edge1.isValidConnectionFromTo(neighbors1, activeNode) && edge2.isValidConnectionFromTo(
                        activeNode,
                        neighbors2,
                    ) -> EdgeDirection.ONE_TO_TWO

                    edge1.isValidConnectionFromTo(activeNode, neighbors1) && edge2.isValidConnectionFromTo(
                        neighbors2,
                        activeNode,
                    ) -> EdgeDirection.TOW_TO_ONE

                    else -> EdgeDirection.BOTH
                }
            edges.removeAll(edgePair)
            nodes.remove(activeNode)
            activeNode = null
            addEdge(neighbors1, neighbors2, direction)
        }
    }

    private fun save() {
        if (nodes.isEmpty()) {
            ChatUtils.chat("Copied nothing since the graph is empty.")
            return
        }
        val compileGraph = compileGraph()
        if (config.useAsIslandArea) {
            IslandGraphs.setNewGraph(compileGraph)
            GraphEditorBugFinder.runTests()
            if (active) {
                calculateNewAllNodeFind()
            }
        }
        val json = compileGraph.toJson()
        OSUtils.copyToClipboard(json)
        ChatUtils.chat("Copied Graph to Clipboard.")
        if (config.showsStats) {
            val length = edges.sumOf { it.node1.position.distance(it.node2.position) }.toInt().addSeparators()
            ChatUtils.chat(
                "§lStats\n" + "§eNamed Nodes: ${
                    nodes.count { it.name != null }.addSeparators()
                }\n" + "§eNodes: ${nodes.size.addSeparators()}\n" + "§eEdges: ${edges.size.addSeparators()}\n" + "§eLength: $length",
            )
        }
    }

    private fun editModeClicks() {
        val vector = LocationUtils.calculatePlayerFacingDirection()
        KeyboardManager.WasdInputMatrix.w.handleEditClicks(vector)
        KeyboardManager.WasdInputMatrix.a.handleEditClicks(vector.rotateXZ(Math.toRadians(90.0)))
        KeyboardManager.WasdInputMatrix.s.handleEditClicks(vector.rotateXZ(Math.toRadians(180.0)))
        KeyboardManager.WasdInputMatrix.d.handleEditClicks(vector.rotateXZ(Math.toRadians(270.0)))

        KeyboardManager.WasdInputMatrix.up.handleEditClicks(LorenzVec(0, 1, 0))
        KeyboardManager.WasdInputMatrix.down.handleEditClicks(LorenzVec(0, -1, 0))
    }

    private fun KeyBinding.handleEditClicks(vector: LorenzVec) {
        if (this.keyCode.isKeyClicked()) {
            activeNode?.let {
                it.position = it.position + vector
            } ?: run {
                ghostPosition?.let {
                    ghostPosition = it + vector
                }
            }
        }
    }

    fun onMinecraftInput(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isEnabled()) return
        if (!inEditMode) return
        if (keyBinding !in KeyboardManager.WasdInputMatrix) return
        cir.returnValue = false
    }

    private fun addNode() {
        val closestNode = closestNode
        if (closestNode != null && distanceToPlayer(closestNode.position) < 9.0) {
            if (closestNode == activeNode) {
                feedBackInTutorial("Removed node, since you where closer than 3 blocks from a the active node.")
                nodes.remove(closestNode)
                edges.removeIf { it.isInEdge(closestNode) }
                if (closestNode == activeNode) activeNode = null
                GraphEditor.closestNode = null
                return
            }
        }

        val position = ghostPosition ?: LocationUtils.playerEyeLocation().roundLocationToBlock()
        if (nodes.any { it.position == position }) {
            feedBackInTutorial("Can't create node, here is already another one.")
            return
        }
        val node = GraphingNode(id++, position)
        nodes.add(node)
        feedBackInTutorial("Added graph node.")
        if (activeNode == null) return
        addEdge(activeNode, node)
    }

    fun toggleGhostPosition() {
        if (ghostPosition != null) {
            ghostPosition = null
            feedBackInTutorial("Disabled Ghost Position.")
        } else {
            ghostPosition = LocationUtils.playerEyeLocation().roundLocationToBlock()
            feedBackInTutorial("Enabled Ghost Position.")
        }
    }

    private fun getEdgeIndex(node1: GraphingNode?, node2: GraphingNode?) =
        if (node1 != null && node2 != null && node1 != node2) GraphingEdge(
            node1,
            node2,
        ).let { e -> edges.indexOfFirst { it == e }.takeIf { it != -1 } }
        else null

    private fun addEdge(node1: GraphingNode?, node2: GraphingNode?, direction: EdgeDirection = EdgeDirection.BOTH) =
        if (node1 != null && node2 != null && node1 != node2) {
            val edge = GraphingEdge(node1, node2, direction)
            if (edge.isInEdge(activeNode)) {
                checkDissolve()
                selectedEdge = findEdgeBetweenActiveAndClosest()
            }
            edges.add(edge)
        } else false

    private fun compileGraph(): Graph {
        val indexedTable = nodes.mapIndexed { index, node -> node.id to index }.toMap()
        val nodes = nodes.mapIndexed { index, node ->
            GraphNode(
                index,
                node.position,
                node.name,
                node.tags.map {
                    it.internalName
                },
            )
        }
        val neighbours = GraphEditor.nodes.map { node ->
            edges.filter { it.isInEdge(node) && it.isValidDirectionFrom(node) }.map { edge ->
                val otherNode = if (node == edge.node1) edge.node2
                else edge.node1
                // TODO: Fix this to not use a bang bang
                @Suppress("MapGetWithNotNullAssertionOperator")
                nodes[indexedTable[otherNode.id]!!] to node.position.distance(otherNode.position)
            }.sortedBy { it.second }
        }
        nodes.forEachIndexed { index, node -> node.neighbours = neighbours[index].toMap() }
        return Graph(nodes)
    }

    fun import(graph: Graph) {
        clear()
        nodes.addAll(
            graph.map {
                GraphingNode(
                    it.id,
                    it.position,
                    it.name,
                    it.tagNames.mapNotNull { tag -> GraphNodeTag.byId(tag) }.toMutableList(),
                )
            },
        )
        val translation = graph.mapIndexed { index, node -> node to nodes[index] }.toMap()

        val neighbors = graph.map { node ->
            // TODO: Fix this to not use bang bangs
            @Suppress("MapGetWithNotNullAssertionOperator")
            node.neighbours.map {
                GraphingEdge(
                    translation[node]!!,
                    translation[it.key]!!,
                    EdgeDirection.ONE_TO_TWO,
                )
            }
        }.flatten()

        val reduced = neighbors.groupingBy { it }.reduce { _, accumulator, element ->
            if (
                (element.node1 == accumulator.node1 && accumulator.direction != element.direction) ||
                (element.node1 == accumulator.node2 && accumulator.direction == element.direction)
            ) {
                accumulator.direction = EdgeDirection.BOTH
            }
            accumulator
        }

        edges.addAll(reduced.values)
        id = nodes.lastOrNull()?.id?.plus(1) ?: 0
        checkDissolve()
        selectedEdge = findEdgeBetweenActiveAndClosest()
    }

    private val highlightedNodes = mutableSetOf<GraphingNode>()
    private val highlightedEdges = mutableSetOf<GraphingEdge>()

    private fun testDijkstra() {

        val savedCurrent = closestNode ?: return
        val savedActive = activeNode ?: return

        val compiled = compileGraph()
        import(compiled)
        highlightedEdges.clear()
        highlightedNodes.clear()

        val current = compiled.firstOrNull { it.position == savedCurrent.position } ?: return
        val goal = compiled.firstOrNull { it.position == savedActive.position } ?: return

        val path = GraphUtils.findShortestPathAsGraph(current, goal)

        if (path.isEmpty()) {
            ChatUtils.chat("No Path found")
        }

        val inGraph = path.map { nodes[it.id] }
        highlightedNodes.addAll(inGraph)

        highlightedEdges.addAll(
            highlightedNodes.zipWithNext { a, b -> edges.firstOrNull { it.isValidConnectionFromTo(a, b) } }.filterNotNull(),
        )
    }

    private fun clear() {
        id = 0
        nodes.clear()
        edges.clear()
        activeNode = null
        closestNode = null
        dissolvePossible = false
        ghostPosition = null
    }

    fun distanceToPlayer(location: LorenzVec): Double {
        val playerPosition = ghostPosition ?: LocationUtils.playerLocation()
        return location.distanceSq(playerPosition)
    }
}

// The node object the graph editor is working with
class GraphingNode(
    val id: Int,
    var position: LorenzVec,
    var name: String? = null,
    var tags: MutableList<GraphNodeTag> = mutableListOf(),
) {

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphingNode

        if (id != other.id) return false

        return true
    }
}

private class GraphingEdge(val node1: GraphingNode, val node2: GraphingNode, var direction: EdgeDirection = EdgeDirection.BOTH) {

    fun isInEdge(node: GraphingNode?) = node1 == node || node2 == node

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphingEdge

        return (this.node1 == other.node1 && this.node2 == other.node2) || (this.node1 == other.node2 && this.node2 == other.node1)
    }

    override fun hashCode(): Int {
        val hash1 = node1.hashCode()
        val hash2 = node2.hashCode()

        var result: Int
        if (hash1 <= hash2) {
            result = hash1
            result = 31 * result + hash2
        } else {
            result = hash2
            result = 31 * result + hash1
        }
        return result
    }

    fun cycleDirection(standpoint: GraphingNode?) {
        direction = if (standpoint != node2) {
            when (direction) {
                EdgeDirection.BOTH -> EdgeDirection.ONE_TO_TWO
                EdgeDirection.ONE_TO_TWO -> EdgeDirection.TOW_TO_ONE
                EdgeDirection.TOW_TO_ONE -> EdgeDirection.BOTH
            }
        } else {
            when (direction) {
                EdgeDirection.BOTH -> EdgeDirection.TOW_TO_ONE
                EdgeDirection.TOW_TO_ONE -> EdgeDirection.ONE_TO_TWO
                EdgeDirection.ONE_TO_TWO -> EdgeDirection.BOTH
            }
        }
    }

    fun cycleText(standpoint: GraphingNode?) = when (direction) {
        EdgeDirection.BOTH -> "Bidirectional"
        EdgeDirection.ONE_TO_TWO -> if (standpoint != node1) {
            "AwayFromYou"
        } else {
            "ToYou"
        }

        EdgeDirection.TOW_TO_ONE -> if (standpoint != node1) {
            "ToYou"
        } else {
            "AwayFromYou"
        }
    }

    fun isValidDirectionFrom(standpoint: GraphingNode?) = when (direction) {
        EdgeDirection.BOTH -> true
        EdgeDirection.ONE_TO_TWO -> standpoint == node1
        EdgeDirection.TOW_TO_ONE -> standpoint == node2
    }

    fun isValidConnectionFromTo(a: GraphingNode?, b: GraphingNode?): Boolean =
        ((this.node1 == a && this.node2 == b) || (this.node1 == b && this.node2 == a)) && isValidDirectionFrom(a)
}

private enum class EdgeDirection {
    BOTH,
    ONE_TO_TWO,
    TOW_TO_ONE,
}
