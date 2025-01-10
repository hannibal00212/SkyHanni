package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.transformAt
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object StockOfStonkFeature {

    private val config get() = SkyHanniMod.feature.inventory

    private val patternGroup = RepoPattern.group("inventory.stockofstonks")

    /**
     * REGEX-TEST: Stonks Auction
     */
    private val inventoryPattern by patternGroup.pattern(
        "inventory",
        "Stonks Auction",
    )

    /**
     * REGEX-TEST: §dStonks Auction
     */
    private val itemPattern by patternGroup.pattern(
        "item",
        "§dStonks Auction",
    )

    /**
     * REGEX-TEST: §5§o§7§7▶ §c§lTOP 5,000§7 - §5Stock of Stonks §8x2
     * REGEX-TEST: §5§o§7§a▶ §a§lTOP 100§7 - §5Stock of Stonks §8x25
     */
    private val topPattern by patternGroup.pattern(
        "top",
        "§5§o§7§.▶ §.§lTOP (?<rank>[\\d,]+)§7 - §5Stock of Stonks §8x(?<amount>\\d+)",
    )

    /**
     * REGEX-TEST: §5§o§7   Minimum Bid: §62,400,002 Coins
     */
    private val bidPattern by patternGroup.pattern(
        "bid",
        "§5§o§7   Minimum Bid: §6(?<amount>[\\d,]+) Coins",
    )

    var inInventory = false

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (isEnabled()) {
            inInventory = inventoryPattern.matches(event.inventoryName)
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onLorenzToolTip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (!inInventory) return
        if (!itemPattern.matches(event.itemStack.displayName)) return
        var stonksReward = 0
        var index = 0
        var bestValueIndex = 0
        var bestRatio = Long.MAX_VALUE
        loop@ while (index < event.toolTip.size) {
            val line = event.toolTip[index]
            index++
            topPattern.matchMatcher(line) {
                stonksReward = group("amount").toInt()
                continue@loop
            }
            bidPattern.matchMatcher(line) {
                val cost = group("amount").formatLong().coerceAtLeast(2000000) // minimum bid is 2,000,000
                val ratio = cost / stonksReward.transformIf({ this == 0 }, { 1 })
                event.toolTip[index - 1] = line + " §7(§6§6${ratio.addSeparators()} §7per)" // double §6 for the replacement at the end
                if (ratio < bestRatio) {
                    bestValueIndex = index - 1
                    bestRatio = ratio
                }
            }
        }
        event.toolTip.transformAt(bestValueIndex) { replace("§6§6", "§a") }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.stonkOfStonkPrice
}
