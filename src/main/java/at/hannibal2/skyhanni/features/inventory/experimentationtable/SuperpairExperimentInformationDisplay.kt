package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent.SlotClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
// TODO important: all use cases of listOf in combination with string needs to be gone. no caching, constant new list creation, and bad design.
object SuperpairExperimentInformationDisplay {

    private val config get() = SkyHanniMod.feature.inventory.experimentationTable

    private var display = emptyList<String>()

    private var uncoveredAt = 0
    private var uncoveredItems = mutableListOf<Pair<Int, String>>()
    private var possiblePairs = 0

    data class Item(val index: Int, val name: String)
    data class ItemPair(val first: Item, val second: Item)

    // TODO remove string. use enum instead! maybe even create new data type instead of map of pairs
    private var found = mutableMapOf<Pair<Item?, ItemPair?>, String>()

    private var toCheck = mutableListOf<Pair<Int, Int>>()
    private var lastClicked = mutableListOf<Pair<Int, Int>>()
    private var lastClick = SimpleTimeMark.farPast()
    private var currentExperiment = Experiment.NONE
    private var instantFind = 0

    private val sideSpaces1 = listOf(17, 18, 26, 27, 35, 36)
    private val sideSpaces2 = listOf(16, 17, 18, 19, 25, 26, 27, 28, 34, 35, 36, 37)

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        display = emptyList()

        uncoveredAt = 0
        uncoveredItems.clear()
        possiblePairs = 0

        found.clear()
        toCheck.clear()
        lastClicked.clear()
        lastClick = SimpleTimeMark.farPast()
        currentExperiment = Experiment.NONE
        instantFind = 0
    }

    @SubscribeEvent
    fun onChestGuiOverlayRendered(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.superpairDisplayPosition.renderStrings(display, posLabel = "Superpair Experiment Information")
        display = checkItems(toCheck)
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!isEnabled()) return
        currentExperiment = ExperimentationTableAPI.getCurrentExperiment() ?: return

        if (isOutOfBounds(event.slotId, currentExperiment)) return
        val item = event.item ?: return
        if (item.displayName.removeColor() == "?") return
        val clicksItem = InventoryUtils.getItemAtSlotIndex(4)

        if (lastClicked.none { it.first == event.slotId && it.second == uncoveredAt } && lastClick.passedSince() > 100.milliseconds) {
            if (clicksItem != null && clicksItem.displayName.removeColor().split(" ")[1] == "0") return
            lastClicked.add(Pair(event.slotId, uncoveredAt))
            lastClick = SimpleTimeMark.now()
            toCheck.add(event.slotId to uncoveredAt)
            uncoveredAt += 1
        }
    }

    private fun checkItems(check: MutableList<Pair<Int, Int>>): List<String> {
        currentExperiment = ExperimentationTableAPI.getCurrentExperiment() ?: return listOf()
        if (check.isEmpty()) return drawDisplay()

        for ((slot, uncovered) in check) {
            val itemNow = InventoryUtils.getItemAtSlotIndex(slot) ?: return drawDisplay()
            val itemName = itemNow.displayName.removeColor()

            if (isWaiting(itemName) || isOutOfBounds(slot, currentExperiment)) return drawDisplay()

            val reward = convertToReward(itemNow)
            if (uncoveredItems.none { it.first == slot }) uncoveredItems.add(Pair(slot, reward))

            when {
                isPowerUp(reward) -> handlePowerUp(slot, reward)
                isReward(itemName) -> handleReward(slot, uncovered, reward)
            }

            possiblePairs = calculatePossiblePairs()

            val since = clicksSinceSeparator(lastClicked)

            if ((since >= 2 || (since == -1 && lastClicked.size >= 2)) && instantFind == 0) {
                lastClicked.add(-1 to uncoveredAt)
                uncoveredAt += 1
            }
            toCheck.removeIf { it.first == slot }

            return drawDisplay()
        }
        possiblePairs = calculatePossiblePairs()
        return drawDisplay()
    }

    private fun handlePowerUp(slot: Int, reward: String) {
        val item = toEither(Item(slot, reward))

        found[item] = "Powerup"
        possiblePairs--
        lastClicked.removeIf { it.first == slot }
        uncoveredAt -= 1
        if (reward == "Instant Find") instantFind += 1
    }

    private fun handleReward(slot: Int, uncovered: Int, reward: String) {
        val lastSlotClicked =
            if (instantFind == 0 && lastClicked.none { it.first == -1 && it.second == uncovered - 1 } && lastClicked.size != 1) lastClicked.find { it.second == uncovered - 1 }
                ?: return else lastClicked.find { it.second == uncovered } ?: return

        val lastItem = InventoryUtils.getItemAtSlotIndex(lastSlotClicked.first) ?: return
        val itemClicked = InventoryUtils.getItemAtSlotIndex(slot) ?: return

        val lastItemName = convertToReward(lastItem)

        if (isWaiting(lastItemName)) return

        when {
            instantFind >= 1 -> {
                handleFoundPair(slot, reward, lastSlotClicked.first)
                instantFind -= 1
                lastClicked.add(-1 to uncoveredAt)
                uncoveredAt += 1
            }

            hasFoundPair(slot, lastSlotClicked.first, reward, lastItemName) && lastItem.itemDamage == itemClicked.itemDamage -> handleFoundPair(
                slot,
                reward,
                lastSlotClicked.first,
            )

            hasFoundMatch(slot, reward) -> handleFoundMatch(slot, reward)
            else -> handleNormalReward(slot, reward)
        }

    }

    private fun handleFoundPair(
        slot: Int,
        reward: String,
        lastSlotClicked: Int,
    ) {
        val pair = toEither(ItemPair(Item(slot, reward), Item(lastSlotClicked, reward)))

        found[pair] = "Pair"
        found.entries.removeIf {
            it.value == "Match" && right(it.key).first.index == slot
        }
        found.entries.removeIf {
            it.value == "Normal" && (left(it.key).index == slot || left(it.key).index == lastSlotClicked)
        }
    }

    private fun handleFoundMatch(slot: Int, reward: String) {
        val match = uncoveredItems.find { it.second == reward }?.first ?: return
        val pair = toEither(ItemPair(Item(slot, reward), Item(match, reward)))

        if (found.none {
                listOf("Pair", "Match").contains(it.value) && (right(it.key).first.index == slot)
            }) found[pair] = "Match"
        found.entries.removeIf { it.value == "Normal" && (left(it.key).index == slot || left(it.key).index == match) }
    }

    private fun handleNormalReward(slot: Int, reward: String) {
        val item = toEither(Item(slot, reward))

        if (found.none {
                listOf("Match", "Pair").contains(it.value) && (right(it.key).first.index == slot || right(it.key).second.index == slot)
            } && found.none { it.value == "Normal" && left(it.key).index == slot }) found[item] = "Normal"
    }

    private fun calculatePossiblePairs() =
        ((currentExperiment.gridSize - 2) / 2) - found.filter { listOf("Pair", "Match", "Normal").contains(it.value) }.size

    private fun drawDisplay() = buildList {
        add("§6Superpair Experimentation Data")
        add("")

        val pairs = found.entries.filter { it.value == "Pair" }
        val matches = found.entries.filter { it.value == "Match" }
        val powerups = found.entries.filter { it.value == "Powerup" }
        val normals = found.entries.filter { it.value == "Normal" }

        if (pairs.isNotEmpty()) add("§2Found")
        for (pair in pairs) {
            val prefix = determinePrefix(pairs.indexOf(pair), pairs.lastIndex)
            add(" $prefix §a${right(pair.key).first.name}")
        }
        if (matches.isNotEmpty()) add("§eMatched")
        for (match in matches) {
            val prefix = determinePrefix(matches.indexOf(match), matches.lastIndex)
            add(" $prefix §e${right(match.key).first.name}")
        }
        if (powerups.isNotEmpty()) add("§bPowerUp")
        for (powerup in powerups) {
            val prefix = determinePrefix(powerups.indexOf(powerup), powerups.size - 1)
            add(" $prefix §b${left(powerup.key).name}")
        }
        val toAdd = mutableListOf<String>()
        if (possiblePairs >= 1) toAdd.add("§ePairs - $possiblePairs")
        if (2 - powerups.size >= 1) toAdd.add("§bPowerUps - ${2 - powerups.size}")
        if (normals.isNotEmpty()) toAdd.add("§7Normals - ${normals.size}")

        if (toAdd.isNotEmpty()) {
            add("")
            add("§4Not found")
        }
        for (string in toAdd) if (string != toAdd.last()) add(" ├ $string") else add(" └ $string")
    }

    private fun convertToReward(item: ItemStack) = if (item.displayName.removeColor() == "Enchanted Book") item.getLore()[2].removeColor()
    else item.displayName.removeColor()

    private fun determinePrefix(index: Int, lastIndex: Int) = if (index == lastIndex) "└" else "├"

    private fun hasFoundPair(
        firstSlot: Int,
        secondSlot: Int,
        firstName: String,
        secondName: String,
    ) = firstSlot != secondSlot && firstName == secondName

    private fun hasFoundMatch(itemSlot: Int, reward: String) =
        uncoveredItems.any { (slot, name) -> slot != itemSlot && name == reward } && found.none {
            listOf("Pair", "Match").contains(it.value) && (right(it.key).first.index == itemSlot || right(it.key).second.index == itemSlot)
        }

    private fun isPowerUp(reward: String) = ExperimentationTableAPI.powerUpPattern.matches(reward)

    private fun isReward(reward: String) = ExperimentationTableAPI.rewardPattern.matches(reward)

    private fun isWaiting(itemName: String) =
        listOf("Click any button!", "Click a second button!", "Next button is instantly rewarded!").contains(itemName)

    private fun clicksSinceSeparator(list: MutableList<Pair<Int, Int>>): Int {
        val lastIndex = list.indexOfLast { it.first == -1 }
        return if (lastIndex != -1) list.size - 1 - lastIndex else -1
    }

    private fun isOutOfBounds(slot: Int, experiment: Experiment): Boolean =
        slot <= experiment.startSlot || slot >= experiment.endSlot || (if (experiment.sideSpace == 1) slot in sideSpaces1 else slot in sideSpaces2)

    // TODO remove left and right, use custom data type instead
    private fun left(it: Pair<Item?, ItemPair?>): Item = it.first ?: Item(-1, "")

    private fun right(it: Pair<Item?, ItemPair?>): ItemPair = it.second ?: ItemPair(Item(-1, ""), Item(-1, ""))

    private fun toEither(it: Any): Pair<Item?, ItemPair?> = if (it is Item) it to null else null to it as ItemPair

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.superpairDisplay && ExperimentationTableAPI.getCurrentExperiment() != null
}
