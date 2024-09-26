package at.hannibal2.skyhanni.features.mining.fossilexcavator.solver

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.SkyHanniMod.Companion.coroutineScope
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.features.mining.fossilexcavator.FossilExcavatorAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FossilSolverDisplay {

    private val config get() = SkyHanniMod.feature.mining.fossilExcavator.solver

    private val patternGroup = RepoPattern.group("mining.fossilexcavator")
    private val chargesRemainingPattern by patternGroup.pattern(
        "chargesremaining",
        "Chisel Charges Remaining: (?<charges>\\d+)",
    )
    private val fossilProgressPattern by patternGroup.pattern(
        "fossilprogress",
        "Fossil Excavation Progress: (?<progress>[\\d.]+%)",
    )

    private val inExcavatorMenu get() = FossilExcavatorAPI.inExcavatorMenu

    private var foundPercentage = false
    private var percentage: String? = null

    var maxCharges = 0
    private var chargesRemaining = 0
    private var possibleFossilsRemaining = 0

    private var slotToClick: Int? = null
    private var correctPercentage: String? = null

    private var isNotPossible = false
    private var isCompleted = false

    private var inventoryItemNames = listOf<String>()

    private const val NOT_POSSIBLE_STRING = "§cNo possible fossils on board."
    private const val SOLVED_STRING = "§aFossil found, get all the loot you can."
    private const val FOSSILS_REMAINING_STRING = "§ePossible fossils remaining: "
    private const val CHARGES_REMAINING_STRING = "§eCharges remaining: "

    var possibleFossilTypes = setOf<FossilType>()

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        clearData()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        clearData()
    }

    private fun clearData() {
        foundPercentage = false
        percentage = null
        chargesRemaining = 0
        slotToClick = null
        correctPercentage = null
        isNotPossible = false
        isCompleted = false
        inventoryItemNames = emptyList()
        possibleFossilTypes = emptySet()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        val slots = InventoryUtils.getItemsInOpenChest()
        val itemNames = slots.map { it.stack.displayName.removeColor() }
        if (itemNames != inventoryItemNames) {
            inventoryItemNames = itemNames
            if (inExcavatorMenu) return

            updateData()
        }
    }

    private fun updateData() {
        val fossilLocations = mutableSetOf<Int>()
        val dirtLocations = mutableSetOf<Int>()

        var foundChargesRemaining = false
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val stack = slot.stack
            val slotIndex = slot.slotIndex
            val stackName = stack.displayName.removeColor()
            val isDirt = stackName == "Dirt"
            val isFossil = stackName == "Fossil"
            when {
                isDirt -> dirtLocations.add(slotIndex)
                isFossil -> fossilLocations.add(slotIndex)
                else -> continue
            }

            if (!foundChargesRemaining) {
                for (line in stack.getLore()) {
                    chargesRemainingPattern.matchMatcher(line.removeColor()) {
                        chargesRemaining = group("charges").toInt()
                        if (maxCharges == 0) maxCharges = chargesRemaining
                        foundChargesRemaining = true
                    }
                }
            }

            if (!isFossil || foundPercentage) continue
            for (line in stack.getLore()) {
                fossilProgressPattern.matchMatcher(line.removeColor()) {
                    foundPercentage = true
                    percentage = group("progress")
                }
            }
        }

        coroutineScope.launch {
            FossilSolver.findBestTile(fossilLocations, dirtLocations, percentage)
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        if (inExcavatorMenu) return

        event.makePickblock()

        val slot = event.slot ?: return
        if (slot.slotIndex == slotToClick) {
            slotToClick = null
            correctPercentage = null
        }
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!isEnabled()) return
        if (inExcavatorMenu) return
        if (slotToClick == null) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex == slotToClick) {
                slot highlight LorenzColor.GREEN.toColor().addAlpha(90)
            }
        }
    }

    @SubscribeEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!isEnabled()) return
        if (!config.showPercentage) return
        if (slotToClick != event.slot.slotNumber) return
        if (inExcavatorMenu) return
        val correctPercentage = correctPercentage ?: return

        event.stackTip = correctPercentage
        event.offsetX = 10
        event.offsetY = 10
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return

        if (inExcavatorMenu) {
            // Render here so they can move it around. As if you press key while doing the excavator you lose the scrap
            config.position.renderString("§eExcavator solver gui", posLabel = "Fossil Excavator Solver")
            return
        }

        val displayList = mutableListOf<String>()

        when {
            isNotPossible -> displayList.add(NOT_POSSIBLE_STRING)
            isCompleted -> displayList.add(SOLVED_STRING)
            else -> displayList.add("${FOSSILS_REMAINING_STRING}§a$possibleFossilsRemaining")
        }
        displayList.add("${CHARGES_REMAINING_STRING}§a$chargesRemaining")

        if (possibleFossilTypes.isNotEmpty()) {
            displayList.add("§ePossible Fossil types:")
            for (fossil in possibleFossilTypes) {
                displayList.add("§7- ${fossil.displayName}")
            }
        }

        config.position.renderStrings(displayList, posLabel = "Fossil Excavator Solver")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(36, "mining.fossilExcavator", "mining.fossilExcavator2.solver")
        event.move(37, "mining.fossilExcavator2", "mining.fossilExcavator")
    }

    fun nextData(slotToClick: FossilTile, correctPercentage: Double, fossilsRemaining: Int) {
        val formattedPercentage = (correctPercentage * 100).roundTo(1)

        possibleFossilsRemaining = fossilsRemaining
        FossilSolverDisplay.slotToClick = slotToClick.toSlotIndex()
        FossilSolverDisplay.correctPercentage = "§2$formattedPercentage%"
    }

    fun showError() {
        isNotPossible = true
    }

    fun showCompleted() {
        isCompleted = true
    }

    private fun isEnabled() = IslandType.DWARVEN_MINES.isInIsland() && config.enabled && FossilExcavatorAPI.inInventory
}
