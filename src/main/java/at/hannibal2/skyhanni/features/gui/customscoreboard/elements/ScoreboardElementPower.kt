package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.MaxwellAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.maxwellConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators

// internal
// power update event?
object ScoreboardElementPower : ScoreboardElement() {
    override fun getDisplay(): String = MaxwellAPI.currentPower?.let {
        CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(
            "Power",
            it + if (maxwellConfig.showMagicalPower) " §7(§6${MaxwellAPI.magicalPower?.addSeparators()}§7)" else "",
            "§a",
        )
    } ?: "§cOpen \"Your Bags\"!"

    override val configLine = "Power: §aSighted §7(§61.263§7)"

    override fun showIsland() = !RiftAPI.inRift()
}

// click: does a "your bags" command exist?
