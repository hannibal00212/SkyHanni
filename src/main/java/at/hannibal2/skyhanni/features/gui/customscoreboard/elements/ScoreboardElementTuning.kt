package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.maxwell.MaxwellAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.maxwellConfig
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.StringUtils.pluralize

// internal
// power update event
object ScoreboardElementTuning : ScoreboardElement() {
    override fun getDisplay(): Any {
        val tunings = MaxwellAPI.tunings
        if (tunings.isEmpty()) return "§cNo Maxwell Tunings :("

        val title = pluralize(tunings.size, "Tuning")

        return if (maxwellConfig.compactTuning) {
            val tuningDisplay = tunings.take(3).joinToString("§7, ") { tuning ->
                with(tuning) {
                    if (displayConfig.displayNumbersFirst) "$color$value$icon"
                    else "$color$icon$value"
                }
            }
            if (displayConfig.displayNumbersFirst) "$tuningDisplay §f$title"
            else "$title: $tuningDisplay"
        } else {
            val tuningAmount = maxwellConfig.tuningAmount.coerceAtLeast(1)
            val tuningList = tunings.take(tuningAmount).map { tuning ->
                with(tuning) {
                    " §7- §f" + if (displayConfig.displayNumbersFirst) "$color$value $icon $name"
                    else "$name: $color$value$icon"
                }
            }
            listOf("$title:") + tuningList
        }
    }

    override val configLine = "Tuning: §c❁34§7, §e⚔20§7, and §9☣7"

    override fun showIsland() = !RiftAPI.inRift()
}
