package at.hannibal2.skyhanni.features.mining.fossilexcavator

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.mining.FossilExcavationEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ProfitPerExcavation {
    private val config get() = SkyHanniMod.feature.mining.fossilExcavator

    @SubscribeEvent
    fun onFossilExcavation(event: FossilExcavationEvent) {
        if (!config.profitPerExcavation) return
        val loot = event.loot

        var totalProfit = 0.0
        val map = mutableMapOf<String, Double>()
        for ((name, amount) in loot) {
            if (name == "§bGlacite Powder") continue
            NEUInternalName.fromItemNameOrNull(name)?.let {
                val pricePer = it.getPrice()
                if (pricePer == -1.0) continue
                val profit = amount * pricePer
                val text = "§eFound $name §8${amount.addSeparators()}x §7(§6${profit.shortFormat()}§7)"
                map[text] = profit
                totalProfit += profit
            }
        }

        val scrapItem = FossilExcavatorAPI.scrapItem

        val scrapPrice = scrapItem.getPrice()
        map["${scrapItem.itemName}: §c-${scrapPrice.shortFormat()}"] = -scrapPrice
        totalProfit -= scrapPrice

        val hover = map.sortedDesc().keys.toMutableList()
        val profitPrefix = if (totalProfit < 0) "§c" else "§6"
        val totalMessage = "Profit this excavation: $profitPrefix${totalProfit.shortFormat()}"
        hover.add("")
        hover.add("§e$totalMessage")
        ChatUtils.hoverableChat(totalMessage, hover)
    }
}
