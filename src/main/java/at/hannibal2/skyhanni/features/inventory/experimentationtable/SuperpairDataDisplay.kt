package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiContainerEvent.SlotClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentationTableAPI.remainingClicksPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.equalsOneOf
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils.isAnyOf
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

// TODO important: all use cases of listOf in combination with string needs to be gone. no caching, constant new list creation, and bad design.
@SkyHanniModule
object SuperpairDataDisplay {

    private val config get() = SkyHanniMod.feature.inventory.experimentationTable

    private data class SuperpairItem(val index: Int, val reward: String, val damage: Int)
    private data class FoundData(val item: SuperpairItem? = null, val first: SuperpairItem? = null, val second: SuperpairItem? = null)

    private enum class FoundType {
        NORMAL,
        POWERUP,
        MATCH,
        PAIR
    }

    private val sideSpaces1 = listOf(17, 18, 26, 27, 35, 36)
    private val sideSpaces2 = listOf(16, 17, 18, 19, 25, 26, 27, 28, 34, 35, 36, 37)
    private val emptySuperpairItem = SuperpairItem(-1, "", -1)

    private var display = emptyList<String>()
    private var uncoveredItems = mapOf<Int, SuperpairItem>()
    private val found = mutableMapOf<FoundType, MutableList<FoundData>>()

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        display = emptyList()

        uncoveredItems = emptyMap()
        found.clear()
    }

    @SubscribeEvent
    fun onChestGuiOverlayRendered(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (InventoryUtils.openInventoryName() == "Experimentation Table") {
            // Render here so they can move it around.
            config.superpairDisplayPosition.renderString("§6Superpair Experimentation Data", posLabel = "Superpair Experimentation Data")
        }
        if (ExperimentationTableAPI.getCurrentExperiment() == null) return

        if (display.isEmpty()) display = drawDisplay()

        config.superpairDisplayPosition.renderStrings(display, posLabel = "Superpair Experimentation Data")
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!isEnabled()) return
        if (ExperimentationTableAPI.getCurrentExperiment() == null) return

        val currentExperiment = ExperimentationTableAPI.getCurrentExperiment() ?: return

        val item = event.item ?: return
        if (isOutOfBounds(event.slotId, currentExperiment) || item.displayName.removeColor() == "?") return

        val clicksItem = InventoryUtils.getItemAtSlotIndex(4)

        // TODO add variable name to indicate what is going on here
        val items = uncoveredItems.toMutableMap()
        if (items.none { it.value.index == event.slotId && it.key == items.keys.max() }) {
            if (clicksItem != null) {
                remainingClicksPattern.matchMatcher(clicksItem.displayName.removeColor()) { if (group("clicks").toInt() == 0) return }
            }

            handleItem(items, event.slotId)
        }
        uncoveredItems = items
    }

    private fun handleItem(items: MutableMap<Int, SuperpairItem>, slot: Int) = DelayedRun.runDelayed(200.milliseconds) {
        val itemNow = InventoryUtils.getItemAtSlotIndex(slot) ?: return@runDelayed
        val itemName = itemNow.displayName.removeColor()
        val reward = convertToReward(itemNow)
        val itemData = SuperpairItem(slot, reward, itemNow.itemDamage)
        val uncovered = items.keys.maxOrNull() ?: -1

        if (isWaiting(itemName)) return@runDelayed

        if (items.none { it.key == uncovered && it.value.index == slot }) items[uncovered + 1] = itemData

        when {
            isPowerUp(reward) -> handlePowerUp(items, itemData, uncovered + 1)
            isReward(itemName) -> handleReward(items, itemData, uncovered + 1)
        }

        val since = clicksSinceSeparator(items)

        val lastReward = items.entries.last().value.reward
        // TODO use repo patterns for "Instant Find"
        if ((since >= 2 || (since == -1 && items.size >= 2)) && lastReward != "Instant Find") items[uncovered + 2] =
            emptySuperpairItem

        display = drawDisplay()
    }

    private fun handlePowerUp(items: MutableMap<Int, SuperpairItem>, item: SuperpairItem, uncovered: Int) {
        // TODO use repo patterns for "Instant Find"
        if (item.reward != "Instant Find") items.remove(uncovered)

        val itemData = FoundData(item = item)
        found.getOrPut(FoundType.POWERUP) { mutableListOf(itemData) }.apply { if (!contains(itemData)) add(itemData) }
    }

    private fun handleReward(items: MutableMap<Int, SuperpairItem>, item: SuperpairItem, uncovered: Int) {
        val last = items.getOrDefault(uncovered - 1, item)

        if (isWaiting(last.reward)) return

        when {
            // TODO use repo patterns for "Instant Find"
            last.reward == "Instant Find" -> handleInstantFind(items, item, uncovered)
            hasFoundPair(item, last) -> handleFoundPair(item, last)
            hasFoundMatch(items, item) -> handleFoundMatch(items, item)
            else -> handleNormalReward(item)
        }

        println(found)
    }

    private fun handleInstantFind(items: MutableMap<Int, SuperpairItem>, item: SuperpairItem, uncovered: Int) {
        items[uncovered - 1] = item
        items[uncovered] = emptySuperpairItem

        handleFoundPair(item, emptySuperpairItem)
    }

    private fun handleFoundPair(
        first: SuperpairItem,
        second: SuperpairItem,
    ) {
        found.entries.forEach {
            when (it.key) {
                FoundType.MATCH -> it.value.removeIf { data -> data.first?.sameAs(first) ?: false || data.second?.sameAs(first) ?: false }
                FoundType.NORMAL -> it.value.removeIf { data -> data.item?.sameAs(first) ?: false || data.item?.sameAs(second) ?: false }
                else -> {}
            }
        }

        val pairData = FoundData(first = first, second = second)

        found.getOrPut(FoundType.PAIR) { mutableListOf(pairData) }.apply { if (!contains(pairData)) add(pairData) }
    }

    private fun handleFoundMatch(items: MutableMap<Int, SuperpairItem>, item: SuperpairItem) {
        // TODO better name
        val match = items.values.find { it.index != item.index && it.sameAs(item) } ?: return

        found.entries.forEach {
            when {
                it.key.isAnyOf(FoundType.MATCH, FoundType.PAIR) -> {
                    // TODO extract logic in some way
                    if (it.value.any { data ->
                            (data.first?.index ?: -1).equalsOneOf(item.index, match.index) ||
                                (data.second?.index ?: -1).equalsOneOf(item.index, match.index)
                        }
                    ) {
                        return
                    }
                }

                it.key == FoundType.NORMAL -> it.value.removeIf { data ->
                    (data.item?.index ?: -1).equalsOneOf(item.index, match.index)
                }

                else -> {}
            }
        }

        val pairData = FoundData(first = item, second = match)
        found.getOrPut(FoundType.MATCH) { mutableListOf(pairData) }.apply { if (!contains(pairData)) add(pairData) }
    }

    private fun handleNormalReward(item: SuperpairItem) {
        for ((key, value) in found.entries) {
            when {
                key.isAnyOf(FoundType.MATCH, FoundType.PAIR) -> {
                    if (value.any { data ->
                            item.index.equalsOneOf(data.first?.index ?: -1, data.second?.index ?: -1)
                        }
                    ) return
                }

                else ->
                    if (
                        value.any { data ->
                            (data.item?.index ?: -1) == item.index && data.item?.sameAs(item) == true
                        }
                    ) return
            }
        }

        val itemData = FoundData(item = item)
        found.getOrPut(FoundType.NORMAL) { mutableListOf(itemData) }.apply { if (!contains(itemData)) add(itemData) }
    }

    private fun drawDisplay() = buildList {
        val currentExperiment = ExperimentationTableAPI.getCurrentExperiment() ?: return emptyList<String>()

        add("§6Superpair Experimentation Data")
        add("")

        val normals = found.entries.firstOrNull { it.key == FoundType.NORMAL }?.value ?: mutableListOf()
        val powerups = found.entries.firstOrNull { it.key == FoundType.POWERUP }?.value ?: mutableListOf()
        val matches = found.entries.firstOrNull { it.key == FoundType.MATCH }?.value ?: mutableListOf()
        val pairs = found.entries.firstOrNull { it.key == FoundType.PAIR }?.value ?: mutableListOf()
        val possiblePairs = calculatePossiblePairs(currentExperiment)

        if (pairs.isNotEmpty()) add("§2Found")
        for (pair in pairs) {
            val prefix = determinePrefix(pairs.indexOf(pair), pairs.lastIndex)
            add(" $prefix §a${pair.first?.reward.orEmpty()}")
        }
        if (matches.isNotEmpty()) add("§eMatched")
        for (match in matches) {
            val prefix = determinePrefix(matches.indexOf(match), matches.lastIndex)
            add(" $prefix §e${match.first?.reward.orEmpty()}")
        }
        if (powerups.isNotEmpty()) add("§bPowerUp")
        for (powerup in powerups) {
            val prefix = determinePrefix(powerups.indexOf(powerup), powerups.size - 1)
            add(" $prefix §b${powerup.item?.reward.orEmpty()}")
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

    private fun calculatePossiblePairs(currentExperiment: Experiment) =
        ((currentExperiment.gridSize - 2) / 2) - found.filter { it.key != FoundType.POWERUP }.values.sumOf { it.size }

    private fun convertToReward(item: ItemStack) = if (item.displayName.removeColor() == "Enchanted Book") item.getLore()[2].removeColor()
    else item.displayName.removeColor()

    private fun determinePrefix(index: Int, lastIndex: Int) = if (index == lastIndex) "└" else "├"

    private fun hasFoundPair(
        first: SuperpairItem,
        second: SuperpairItem,
    ) = first.index != second.index && first.sameAs(second)

    // TODO extract logic greatly
    private fun hasFoundMatch(items: Map<Int, SuperpairItem>, firstItem: SuperpairItem) =
        items.any { it.value.index != firstItem.index && it.value.sameAs(firstItem) } &&
            found.entries.none {
                it.key.isAnyOf(FoundType.PAIR, FoundType.MATCH) &&
                    it.value.any { data ->
                        firstItem.index.equalsOneOf(data.first?.index ?: -1, data.second?.index ?: -1)
                    }
            }

    private fun isPowerUp(reward: String) = ExperimentationTableAPI.powerUpPattern.matches(reward)

    private fun isReward(reward: String) =
        ExperimentationTableAPI.rewardPattern.matches(reward) || ExperimentationTableAPI.powerUpPattern.matches(reward)

    // TODO use repo patterns instead
    private fun isWaiting(itemName: String) =
        listOf("Click any button!", "Click a second button!", "Next button is instantly rewarded!").contains(itemName)

    private fun clicksSinceSeparator(list: MutableMap<Int, SuperpairItem>): Int {
        val lastIndex = list.entries.indexOfLast { it.value == emptySuperpairItem }
        return if (lastIndex != -1) list.size - 1 - lastIndex else -1
    }

    private fun isOutOfBounds(slot: Int, experiment: Experiment): Boolean =
        slot <= experiment.startSlot ||
            slot >= experiment.endSlot ||
            (if (experiment.sideSpace == 1) slot in sideSpaces1 else slot in sideSpaces2)

    private fun SuperpairItem?.sameAs(other: SuperpairItem) = this?.reward == other.reward && this.damage == other.damage

    private fun isEnabled() = IslandType.PRIVATE_ISLAND.isInIsland() && config.superpairDisplay
}
