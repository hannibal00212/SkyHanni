package at.hannibal2.skyhanni.config.features.rift.area.mountaintop

import at.hannibal2.skyhanni.config.core.config.Position
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class TimiteConfig {
    @Expose
    @ConfigOption(name = "Timite Timer", desc = "Count down the time until Timite evolves with the time gun.")
    @ConfigEditorBoolean
    var timiteTimer: Boolean = true

    @Expose
    @ConfigOption(name = "Timite Expiry Timer", desc = "Count down the time until Timite/Obsolite expires.")
    @ConfigEditorBoolean
    var timiteExpiryTimer: Boolean = true

    @Expose
    @ConfigOption(name = "Timite Tracker", desc = "Tracks collected Timite ores and shows mote profit")
    @ConfigEditorBoolean
    var timiteTracker: Boolean = false

    @Expose
    @ConfigLink(owner = TimiteConfig::class, field = "timiteTimer")
    var timerPos: Position = Position(421, -220, false, true)

    @Expose
    @ConfigLink(owner = TimiteConfig::class, field = "timiteTracker")
    var trackerPos: Position = Position(-201, -220, false, true)
}
