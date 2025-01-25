package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.jsonobjects.repo.RiftWoodenButtonsJson
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.model.GraphNodeTag
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.features.rift.RiftApi.isBlowgun
import at.hannibal2.skyhanni.features.rift.everywhere.EnigmaSoulWaypoints.soulLocations
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.block.BlockButtonWood
import net.minecraft.init.Blocks
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object WoodenButtonsHelper {

    private val config get() = RiftApi.config.enigmaSoulWaypoints

    private val patternGroup = RepoPattern.group("rift.area.dreadfarm.buttons")

    /**
     * REGEX-TEST: §eYou have hit §r§b1/56 §r§eof the wooden buttons!
     * REGEX-TEST: §eYou have hit §r§b10/56 §r§eof the wooden buttons!
     */
    private val buttonHitPattern by patternGroup.pattern(
        "hit",
        "§eYou have hit §r§b\\d+/56 §r§eof the wooden buttons!",
    )

    private var buttonLocations = mapOf<String, List<LorenzVec>>()
    private var hitButtons = mutableSetOf<LorenzVec>()
    private var lastHitButton: LorenzVec? = null
    private var currentSpot: GraphNode? = null
    private var lastBlowgunFire = SimpleTimeMark.farPast()

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<RiftWoodenButtonsJson>("rift/RiftWoodenButtons")
        buttonLocations = mutableMapOf<String, List<LorenzVec>>().apply {
            data.houses.forEach { (houseName, spots) ->
                spots.forEach { spot ->
                    this["$houseName House:${spot.position}"] = spot.buttons
                }
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        hitButtons.clear()
        RiftApi.allButtonsHit = false
        currentSpot = null
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        findClosestSpot()
        checkBlowgunActivatedButtons()
    }

    private fun findClosestSpot() {
        if (!showButtons()) return
        val graph = IslandGraphs.currentIslandGraph ?: return

        val closestNode = graph.nodes
            .filter { it.tags.contains(GraphNodeTag.RIFT_BUTTONS_QUEST) }
            .filter { node ->
                val spotName = "${node.name}:${node.position}"
                val buttonsAtSpot = buttonLocations[spotName] ?: return@filter false
                buttonsAtSpot.any { !hitButtons.contains(it) }
            }
            .minByOrNull { it.position.distanceToPlayer() }

        if (closestNode != currentSpot) {
            currentSpot = closestNode
            currentSpot?.let {
                IslandGraphs.pathFind(
                    it.position,
                    "Button Spot",
                    config.color.toSpecialColor(),
                    condition = { config.showPathFinder && config.showButtonsHelper },
                )
            }
        }
    }

    @HandleEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!checkButtons()) return

        val location = event.position
        if (location.getBlockAt() == Blocks.wooden_button && !hitButtons.contains(location)) {
            lastHitButton = event.position
        }
    }

    @HandleEvent
    fun onItemClick(event: ItemClickEvent) {
        if (!checkButtons()) return
        if (event.clickType != ClickType.RIGHT_CLICK) return
        if (!event.itemInHand.isBlowgun) return
        lastBlowgunFire = SimpleTimeMark.now()
    }

    private fun checkBlowgunActivatedButtons() {
        if (lastBlowgunFire.passedSince() > 2.5.seconds) return
        buttonLocations.values.flatten().forEach { buttonLocation ->
            val blockState = buttonLocation.getBlockStateAt()
            if (blockState.block is BlockButtonWood &&
                blockState.getValue(BlockButtonWood.POWERED) == true &&
                buttonLocation.canBeSeen(1..3) &&
                lastHitButton != buttonLocation &&
                !hitButtons.contains(buttonLocation)
            ) {
                lastHitButton = buttonLocation
                addLastHitButton()
            }
        }
    }

    private fun addLastHitButton() {
        if (lastHitButton !in hitButtons) {
            lastHitButton?.let { hitButtons.add(it) }
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!checkButtons()) return

        buttonHitPattern.matchMatcher(event.message) {
            addLastHitButton()
        }

        if (event.message != "§eYou've hit all §r§b56 §r§ewooden buttons!") return
        RiftApi.allButtonsHit = true
        hitButtons = buttonLocations.values.flatten().toMutableSet()
        soulLocations["Buttons"]?.let {
            IslandGraphs.pathFind(
                it,
                "Buttons Enigma Soul",
                config.color.toSpecialColor(),
                condition = { config.showPathFinder },
            )
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!showButtons()) return

        val spot = currentSpot ?: return
        val distance = spot.position.distanceToPlayer()
        if (distance > 2.5) {
            event.drawDynamicText(spot.position.add(y = 1), "Hit Buttons Here!", 1.25)
        }

        if (distance > 15.0) return
        val spotName = "${spot.name}:${spot.position}"
        buttonLocations[spotName]?.forEach { button ->
            if (!hitButtons.contains(button)) {
                event.drawWaypointFilled(button, config.color.toSpecialColor(), inverseAlphaScale = true)
            }
        }
    }

    private fun checkButtons() = RiftApi.inRift() && !RiftApi.allButtonsHit
    fun showButtons() = checkButtons() && RiftApi.trackingButtons && config.showButtonsHelper
}
