package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HotmData
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.fractionOf
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@SkyHanniModule
object PowderPerHotmPerk {

    private val config get() = SkyHanniMod.feature.mining.hotm

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return

        val itemName = event.itemStack.displayName
        val perk = HotmData.getPerkByNameOrNull(itemName.removeColor()) ?: return

        if (perk.getLevelUpCost() == null) return

        if (config.powderSpent) event.toolTip.add(2, handlePowderSpend(perk))
        if (config.powderFor10Levels) handlePowderFor10Levels(event, perk)
    }

    private fun handlePowderFor10Levels(event: LorenzToolTipEvent, perk: HotmData) {
        if (!Keyboard.KEY_LSHIFT.isKeyHeld()) return

        val indexOfCost = event.toolTip.indexOfFirst { HotmData.perkCostPattern.matches(it) }

        if (indexOfCost == -1) return

        val powderFor10Levels =
            perk.calculateTotalCost((perk.rawLevel + 10).coerceAtMost(perk.maxLevel)) - perk.calculateTotalCost(perk.rawLevel)

        event.toolTip.add(indexOfCost + 2, "§7Powder for 10 levels: §e${powderFor10Levels.addSeparators()}")
    }

    private fun handlePowderSpend(perk: HotmData): String {
        val currentPowderSpend = perk.calculateTotalCost(perk.rawLevel)
        val maxPowderNeeded = perk.totalCostMaxLevel
        val percentage = (currentPowderSpend.fractionOf(maxPowderNeeded) * 100).round(2)

        return when (config.powderSpentDesign) {
            PowderSpentDesign.NUMBER -> {
                if (perk.rawLevel == perk.maxLevel) {
                    "§7Powder spent: §e${maxPowderNeeded.addSeparators()} §7(§aMax level§7)"
                } else {
                    "§7Powder spent: §e${currentPowderSpend.addSeparators()}§7 / §e${maxPowderNeeded.addSeparators()}"
                }
            }

            PowderSpentDesign.PERCENTAGE -> {
                if (perk.rawLevel == perk.maxLevel) {
                    "§7Powder spent: §e$percentage% §7(§aMax level§7)"
                } else {
                    "§7Powder spent: §e$percentage%§7 of max"
                }
            }

            PowderSpentDesign.NUMBER_AND_PERCENTAGE -> {
                if (perk.rawLevel == perk.maxLevel) {
                    "§7Powder spent: §e${maxPowderNeeded.addSeparators()} §7(§aMax level§7)"
                } else {
                    "§7Powder spent: §e${currentPowderSpend.addSeparators()}§7/§e${maxPowderNeeded.addSeparators()}§7 (§e$percentage%§7)"
                }
            }
        }
    }

    enum class PowderSpentDesign(val str: String) {
        NUMBER("Number"),
        PERCENTAGE("Percentage"),
        NUMBER_AND_PERCENTAGE("Number and Percentage");

        override fun toString() = str
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && HotmData.inInventory && (config.powderSpent || config.powderFor10Levels)
}
