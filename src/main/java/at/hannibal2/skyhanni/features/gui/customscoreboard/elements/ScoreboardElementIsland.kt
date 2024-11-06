package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.utils.LorenzUtils

// internal
// update on island change event
object ScoreboardElementIsland : ScoreboardElement() {
    override fun getDisplay() = "§7㋖ §a" + LorenzUtils.skyBlockIsland.displayName

    override val configLine = "§7㋖ §aHub"
}

// click: open /warp
