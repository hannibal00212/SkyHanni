package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.HandleEvent.Companion.HIGH
import at.hannibal2.skyhanni.events.mining.CrystalNucleusLootEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat

@SkyHanniModule
object CrystalNucleusProfitPer {
    private val config get() = SkyHanniMod.feature.mining.crystalNucleusTracker

    val jungleKeyItem = "JUNGLE_KEY".asInternalName()
    val robotPartItems = listOf(
        "CONTROL_SWITCH",
        "ELECTRON_TRANSMITTER",
        "FTX_3070",
        "ROBOTRON_REFLECTOR",
        "SUPERLITE_MOTOR",
        "SYNTHETIC_HEART",
    ).map { it.asInternalName() }

    @HandleEvent(priority = HIGH)
    fun onCrystalNucleusLoot(event: CrystalNucleusLootEvent) {
        if (!config.profitPer) return
        val loot = event.loot

        var totalProfit = 0.0
        val map = mutableMapOf<String, Double>()
        for ((internalName, amount) in loot) {
            internalName.getPrice().takeIf { price -> price != -1.0 }?.let { pricePer ->
                val profit = amount * pricePer
                val text = "§eFound ${internalName.itemName} §8${amount.addSeparators()}x §7(§6${profit.shortFormat()}§7)"
                map.addOrPut(text, profit)
                totalProfit += profit
            }
        }

        val jungleKeyPrice = jungleKeyItem.getPrice()
        map["§cUsed §5Jungle Key§7: §c-${jungleKeyPrice.shortFormat()}"] = -jungleKeyPrice
        totalProfit -= jungleKeyPrice

        var robotPartsPrice = 0.0
        robotPartItems.forEach { robotPartsPrice += it.getPrice() }
        map["§cUsed §9Robot Parts§7: §c-${robotPartsPrice.shortFormat()}"] = -robotPartsPrice
        totalProfit -= robotPartsPrice

        val hover = map.sortedDesc().filter {
            (it.value >= config.profitPerMinimum) || it.value < 0
        }.keys.toMutableList()

        if (hover.size != map.size) hover.add("§7${map.size - hover.size} cheap items are hidden.")
        val profitPrefix =
            if (totalProfit < 0) "§c"
            else "§6"
        val totalMessage = "Profit for Crystal Nucleus Run§e: $profitPrefix${totalProfit.shortFormat()}"
        hover.add("")
        hover.add("§e$totalMessage")
    }
}
