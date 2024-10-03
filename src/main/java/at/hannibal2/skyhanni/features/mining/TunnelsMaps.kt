package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.Graph
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.LorenzWarpEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.CollectionUtils.filterNotNullKeys
import at.hannibal2.skyhanni.utils.ColorUtils.getFirstColorCode
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.GraphUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzColor.Companion.toLorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DPathWithWaypoint
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TunnelsMaps {

    private val config get() = SkyHanniMod.feature.mining.tunnelMaps

    private var graph: Graph = Graph(emptyList())
    private lateinit var campfire: GraphNode

    private var goalReached = false
    private var prevGoal: GraphNode? = null
    private var goal: GraphNode? = null
        set(value) {
            prevGoal = field
            field = value
        }

    private var closestNode: GraphNode? = null
    private var path: Pair<Graph, Double>? = null

    private var possibleLocations = mapOf<String, List<GraphNode>>()
    private val cooldowns = mutableMapOf<GraphNode, SimpleTimeMark>()
    private var active: String = ""

    private lateinit var fairySouls: Map<String, GraphNode>

    // TODO what is this? why is there a difference? can this be replaced with GraphNodeTag.GRIND_ORES?
    private lateinit var newGemstones: Map<String, List<GraphNode>>
    private lateinit var oldGemstones: Map<String, List<GraphNode>>
    private lateinit var normalLocations: Map<String, List<GraphNode>>

    private var locationDisplay: List<Renderable> = emptyList()

    private fun getNext(name: String = active): GraphNode? {
        fairySouls[name]?.let {
            goalReached = false
            return it
        }

        val closest = closestNode ?: return null
        val list = possibleLocations[name] ?: return null

        val offCooldown = list.filter { cooldowns[it]?.isInPast() != false }
        val best = offCooldown.minByOrNull { GraphUtils.findShortestDistance(closest, it) } ?: list.minBy {
            cooldowns[it] ?: SimpleTimeMark.farPast()
        }
        if (cooldowns[best]?.isInPast() != false) {
            cooldowns[best] = 5.0.seconds.fromNow()
        }
        goalReached = false
        return best
    }

    private fun hasNext(name: String = active): Boolean {
        val list = possibleLocations[name] ?: return false
        return list.size > 1
    }

    private val oldGemstonePattern by RepoPattern.pattern(
        "mining.tunnels.maps.gem.old", ".*(?:Ruby|Amethyst|Jade|Sapphire|Amber|Topaz).*",
    )
    private val newGemstonePattern by RepoPattern.pattern(
        "mining.tunnels.maps.gem.new", ".*(?:Aquamarine|Onyx|Citrine|Peridot).*",
    )
    private val commissionInvPattern by RepoPattern.pattern(
        "mining.commission.inventory", "Commissions",
    )

    /**
     * REGEX-TEST: §7- §b277 Glacite Powder
     * REGEX-TEST: §7- §b1,010 Glacite Powder
     */
    private val glacitePattern by RepoPattern.pattern(
        "mining.commisson.reward.glacite",
        "§7- §b[\\d,]+ Glacite Powder",
    )
    private val collectorCommissionPattern by RepoPattern.pattern(
        "mining.commisson.collector",
        "§9(?<what>\\w+(?: \\w+)?) Collector",
    )
    private val invalidGoalPattern by RepoPattern.pattern(
        "mining.commisson.collector.invalid",
        "Glacite|Scrap",
    )
    private val completedPattern by RepoPattern.pattern(
        "mining.commisson.completed",
        "§a§lCOMPLETED",
    )

    private val translateTable = mutableMapOf<String, String>()

    /** @return Errors with an empty String */
    private fun getGenericName(input: String): String = translateTable.getOrPut(input) {
        possibleLocations.keys.firstOrNull { it.uppercase().removeColor().contains(input.uppercase()) } ?: ""
    }

    private var clickTranslate = mapOf<Int, String>()

    private val ROYAL_PIGEON by lazy { "ROYAL_PIGEON".asInternalName() }

    private var isCommission = false

    @SubscribeEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        clickTranslate = mapOf()
        if (!commissionInvPattern.matches(event.inventoryName)) return
        clickTranslate = event.inventoryItems.mapNotNull { (slotId, item) ->
            val lore = item.getLore()
            if (!glacitePattern.anyMatches(lore)) return@mapNotNull null
            if (completedPattern.anyMatches(lore)) return@mapNotNull null
            val type = lore.matchFirst(collectorCommissionPattern) {
                group("what")
            } ?: return@mapNotNull null
            if (invalidGoalPattern.matches(type)) return@mapNotNull null
            val mapName = getGenericName(type)
            if (mapName.isEmpty()) {
                ErrorManager.logErrorStateWithData(
                    "Unknown Collection Commission: $type", "$type can't be found in the graph.",
                    "type" to type,
                    "graphNames" to possibleLocations.keys,
                    "lore" to lore,
                )
                null
            } else {
                slotId to getGenericName(type)
            }
        }.toMap()
        if (config.autoCommission) {
            clickTranslate.values.firstOrNull()?.let {
                isCommission = true
                setActiveAndGoal(it)
            } ?: run {
                if (isCommission) {
                    active = ""
                    clearPath()
                    isCommission = false
                }
            }
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        clickTranslate = mapOf()
    }

    @SubscribeEvent
    fun onRenderItemTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        clickTranslate[event.slot.slotIndex]?.let {
            event.toolTip.add("§e§lRight Click §r§eto for Tunnel Maps.")
        }
    }

    @SubscribeEvent
    fun onGuiContainerSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        if (event.clickedButton != 1) return
        clickTranslate[event.slotId]?.let {
            isCommission = true
            setActiveAndGoal(it)
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        graph = event.getConstant<Graph>("island_graphs/GLACITE_TUNNELS", gson = Graph.gson)
        possibleLocations = graph.groupBy { it.name }.filterNotNullKeys().mapValues { (_, value) ->
            value
        }
        val fairy = mutableMapOf<String, GraphNode>()
        val oldGemstone = mutableMapOf<String, List<GraphNode>>()
        val newGemstone = mutableMapOf<String, List<GraphNode>>()
        val other = mutableMapOf<String, List<GraphNode>>()
        possibleLocations.forEach { (key, value) ->
            when {
                key.contains("Campfire") -> campfire = value.first()
                key.contains("Fairy") -> fairy[key] = value.first()
                newGemstonePattern.matches(key) -> newGemstone[key] = value
                oldGemstonePattern.matches(key) -> oldGemstone[key] = value
                else -> {
                    // ignore node names without color codes
                    if (key.removeColor() != key) {
                        other[key] = value
                    }
                }
            }
        }
        fairySouls = fairy
        this.newGemstones = newGemstone
        this.oldGemstones = oldGemstone
        normalLocations = other
        translateTable.clear()
        DelayedRun.runNextTick {
            // Needs to be delayed since the config may not be loaded
            locationDisplay = generateLocationsDisplay()
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        onToggle(
            config.compactGemstone,
            config.excludeFairy,
        ) {
            locationDisplay = generateLocationsDisplay()
        }
    }

    @SubscribeEvent
    fun onRenderDisplay(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        val display = buildList<Renderable> {
            if (active.isNotEmpty()) {
                if (goal == campfire && active != campfire.name) {
                    add(Renderable.string("§6Override for ${campfire.name}"))
                    add(
                        Renderable.clickable(
                            Renderable.string("§eMake §f$active §eactive"),
                            onClick = {
                                goal = getNext()
                            },
                        ),
                    )
                } else {
                    add(
                        Renderable.clickAndHover(
                            Renderable.string("§6Active: §f$active"),
                            listOf("§eClick to disable current Waypoint"),
                            onClick = ::clearPath,
                        ),
                    )
                    if (hasNext()) {
                        add(
                            Renderable.clickable(
                                Renderable.string("§eNext Spot"),
                                onClick = {
                                    goal = getNext()
                                },
                            ),
                        )
                    } else {
                        addString("")
                    }
                }
            } else {
                addString("")
                addString("")
            }
            addAll(locationDisplay)
        }
        config.position.renderRenderables(display, posLabel = "Tunnels Maps")

    }

    private fun generateLocationsDisplay() = buildList {
        add(Renderable.string("§6Locations:"))
        add(
            Renderable.multiClickAndHover(
                campfire.name!!,
                listOf(
                    "§eLeft Click to set active", "§eRight Click for override",
                ),
                click = mapOf(
                    0 to guiSetActive(campfire.name!!), 1 to ::campfireOverride,
                ),
            ),
        )
        if (!config.excludeFairy.get()) {
            add(
                Renderable.hoverable(
                    Renderable.horizontalContainer(
                        listOf(Renderable.string("§dFairy Souls")) + fairySouls.map {
                            val name = it.key.removePrefix("§dFairy Soul ")
                            Renderable.clickable(Renderable.string("§d[$name]"), onClick = guiSetActive(it.key))
                        },
                    ),
                    Renderable.string("§dFairy Souls"),
                ),
            )
        }
        if (config.compactGemstone.get()) {
            add(
                Renderable.table(
                    listOf(
                        newGemstones.map(::toCompactGemstoneName), oldGemstones.map(::toCompactGemstoneName),
                    ),
                ),
            )
        } else {
            addAll(
                newGemstones.map {
                    Renderable.clickable(Renderable.string(it.key), onClick = guiSetActive(it.key))
                },
            )
            addAll(
                oldGemstones.map {
                    Renderable.clickable(Renderable.string(it.key), onClick = guiSetActive(it.key))
                },
            )
        }
        addAll(
            normalLocations.map {
                Renderable.clickable(Renderable.string(it.key), onClick = guiSetActive(it.key))
            },
        )
    }

    private fun toCompactGemstoneName(it: Map.Entry<String, List<GraphNode>>): Renderable = Renderable.clickAndHover(
        Renderable.string(
            (it.key.getFirstColorCode()?.let { "§$it" } ?: "") + ("ROUGH_".plus(
                it.key.removeColor().removeSuffix("stone"),
            ).asInternalName().itemName.takeWhile { it != ' ' }.removeColor()),
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
        ),
        tips = listOf(it.key),
        onClick = guiSetActive(it.key),
    )

    private fun campfireOverride() {
        goalReached = false
        goal = campfire
    }

    private fun setActiveAndGoal(it: String) {
        active = it
        goal = getNext()
    }

    private fun guiSetActive(it: String): () -> Unit = {
        isCommission = false
        setActiveAndGoal(it)
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (checkGoalReached()) return
        val prevclosest = closestNode
        closestNode = graph.minBy { it.position.distanceSqToPlayer() }
        val closest = closestNode ?: return
        val goal = goal ?: return
        if (closest == prevclosest && goal == prevGoal) return
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
                this.path = Graph(path.drop(1)) to (distance - firstPath + direct)
                return
            }
        }
        this.path = path to (distance + nodeDistance)
    }

    private fun checkGoalReached(): Boolean {
        if (goalReached) return true
        val goal = goal ?: return false
        val distance = goal.position.distanceSqToPlayer()
        goalReached = distance < if (goal == campfire) {
            15.0 * 15.0
        } else {
            6.0 * 6.0
        }
        if (goalReached) {
            if (goal == campfire && active != campfire.name) {
                this.goal = getNext()
            } else {
                cooldowns[goal] = 60.0.seconds.fromNow()
                clearPath()
            }
            return true
        }
        return false
    }

    private fun clearPath() {
        path = null
        goal = null
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        val path = path?.takeIf { it.first.isNotEmpty() } ?: return
        event.draw3DPathWithWaypoint(
            path.first,
            getPathColor(),
            config.pathWidth.toInt(),
            true,
            bezierPoint = 2.0,
            textSize = config.textSize.toDouble(),
            showNodeNames = true,
        )
        event.drawDynamicText(
            if (config.distanceFirst) {
                path.first.first()
            } else {
                path.first.last()
            }.position,
            "§e${path.second.roundToInt()}m",
            config.textSize.toDouble(),
            yOff = 10f,
        )
    }

    private fun getPathColor(): Color = if (config.dynamicPathColour) {
        goal?.name?.getFirstColorCode()?.toLorenzColor()?.takeIf { it != LorenzColor.WHITE }?.toColor()
    } else {
        null
    } ?: config.pathColour.toChromaColor()

    @SubscribeEvent
    fun onKeyPress(event: LorenzKeyPressEvent) {
        if (!isEnabled()) return
        if (Minecraft.getMinecraft().currentScreen != null) return
        campfireKey(event)
        nextSpotKey(event)
    }

    @SubscribeEvent
    fun onItemClick(event: ItemClickEvent) {
        if (!isEnabled() || !config.leftClickPigeon) return
        if (event.clickType != ClickType.LEFT_CLICK) return
        if (event.itemInHand?.getInternalNameOrNull() != ROYAL_PIGEON) return
        nextSpot()
    }

    private fun campfireKey(event: LorenzKeyPressEvent) {
        if (event.keyCode != config.campfireKey) return
        if (config.travelScroll) {
            HypixelCommands.warp("basecamp")
        } else {
            campfireOverride()
        }
    }

    @SubscribeEvent
    fun onLorenzWarp(event: LorenzWarpEvent) {
        if (!isEnabled()) return
        if (goal != null) {
            DelayedRun.runNextTick {
                goal = getNext()
            }
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (closestNode == null) return // Value that must be none null if it was active
        closestNode = null
        clearPath()
        cooldowns.clear()
        goalReached = false
    }

    private var nextSpotDelay = SimpleTimeMark.farPast()

    private fun nextSpotKey(event: LorenzKeyPressEvent) {
        if (event.keyCode != config.nextSpotHotkey) return
        nextSpot()
    }

    private fun nextSpot() {
        if (!nextSpotDelay.isInPast()) return
        nextSpotDelay = 0.5.seconds.fromNow()
        goal = getNext()
    }

    private val areas = setOf("Glacite Tunnels", "Dwarven Base Camp", "Great Glacite Lake", "Fossil Research Center")

    private fun isEnabled() = IslandType.DWARVEN_MINES.isInIsland() && config.enable && LorenzUtils.skyBlockArea in areas
}
