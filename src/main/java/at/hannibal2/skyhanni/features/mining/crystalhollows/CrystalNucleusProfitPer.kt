package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.HandleEvent.Companion.HIGH
import at.hannibal2.skyhanni.events.mining.CrystalNucleusLootEvent
import at.hannibal2.skyhanni.features.mining.crystalhollows.CrystalNucleusApi.JUNGLE_KEY_ITEM
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat

@SkyHanniModule
object CrystalNucleusProfitPer {
    private val config get() = SkyHanniMod.feature.mining.crystalNucleusTracker

    @HandleEvent(priority = HIGH)
    fun onCrystalNucleusLoot(event: CrystalNucleusLootEvent) {
        if (!config.profitPer) return
        val loot = event.loot

        var totalProfit = 0.0
        val map = mutableMapOf<String, Double>()
        for ((internalName, amount) in loot) {
            internalName.getPrice().takeIf { price: Double -> price != -1.0 }?.let { pricePer: Double ->
                val profit: Double = amount * pricePer
                val nameFormat = internalName.itemName
                val text = "§eFound $nameFormat §8${amount.addSeparators()}x §7(§6${profit.shortFormat()}§7)"
                map.addOrPut(text, profit)
                totalProfit += profit
            }
        }

        val hover = map.sortedDesc().filter {
            (it.value >= config.profitPerMinimum)
        }.keys.toMutableList()

        // Account for excluded items
        map.filter { it.key !in hover }.takeIf { it.isNotEmpty() }?.let {
            hover.add("§7${it.size} cheap items are hidden §7(§6${it.values.sum().shortFormat()}§7).")
        }

        val jungleKeyCost = JUNGLE_KEY_ITEM.getPrice()
        val partsCost = CrystalNucleusApi.getPrecursorRunPrice()
        totalProfit -= (jungleKeyCost + partsCost)

        val profitPrefix = if (totalProfit < 0) "§c" else "§6"
        val totalMessage = "Profit for Crystal Nucleus Run§e: $profitPrefix${totalProfit.shortFormat()}"

        hover.add("")
        hover.add("§cUsed §5Jungle Key§7: §c-${jungleKeyCost.shortFormat()}")
        hover.add("§cUsed §9Robot Parts§7: §c-${partsCost.shortFormat()}")
        hover.add("")
        hover.add("§e$totalMessage")

        ChatUtils.hoverableChat(totalMessage, hover)
    }
}
