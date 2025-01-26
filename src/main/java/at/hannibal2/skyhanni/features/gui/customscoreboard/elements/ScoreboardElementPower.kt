package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.MaxwellApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.displayConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.maxwellConfig
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators

// internal
// power update event?
object ScoreboardElementPower : ScoreboardElement() {
    override fun getDisplay(): String = MaxwellApi.currentPower?.let {
        val mp = if (maxwellConfig.showMagicalPower) "§7(§6${MaxwellApi.magicalPower?.addSeparators()}§7)" else ""
        if (displayConfig.displayNumbersFirst) {
            "§a${it.replace(" Power", "")} Power $mp"
        } else "Power: §a$it $mp"
    } ?: "§cOpen \"Your Bags\"!"

    override val configLine = "Power: §aSighted §7(§61.263§7)"

    override fun showIsland() = !RiftApi.inRift()
}

// click: does a "your bags" command exist?
