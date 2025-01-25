package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityEggsConfig
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityApi.HoppityStateDataSet
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.resettingEntries
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryApi
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryTimeTowerManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

typealias RarityType = HoppityEggsConfig.CompactRarityTypes

@SkyHanniModule
object HoppityEggsCompactChat {

    private var hoppityDataSet = HoppityStateDataSet()
    private val config get() = ChocolateFactoryApi.config
    private val eventConfig get() = SkyHanniMod.feature.event.hoppityEggs
    private val rarityConfig get() = HoppityEggsManager.config.rarityInCompact

    fun compactChat(event: SkyHanniChatEvent?, dataSet: HoppityStateDataSet) {
        if (!HoppityEggsManager.config.compactChat) return
        hoppityDataSet = dataSet.copy()
        event?.let { it.blockedReason = "compact_hoppity" }
        if (hoppityDataSet.hoppityMessages.size == 3) sendCompact()
    }

    private fun sendCompact() {
        if (hoppityDataSet.lastMeal.let { HoppityEggType.resettingEntries.contains(it) } && eventConfig.sharedWaypoints) {
            DelayedRun.runDelayed(5.milliseconds) {
                createWaypointShareCompactMessage(HoppityEggsManager.getAndDisposeWaypointOnclick())
                hoppityDataSet.reset()
            }
        } else {
            ChatUtils.hoverableChat(createCompactMessage(), hover = hoppityDataSet.hoppityMessages, prefix = false)
            hoppityDataSet.reset()
        }
    }

    private fun createCompactMessage(): String {
        val mealNameFormat = when (hoppityDataSet.lastMeal) {
            in resettingEntries -> "${hoppityDataSet.lastMeal?.coloredName.orEmpty()} Egg"
            else -> "${hoppityDataSet.lastMeal?.coloredName.orEmpty()} Rabbit"
        }
        val nameFormat = hoppityDataSet.lastName.takeIf { it.isNotEmpty() } ?: "§C§L???"
        val rarityString = hoppityDataSet.lastRarity?.let { "${it.chatColorCode}§l${it.rawName}" } ?: "§C§L???"
        val rarityFormat = when {
            hoppityDataSet.duplicate && rarityConfig in listOf(RarityType.BOTH, RarityType.DUPE) -> "$rarityString "
            !hoppityDataSet.duplicate && rarityConfig in listOf(RarityType.BOTH, RarityType.NEW) -> "$rarityString "
            else -> ""
        }


        return if (hoppityDataSet.duplicate) {
            val dupeChocAmount = hoppityDataSet.lastDuplicateAmount?.shortFormat() ?: "?"
            val timeFormat = hoppityDataSet.lastDuplicateAmount?.let {
                ChocolateFactoryApi.timeUntilNeed(it).format(maxUnits = 2)
            } ?: "?"
            val dupeNumberFormat = if (eventConfig.showDuplicateNumber) {
                (HoppityCollectionStats.getRabbitCount(hoppityDataSet.lastName)).takeIf { it > 0 }?.let {
                    " §7(§b#$it§7)"
                }.orEmpty()
            } else ""

            val timeStr = if (config.showDuplicateTime) ", §a+§b$timeFormat§7" else ""
            val dupeChocColor = if (eventConfig.recolorTTChocolate && ChocolateFactoryTimeTowerManager.timeTowerActive()) "§d" else "§6"

            val dupeChocFormat = " §7(§6+$dupeChocColor$dupeChocAmount §6Chocolate§7$timeStr)"

            "$mealNameFormat! §7Duplicate $rarityFormat$nameFormat$dupeNumberFormat$dupeChocFormat"
        } else {
            "$mealNameFormat! §d§lNEW $rarityFormat$nameFormat §7(${hoppityDataSet.lastProfit}§7)"
        }
    }

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
