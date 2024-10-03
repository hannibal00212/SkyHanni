package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.jsonobjects.repo.RiftWoodenButtonsJson
import at.hannibal2.skyhanni.data.model.GraphNode
import at.hannibal2.skyhanni.data.model.GraphNodeTag
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.features.rift.RiftAPI.isBlowgun
import at.hannibal2.skyhanni.features.rift.everywhere.EnigmaSoulWaypoints.soulLocations
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.block.BlockButtonWood
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object WoodenButtonsHelper {

    private val config get() = RiftAPI.config.enigmaSoulWaypoints

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

    @SubscribeEvent
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

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        hitButtons.clear()
        RiftAPI.allButtonsHit = false
        currentSpot = null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
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
                    config.color.toChromaColor(),
                    condition = { config.showPathFinder && config.showButtonsHelper },
                )
            }
        }
    }

    @SubscribeEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!checkButtons()) return

        val location = event.position
        if (location.getBlockAt() == Blocks.wooden_button && !hitButtons.contains(location)) {
            lastHitButton = event.position
        }
    }

    @SubscribeEvent
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
                !hitButtons.contains(buttonLocation)) {
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

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!checkButtons()) return

        buttonHitPattern.matchMatcher(event.message) {
            addLastHitButton()
        }

        if (event.message != "§eYou've hit all §r§b56 §r§ewooden buttons!") return
        RiftAPI.allButtonsHit = true
        hitButtons = buttonLocations.values.flatten().toMutableSet()
        soulLocations["Buttons"]?.let {
            IslandGraphs.pathFind(
                it,
                "Buttons Enigma Soul",
                config.color.toChromaColor(),
                condition = { config.showPathFinder },
            )
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
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
                event.drawWaypointFilled(button, config.color.toChromaColor(), inverseAlphaScale = true)
            }
        }
    }

    private fun checkButtons() = RiftAPI.inRift() && !RiftAPI.allButtonsHit
    fun showButtons() = checkButtons() && RiftAPI.trackingButtons && config.showButtonsHelper
}
