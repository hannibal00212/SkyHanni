package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityChatConfig
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.HoppityStateDataSet
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.resettingEntries
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEventSummary.getRabbitsFormat
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryTimeTowerManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.takeIfNotEmpty
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

typealias RarityType = HoppityChatConfig.CompactRarityTypes

@SkyHanniModule
object HoppityEggsCompactChat {

    private var hoppityDataSet = HoppityStateDataSet()
    private val config get() = ChocolateFactoryAPI.config
    private val chatConfig get() = HoppityEggsManager.config.chat
    private val waypointsConfig get() = HoppityEggsManager.config.waypoints
    val hitmanCompactDataSets: MutableList<HoppityStateDataSet> = mutableListOf()

    fun compactChat(event: LorenzChatEvent?, dataSet: HoppityStateDataSet) {
        if (!chatConfig.compact) return
        hoppityDataSet = dataSet.copy()
        event?.let { it.blockedReason = "compact_hoppity" }
        if (hoppityDataSet.hoppityMessages.size == 3) sendCompact()
    }

    private fun sendCompact() {
        if (hoppityDataSet.lastMeal?.let { it == HoppityEggType.HITMAN } == true) handleCompactHitman()
        else sendNonHitman()
    }

    private fun sendNonHitman() {
        if (HoppityEggType.resettingEntries.contains(hoppityDataSet.lastMeal) && waypointsConfig.shared) {
            DelayedRun.runDelayed(5.milliseconds) {
                createWaypointShareCompactMessage(HoppityEggsManager.getAndDisposeWaypointOnclick())
                hoppityDataSet.reset()
                hitmanCompactDataSets.clear()
            }
        } else {
            ChatUtils.hoverableChat(createCompactMessage(), hover = hoppityDataSet.hoppityMessages, prefix = false)
            hoppityDataSet.reset()
            hitmanCompactDataSets.clear()
        }
    }

    private fun handleCompactHitman() {
        if (!chatConfig.compactHitman) {
            sendNonHitman()
            return
        }

        hoppityDataSet.let {
            hitmanCompactDataSets.add(it.copy())
            it.reset()
        }
        val sizeNow = hitmanCompactDataSets.size
        DelayedRun.runDelayed(750.milliseconds) {
            if (hitmanCompactDataSets.size != sizeNow) return@runDelayed

            if (hitmanCompactDataSets.size == 1) {
                hoppityDataSet = hitmanCompactDataSets.first() // Pop back out the stored data
                sendNonHitman()
            } else sendHitmanSummary()
        }

    }

    private fun sendHitmanSummary() {
        if (hitmanCompactDataSets.isEmpty()) return
        val summaryMessage = buildString {
            appendLine("§c§lHitman Summary")
            appendLine()

            // Create a Map of LorenzRarity -> Int so we can use the existing EventSummary logic
            val rarityMap: Map<LorenzRarity, Int> = hitmanCompactDataSets.getGroupedRarityMap()
            getRabbitsFormat(rarityMap, "Total Hitman").forEach { appendLine(it) }

            hitmanCompactDataSets.filter { !it.duplicate }.takeIfNotEmpty()?.let { sets ->
                appendLine()
                // Create a Map of LorenzRarity -> Int so we can use the existing EventSummary logic
                val newRarityMap: Map<LorenzRarity, Int> = sets.getGroupedRarityMap()
                getRabbitsFormat(newRarityMap, "New").forEach { appendLine(it) }
            }

            hitmanCompactDataSets.filter { it.duplicate }.takeIfNotEmpty()?.let { sets ->
                appendLine()
                // Create a Map of LorenzRarity -> Int so we can use the existing EventSummary logic
                val dupeRarityMap: Map<LorenzRarity, Int> = sets.getGroupedRarityMap()
                getRabbitsFormat(dupeRarityMap, "Duplicate").forEach { appendLine(it) }

                // Add the total amount of chocolate from duplicates
                val dupeChocolateAmount = sets.sumOf { it.lastDuplicateAmount ?: 0 }
                val timeFormat = dupeChocolateAmount.getChocExtraTimeString()
                appendLine(" §6+${dupeChocolateAmount.addSeparators()} §6Chocolate§7$timeFormat")
            }
        }
        ChatUtils.hoverableChat(
            summaryMessage,
            hover = hitmanCompactDataSets.sortedBy {
                if (it.duplicate) 1 else 0
            }.map { it.createCompactMessage(withMeal = false) },
            prefix = false,
        )
        hitmanCompactDataSets.clear()
    }

    private fun Collection<HoppityStateDataSet>.getGroupedRarityMap(): Map<LorenzRarity, Int> =
        this.mapNotNull { it.lastRarity }
            .groupingBy { it }
            .eachCount()

    private fun Long?.getChocExtraTimeString(): String {
        if (this == null) return "?"
        val extraTime = ChocolateFactoryAPI.timeUntilNeed(this)
        return if (config.showDuplicateTime) ", §a+§b${extraTime.format(maxUnits = 2)}§7" else ""
    }

    private fun HoppityStateDataSet.getNameFormat(): String =
        lastName.takeIf { it.isNotEmpty() } ?: "§C§L???"
    private fun HoppityStateDataSet.getRarityString(): String =
        lastRarity?.let { "${it.chatColorCode}§l${it.rawName}" } ?: "§C§L???"
    private fun HoppityStateDataSet.getRarityFormat(): String = when {
        hoppityDataSet.duplicate && chatConfig.rarityInCompact in listOf(RarityType.BOTH, RarityType.DUPE) -> "${getRarityString()} "
        !hoppityDataSet.duplicate && chatConfig.rarityInCompact in listOf(RarityType.BOTH, RarityType.NEW) -> "${getRarityString()} "
        else -> ""
    }

    private fun HoppityStateDataSet.createCompactMessage(withMeal: Boolean = true): String {
        val mealNameFormat = if (withMeal) when (lastMeal) {
            in resettingEntries -> "${lastMeal?.coloredName.orEmpty()} Egg"
            else -> "${lastMeal?.coloredName.orEmpty()} Rabbit"
        } else ""

        val nameFormat = getNameFormat()
        val rarityFormat = getRarityFormat()

        return if (duplicate) {
            val dupeChocAmount = lastDuplicateAmount?.shortFormat() ?: "?"
            val dupeNumberFormat = if (chatConfig.showDuplicateNumber) {
                (HoppityCollectionStats.getRabbitCount(lastName)).takeIf { it > 0 }?.let {
                    " §7(§b#$it§7)"
                }.orEmpty()
            } else ""

            val timeStr = lastDuplicateAmount.getChocExtraTimeString()
            val dupeChocColor = if (chatConfig.recolorTTChocolate && ChocolateFactoryTimeTowerManager.timeTowerActive()) "§d" else "§6"

            val dupeChocFormat = " §7(§6+$dupeChocColor$dupeChocAmount §6Chocolate§7$timeStr)"

            "$mealNameFormat! §7Duplicate $rarityFormat$nameFormat$dupeNumberFormat$dupeChocFormat"
        } else {
            "$mealNameFormat! §d§lNEW $rarityFormat$nameFormat §7($lastProfit§7)"
        }
    }

    private fun createCompactMessage(withMeal: Boolean = true) = hoppityDataSet.createCompactMessage(withMeal)

    private fun createWaypointShareCompactMessage(onClick: () -> Unit) {
        val hover = hoppityDataSet.hoppityMessages.joinToString("\n") +
            " \n§eClick here to share the location of this chocolate egg with the server!"
        ChatUtils.clickableChat(
            createCompactMessage(),
            hover = hover,
            onClick = onClick,
            expireAt = 30.seconds.fromNow(),
            oneTimeClick = true,
            prefix = false,
        )
    }
}
