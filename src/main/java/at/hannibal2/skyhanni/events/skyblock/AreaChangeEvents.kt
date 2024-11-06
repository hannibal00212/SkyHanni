package at.hannibal2.skyhanni.events.skyblock

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

// Detect area changes by looking at the scoreboard.
class ScoreboardAreaChangeEvent(val area: String, val previousArea: String?) : SkyHanniEvent()
class GraphAreaChangeEvent(val area: String, val previousArea: String?, val onlyInternal: Boolean) : SkyHanniEvent()
