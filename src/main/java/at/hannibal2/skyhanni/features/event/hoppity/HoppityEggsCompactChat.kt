package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.HandleEvent.Companion.HIGHEST
import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityEggsConfig
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.HoppityStateDataSet
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.BOUGHT
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_FACTORY_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_SHOP_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.resettingEntries
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.HITMAN
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.SIDE_DISH
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.STRAY
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

    fun processChatEvent(
        event: LorenzChatEvent,
        dataSet: HoppityStateDataSet
    ) {
        hoppityDataSet = dataSet.copy()
        compactChat(event)
    }

    private val config get() = ChocolateFactoryAPI.config
    private val eventConfig get() = SkyHanniMod.feature.event.hoppityEggs
    private val rarityConfig get() = HoppityEggsManager.config.rarityInCompact

    private fun compactChat(event: LorenzChatEvent) {
        if (!HoppityEggsManager.config.compactChat) return
        event.blockedReason = "compact_hoppity"
        hoppityDataSet.hoppityMessages.add(event.message)
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
        hoppityDataSet.hoppityMessages.clear()
        ChatUtils.clickableChat(
            createCompactMessage(),
            hover = hover,
            onClick = onClick,
            expireAt = 30.seconds.fromNow(),
            oneTimeClick = true,
            prefix = false,
        )
    }

    @HandleEvent(priority = HIGHEST)
    fun onEggFound(event: EggFoundEvent) {
        if (!HoppityEggsManager.config.compactChat) return
        hoppityDataSet.lastMeal = event.type

        val message = when (event.type) {
            SIDE_DISH ->
                "§d§lHOPPITY'S HUNT §r§dYou found a §r§6§lSide Dish §r§6Egg §r§din the Chocolate Factory§r§d!"
            CHOCOLATE_FACTORY_MILESTONE ->
                "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lChocolate Milestone Rabbit §r§din the Chocolate Factory§r§d!"
            CHOCOLATE_SHOP_MILESTONE ->
                "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lShop Milestone Rabbit §r§din the Chocolate Factory§r§d!"
            STRAY ->
                "§d§lHOPPITY'S HUNT §r§dYou found a §r§aStray Rabbit§r§d!"

            // Each of these have their own from-Hypixel chats, so we don't need to add a message here
            in resettingEntries, HITMAN, BOUGHT -> return
            else -> "§d§lHOPPITY'S HUNT §r§7Unknown Egg Type: §c§l${event.type}"
        }

        hoppityDataSet.hoppityMessages.add(message)
        if (hoppityDataSet.hoppityMessages.size == 3) sendCompact()
    }
}
