package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.MaxwellAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.maxwellConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboardUtils
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.StringUtils.pluralize

// internal
// power update event
object ScoreboardElementTuning : ScoreboardElement() {
    override fun getDisplay(): Any {
        val tunings = MaxwellAPI.tunings ?: return "§cTalk to \"Maxwell\"!"
        if (tunings.isEmpty()) return "§cNo Maxwell Tunings :("

        val title = pluralize(tunings.size, "Tuning")

        return if (maxwellConfig.compactTuning) {
            val tuningDisplay = tunings.take(3).joinToString("§7, ") { tuning ->
                with(tuning) {
                    CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(value, icon, color)
                }
            }
            CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(title, tuningDisplay, "§f")
        } else {
            val tuningAmount = maxwellConfig.tuningAmount.coerceAtLeast(1)
            val tuningList = tunings.take(tuningAmount).map { tuning ->
                with(tuning) {
                    " §7- §f" + CustomScoreboardUtils.formatScoreboardNumberDisplayDisplay(value, icon, color)
                }
            }
            listOf("$title:") + tuningList
        }
    }

    override val configLine = "Tuning: §c❁34§7, §e⚔20§7, and §9☣7"

    override fun showIsland() = !RiftAPI.inRift()
}
