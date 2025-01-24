package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.features.gui.customscoreboard.recentUnknownLines

// internal
// can just be called from unknown lines handler
object ScoreboardElementUnknown : ScoreboardElement() {
    override fun getDisplay(): Any? {
        if (recentUnknownLines().isEmpty()) return null
        return listOf("§cUndetected Lines:") + recentUnknownLines()
    }

    override val configLine = "§cUnknown lines the mod is not detecting"
}

// click: join skyhanni discord
