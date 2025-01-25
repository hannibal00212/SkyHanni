package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.config.features.gui.customscoreboard.ArrowConfig.ArrowAmountDisplay
import at.hannibal2.skyhanni.data.QuiverApi
import at.hannibal2.skyhanni.data.QuiverApi.NONE_ARROW_TYPE
import at.hannibal2.skyhanni.data.QuiverApi.asArrowPercentage
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.arrowConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.percentageColor

// internal and item in hand
// quiver update event and item in hand event
object ScoreboardElementQuiver : ScoreboardElement() {
    override fun getDisplay(): String {
        val currentArrow = QuiverApi.currentArrow ?: return "§cChange your Arrow once"
        if (currentArrow == NONE_ARROW_TYPE) return "No Arrows selected"

        val amountString = (
            if (arrowConfig.colorArrowAmount) {
                percentageColor(
                    QuiverApi.currentAmount.toLong(),
                    QuiverApi.MAX_ARROW_AMOUNT.toLong(),
                ).getChatColor()
            } else ""
            ) +
            if (QuiverApi.wearingSkeletonMasterChestplate) "∞"
            else {
                when (arrowConfig.arrowAmountDisplay) {
                    ArrowAmountDisplay.NUMBER -> QuiverApi.currentAmount.addSeparators()
                    ArrowAmountDisplay.PERCENTAGE -> "${QuiverApi.currentAmount.asArrowPercentage()}%"
                    else -> QuiverApi.currentAmount.addSeparators()
                }
            }

        return if (displayConfig.displayNumbersFirst) "$amountString ${currentArrow.arrow}s"
        else "Arrows: $amountString ${currentArrow.arrow.replace(" Arrow", "")}"
    }

    override fun showWhen() = !(informationFilteringConfig.hideIrrelevantLines && !QuiverApi.hasBowInInventory())

    override val configLine = "Flint Arrow: §f1,234"

    override fun showIsland() = !RiftApi.inRift()
}

// click: open /quiver
