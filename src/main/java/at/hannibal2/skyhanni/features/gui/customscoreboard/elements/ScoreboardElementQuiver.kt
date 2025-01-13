package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.config.features.gui.customscoreboard.ArrowConfig.ArrowAmountDisplay
import at.hannibal2.skyhanni.data.QuiverAPI
import at.hannibal2.skyhanni.data.QuiverAPI.NONE_ARROW_TYPE
import at.hannibal2.skyhanni.data.QuiverAPI.asArrowPercentage
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.arrowConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.percentageColor

// internal and item in hand
// quiver update event and item in hand event
object ScoreboardElementQuiver : ScoreboardElement() {
    override fun getDisplay(): String {
        val currentArrow = QuiverAPI.currentArrow ?: return "§cChange your Arrow once"
        if (currentArrow == NONE_ARROW_TYPE) return "No Arrows selected"

        val colorPrefix = when (arrowConfig.colorArrowAmount) {
            true -> percentageColor(QuiverAPI.currentAmount.toLong(), QuiverAPI.MAX_ARROW_AMOUNT.toLong()).getChatColor()
            false -> ""
        }

        val amountDisplay = when {
            QuiverAPI.wearingSkeletonMasterChestplate -> "∞"
            arrowConfig.arrowAmountDisplay == ArrowAmountDisplay.PERCENTAGE -> "${QuiverAPI.currentAmount.asArrowPercentage()}%"
            else -> QuiverAPI.currentAmount.addSeparators()
        }

        val amountString = colorPrefix + amountDisplay

        return CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(
            currentArrow.arrow,
            amountString,
            "§f",
        )
    }

    override fun showWhen() = !(informationFilteringConfig.hideIrrelevantLines && !QuiverAPI.hasBowInInventory())

    override val configLine = "Flint Arrow: §f1,234"

    override fun showIsland() = !RiftAPI.inRift()
}

// click: open /quiver
