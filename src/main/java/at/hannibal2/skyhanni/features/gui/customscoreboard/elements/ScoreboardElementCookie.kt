package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.BitsApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.utils.TimeUtils.format

// internal
// 1s timer
object ScoreboardElementCookie : ScoreboardElement() {
    override fun getDisplay() = buildString {
        append("§dCookie Buff§f: ")
        append(
            BitsApi.cookieBuffTime?.let {
                if (!BitsApi.hasCookieBuff()) "§cNot Active" else it.timeUntil().format(maxUnits = 2)
            } ?: "§cOpen SB Menu!",
        )
    }

    override fun showWhen() = BitsApi.hasCookieBuff() && informationFilteringConfig.hideEmptyLines

    override val configLine = "§dCookie Buff§f: 3d 17h"
}

// click: open sbmenu
