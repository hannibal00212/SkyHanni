package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.config.features.gui.customscoreboard.ArrowConfig.ArrowAmountDisplay
import at.hannibal2.skyhanni.data.QuiverApi
import at.hannibal2.skyhanni.data.QuiverApi.NONE_ARROW_TYPE
import at.hannibal2.skyhanni.data.QuiverApi.asArrowPercentage
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.arrowConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.percentageColor

// internal and item in hand
// quiver update event and item in hand event
object ScoreboardElementQuiver : ScoreboardElement() {
    override fun getDisplay(): String {
        val currentArrow = QuiverApi.currentArrow ?: return "§cChange your Arrow once"
        if (currentArrow == NONE_ARROW_TYPE) return "No Arrows selected"

        val colorPrefix = when (arrowConfig.colorArrowAmount) {
            true -> percentageColor(QuiverApi.currentAmount.toLong(), QuiverApi.MAX_ARROW_AMOUNT.toLong()).getChatColor()
            false -> ""
        }

        val amountDisplay = when {
            QuiverApi.wearingSkeletonMasterChestplate -> "∞"
            arrowConfig.arrowAmountDisplay == ArrowAmountDisplay.PERCENTAGE -> "${QuiverApi.currentAmount.asArrowPercentage()}%"
            else -> QuiverApi.currentAmount.addSeparators()
        }

        val amountString = colorPrefix + amountDisplay

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(
            currentArrow.arrow,
            amountString,
            "§f",
        )
    }

    override fun showWhen() = !(informationFilteringConfig.hideIrrelevantLines && !QuiverApi.hasBowInInventory())

    override val configLine = "Flint Arrow: §f1,234"

    override fun showIsland() = !RiftApi.inRift()
}

// click: open /quiver
