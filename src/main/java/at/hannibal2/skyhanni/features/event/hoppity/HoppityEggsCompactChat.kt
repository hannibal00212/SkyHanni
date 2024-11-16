package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityEggsConfig
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.HoppityStateDataSet
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.resettingEntries
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
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
    private val config get() = ChocolateFactoryAPI.config
    private val eventConfig get() = SkyHanniMod.feature.event.hoppityEggs
    private val rarityConfig get() = HoppityEggsManager.config.rarityInCompact

    fun compactChat(event: LorenzChatEvent?, dataSet: HoppityStateDataSet) {
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

        return if (hoppityDataSet.duplicate) {
            val format = hoppityDataSet.lastDuplicateAmount?.shortFormat() ?: "?"
            val timeFormatted = hoppityDataSet.lastDuplicateAmount?.let {
                ChocolateFactoryAPI.timeUntilNeed(it).format(maxUnits = 2)
            } ?: "?"

            val dupeNumberFormat = if (eventConfig.showDuplicateNumber) {
                (HoppityCollectionStats.getRabbitCount(hoppityDataSet.lastName)).takeIf { it > 0 }?.let {
                    " §7(§b#$it§7)"
                }.orEmpty()
            } else ""

            val showDupeRarity = rarityConfig.let { it == RarityType.BOTH || it == RarityType.DUPE }
            val timeStr = if (config.showDuplicateTime) ", §a+§b$timeFormatted§7" else ""
            "$mealNameFormat! §7Duplicate ${if (showDupeRarity) "${hoppityDataSet.lastRarity} " else ""}" +
                "${hoppityDataSet.lastName}$dupeNumberFormat §7(§6+$format Chocolate§7$timeStr)"
        } else {
            val showNewRarity = rarityConfig.let { it == RarityType.BOTH || it == RarityType.NEW }
            "$mealNameFormat! §d§lNEW ${if (showNewRarity) "$hoppityDataSet.lastRarity " else ""}" +
                "${hoppityDataSet.lastName} §7(${hoppityDataSet.lastProfit}§7)"
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
